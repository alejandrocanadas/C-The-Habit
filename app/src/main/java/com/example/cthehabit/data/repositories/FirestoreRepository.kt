package com.example.cthehabit.data.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.cthehabit.data.entity.UserMission

class FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val userId: String
        get() = auth.currentUser?.uid
            ?: throw IllegalStateException("Usuario no autenticado")

    // CUESTIONARIO

    suspend fun saveQuestionnaire(answers: Map<String, List<String>>): Result<Unit> = try {
        db.collection("users")
            .document(userId)
            .collection("questionnaire")
            .document("answers")
            .set(mapOf("answers" to answers))
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // EVENTOS DE USO DIARIO

    suspend fun saveUsageEvent(
        date: String,
        usageMap: Map<String, Long>   // {"com.instagram.android": 123456}
    ): Result<Unit> = try {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        db.collection("users")
            .document(userId)
            .collection("events")
            .document(date)
            .set(usageMap)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // LEER EVENTOS (para gráficas)

    suspend fun getUsageEvents(): Result<Map<String, Map<String, Long>>> = try {
        val snapshot = db.collection("users")
            .document(userId)
            .collection("events")
            .get()
            .await()

        val result = snapshot.documents.associate { doc ->
            doc.id to (doc.data?.mapValues { (_, v) ->
                when (v) {
                    is Long -> v
                    is Number -> v.toLong()
                    else -> 0L
                }
            } ?: emptyMap())
        }
        Result.success(result)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun saveMissions(missions: List<UserMission>): Result<Unit> = try {
        val batch = db.batch()

        missions.forEach { mission ->
            val docRef = db.collection("users")
                .document(userId)
                .collection("missions")
                .document(mission.id)

            batch.set(docRef, mission)
        }

        batch.commit().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteTodayMissions(today: String) {
        val snapshot = db.collection("missions")
            .whereEqualTo("date", today)
            .get()
            .await()

        snapshot.documents.forEach {
            it.reference.delete()
        }
    }

    suspend fun getTodayMissions(date: String): Result<List<UserMission>> = try {
        val snapshot = db.collection("users")
            .document(userId)
            .collection("missions")
            .whereEqualTo("dateAssigned", date)
            .whereEqualTo("cancelled", false)
            .get()
            .await()

        val missions = snapshot.documents.mapNotNull { doc ->
            doc.toObject(UserMission::class.java)
        }

        Result.success(missions)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun completeMission(missionId: String): Result<Unit> = try {
        db.collection("users")
            .document(userId)
            .collection("missions")
            .document(missionId)
            .update("completed", true)
            .await()

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun cancelMission(missionId: String): Result<Unit> = try {
        db.collection("users")
            .document(userId)
            .collection("missions")
            .document(missionId)
            .update("cancelled", true)
            .await()

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getQuestionnaire(): Result<Map<String, List<String>>> = try {
        val snapshot = db.collection("users")
            .document(userId)
            .collection("questionnaire")
            .document("answers")
            .get()
            .await()

        val rawAnswers = snapshot.get("answers") as? Map<*, *> ?: emptyMap<Any, Any>()

        val result = rawAnswers.mapNotNull { (key, value) ->
            val k = key as? String
            val v = (value as? List<*>)?.mapNotNull { it as? String }
            if (k != null && v != null) k to v else null
        }.toMap()

        Result.success(result)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getPendingMissionsBefore(date: String): Result<List<UserMission>> = try {
        val snapshot = db.collection("users")
            .document(userId)
            .collection("missions")
            .whereEqualTo("completed", false)
            .whereEqualTo("cancelled", false)
            .get()
            .await()

        val missions = snapshot.documents.mapNotNull { doc ->
            doc.toObject(UserMission::class.java)
        }.filter { it.dateAssigned < date }

        Result.success(missions)
    } catch (e: Exception) {
        Result.failure(e)
    }


}