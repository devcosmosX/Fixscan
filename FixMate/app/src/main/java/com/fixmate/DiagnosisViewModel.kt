package com.fixmate

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fixmate.data.DiagnosisRepository
import com.fixmate.model.ApplianceType
import com.fixmate.model.DiagnosisRequest
import com.fixmate.model.DiagnosisResult
import com.fixmate.model.Symptom
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** UI state for the results screen. */
sealed interface DiagnosisUiState {
    data object Idle : DiagnosisUiState
    data object Analyzing : DiagnosisUiState
    data class Success(val result: DiagnosisResult) : DiagnosisUiState
    data class Error(val message: String) : DiagnosisUiState
}

/** The user's in-progress inputs across the capture + form screens. */
data class FormState(
    val applianceType: ApplianceType = ApplianceType.FRIDGE,
    val brand: String = "",
    val approxAge: String = "1–3 years",
    val selectedSymptomIds: Set<String> = emptySet(),
    val note: String = "",
    val photoUri: String? = null
) {
    val canSubmit: Boolean get() = selectedSymptomIds.isNotEmpty()
}

class DiagnosisViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = DiagnosisRepository(app)

    /** True when a Claude API key is configured; drives the "Live AI" vs "Demo" badge. */
    val usingLiveAi: Boolean get() = repository.usingLiveAi

    private val _form = MutableStateFlow(FormState())
    val form: StateFlow<FormState> = _form.asStateFlow()

    private val _uiState = MutableStateFlow<DiagnosisUiState>(DiagnosisUiState.Idle)
    val uiState: StateFlow<DiagnosisUiState> = _uiState.asStateFlow()

    fun setApplianceType(type: ApplianceType) = _form.update { it.copy(applianceType = type) }
    fun setBrand(value: String) = _form.update { it.copy(brand = value) }
    fun setAge(value: String) = _form.update { it.copy(approxAge = value) }
    fun setNote(value: String) = _form.update { it.copy(note = value) }
    fun setPhoto(uri: String?) = _form.update { it.copy(photoUri = uri) }

    fun toggleSymptom(id: String) = _form.update { state ->
        val updated = state.selectedSymptomIds.toMutableSet()
        if (!updated.add(id)) updated.remove(id)
        state.copy(selectedSymptomIds = updated)
    }

    fun resetForNewDiagnosis() {
        _form.value = FormState()
        _uiState.value = DiagnosisUiState.Idle
    }

    /** Kicks off a diagnosis from the current form state. */
    fun submit() {
        val state = _form.value
        if (!state.canSubmit) return

        val symptomTitles = Symptom.ALL
            .filter { it.id in state.selectedSymptomIds }
            .map { it.title }

        val request = DiagnosisRequest(
            applianceType = state.applianceType.displayName,
            brand = state.brand,
            approxAge = state.approxAge,
            symptoms = symptomTitles,
            note = state.note,
            photoUri = state.photoUri
        )

        _uiState.value = DiagnosisUiState.Analyzing
        viewModelScope.launch {
            try {
                val result = repository.diagnose(request)
                _uiState.value = DiagnosisUiState.Success(result)
            } catch (e: Exception) {
                _uiState.value = DiagnosisUiState.Error(
                    e.message ?: "Something went wrong. Please try again."
                )
            }
        }
    }

    /** Re-run with the same inputs (used by the retry button on errors). */
    fun retry() = submit()
}
