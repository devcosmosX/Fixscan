package com.fixmate.data

import android.content.Context
import com.fixmate.model.DiagnosisRequest
import com.fixmate.model.DiagnosisResult

/** Single entry point the UI uses to run a diagnosis. */
class DiagnosisRepository(context: Context) {

    private val claude = ClaudeService(context.applicationContext)

    /** True when a real Claude API key is configured (live AI), false when using the offline demo. */
    val usingLiveAi: Boolean get() = claude.hasApiKey

    suspend fun diagnose(request: DiagnosisRequest): DiagnosisResult = claude.diagnose(request)
}
