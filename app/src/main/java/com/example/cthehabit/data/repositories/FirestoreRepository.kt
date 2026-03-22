package com.example.cthehabit.data.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
}