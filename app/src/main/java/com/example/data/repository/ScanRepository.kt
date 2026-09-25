package com.example.data.repository

import android.content.Context
import com.example.data.api.GeminiService
import com.example.data.db.FixScanDatabase
import com.example.data.db.ScanEntity
import com.example.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

import com.example.data.firebase.FirebaseBackendService

class ScanRepository(private val context: Context) {

    private val db = FixScanDatabase.getDatabase(context)
    private val scanDao = db.scanDao()
    private val geminiService = GeminiService(context)
    private val firebaseBackendService = FirebaseBackendService(context)

    val scanHistory: Flow<List<DiagnosisResult>> = scanDao.getAllScans().map { entities ->
        entities.mapNotNull { entityToResult(it) }
    }

    suspend fun diagnoseAndSaveScan(
        applianceType: String,
        brand: String,
        approxAge: String,
        symptoms: List<String>,
        note: String?,
        photoUri: String?
    ): DiagnosisResult {
        val diagnosis = geminiService.diagnoseAppliance(
            applianceType = applianceType,
            brand = brand,
            approxAge = approxAge,
            symptoms = symptoms,
            note = note,
            photoUri = photoUri
        )

        // Save scan result to Room local database and sync with Firebase Firestore backend
        saveScan(diagnosis)
        return diagnosis
    }

    suspend fun saveScan(diagnosis: DiagnosisResult) {
        val entity = resultToEntity(diagnosis)
        scanDao.insertScan(entity)
        // Sync with Firebase Firestore backend
        try {
            firebaseBackendService.saveScanToFirestore(diagnosis)
        } catch (e: Exception) {
            // Room persistence already guarantees local availability offline
        }
    }

    suspend fun getScanById(id: String): DiagnosisResult? {
        val entity = scanDao.getScanById(id) ?: return null
        return entityToResult(entity)
    }

    suspend fun deleteScan(id: String) {
        scanDao.deleteScanById(id)
        try {
            firebaseBackendService.deleteScanFromFirestore(id)
        } catch (e: Exception) {
            // Ignored if offline
        }
    }

    suspend fun clearHistory() {
        scanDao.clearAllScans()
    }

    suspend fun askFollowUp(
        diagnosis: DiagnosisResult,
        history: List<ChatMessage>,
        userMessage: String
    ): String {
        return geminiService.sendFollowUpChat(diagnosis, history, userMessage)
    }

    private fun resultToEntity(result: DiagnosisResult): ScanEntity {
        val symptomsArr = JSONArray()
        result.symptoms.forEach { symptomsArr.put(it) }

        val faultsArr = JSONArray()
        result.likelyFaults.forEach { f ->
            val obj = JSONObject()
            obj.put("fault", f.fault)
            obj.put("confidence", f.confidence.name)
            obj.put("explanation", f.explanation)
            faultsArr.put(obj)
        }

        var safetyStr: String? = null
        result.safetyWarning?.let { s ->
            val obj = JSONObject()
            obj.put("isCritical", s.isCritical)
            obj.put("warningText", s.warningText)
            obj.put("recommendedAction", s.recommendedAction)
            safetyStr = obj.toString()
        }

        val costObj = JSONObject()
        costObj.put("minCost", result.costEstimate.minCost)
        costObj.put("maxCost", result.costEstimate.maxCost)
        costObj.put("currency", result.costEstimate.currency)
        costObj.put("basisNote", result.costEstimate.basisNote)
        val srcArr = JSONArray()
        result.costEstimate.sources.forEach { src ->
            val s = JSONObject()
            s.put("title", src.title)
            s.put("url", src.url)
            srcArr.put(s)
        }
        costObj.put("sources", srcArr)

        val mechArr = JSONArray()
        result.mechanics.forEach { m ->
            val obj = JSONObject()
            obj.put("id", m.id)
            obj.put("name", m.name)
            obj.put("rating", m.rating)
            obj.put("reviewCount", m.reviewCount)
            obj.put("distanceKm", m.distanceKm)
            obj.put("address", m.address)
            obj.put("phone", m.phone)
            val specs = JSONArray()
            m.specialties.forEach { specs.put(it) }
            obj.put("specialties", specs)
            obj.put("isVerified", m.isVerified)
            mechArr.put(obj)
        }

        return ScanEntity(
            id = result.id,
            applianceType = result.applianceType,
            brand = result.brand,
            approxAge = result.approxAge,
            symptomsJson = symptomsArr.toString(),
            note = result.note,
            photoUri = result.photoUri,
            likelyFaultsJson = faultsArr.toString(),
            safetyWarningJson = safetyStr,
            costEstimateJson = costObj.toString(),
            mechanicsJson = mechArr.toString(),
            timestamp = result.timestamp
        )
    }

    private fun entityToResult(entity: ScanEntity): DiagnosisResult? {
        return try {
            val symptomsList = mutableListOf<String>()
            val symArr = JSONArray(entity.symptomsJson)
            for (i in 0 until symArr.length()) {
                symptomsList.add(symArr.getString(i))
            }

            val faultsList = mutableListOf<LikelyFault>()
            val faultsArr = JSONArray(entity.likelyFaultsJson)
            for (i in 0 until faultsArr.length()) {
                val obj = faultsArr.getJSONObject(i)
                val confEnum = try {
                    Confidence.valueOf(obj.getString("confidence"))
                } catch (e: Exception) {
                    Confidence.POSSIBLE
                }
                faultsList.add(
                    LikelyFault(
                        fault = obj.getString("fault"),
                        confidence = confEnum,
                        explanation = obj.getString("explanation")
                    )
                )
            }

            var safetyWarning: SafetyWarning? = null
            if (!entity.safetyWarningJson.isNullOrEmpty()) {
                val obj = JSONObject(entity.safetyWarningJson)
                safetyWarning = SafetyWarning(
                    isCritical = obj.getBoolean("isCritical"),
                    warningText = obj.getString("warningText"),
                    recommendedAction = obj.getString("recommendedAction")
                )
            }

            val costObj = JSONObject(entity.costEstimateJson)
            val srcList = mutableListOf<CostSource>()
            val srcArr = costObj.optJSONArray("sources")
            if (srcArr != null) {
                for (i in 0 until srcArr.length()) {
                    val s = srcArr.getJSONObject(i)
                    srcList.add(CostSource(s.getString("title"), s.getString("url")))
                }
            }
            val costEstimate = CostEstimate(
                minCost = costObj.getInt("minCost"),
                maxCost = costObj.getInt("maxCost"),
                currency = costObj.optString("currency", "₹"),
                basisNote = costObj.optString("basisNote", ""),
                sources = srcList
            )

            val mechanicsList = mutableListOf<Mechanic>()
            val mechArr = JSONArray(entity.mechanicsJson)
            for (i in 0 until mechArr.length()) {
                val obj = mechArr.getJSONObject(i)
                val specs = mutableListOf<String>()
                val specArr = obj.optJSONArray("specialties")
                if (specArr != null) {
                    for (j in 0 until specArr.length()) {
                        specs.add(specArr.getString(j))
                    }
                }
                mechanicsList.add(
                    Mechanic(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        rating = obj.getDouble("rating").toFloat(),
                        reviewCount = obj.getInt("reviewCount"),
                        distanceKm = obj.getDouble("distanceKm"),
                        address = obj.getString("address"),
                        phone = obj.getString("phone"),
                        specialties = specs,
                        isVerified = obj.optBoolean("isVerified", true)
                    )
                )
            }

            DiagnosisResult(
                id = entity.id,
                applianceType = entity.applianceType,
                brand = entity.brand,
                approxAge = entity.approxAge,
                symptoms = symptomsList,
                note = entity.note,
                photoUri = entity.photoUri,
                likelyFaults = faultsList,
                safetyWarning = safetyWarning,
                costEstimate = costEstimate,
                mechanics = mechanicsList,
                timestamp = entity.timestamp
            )
        } catch (e: Exception) {
            null
        }
    }
}
