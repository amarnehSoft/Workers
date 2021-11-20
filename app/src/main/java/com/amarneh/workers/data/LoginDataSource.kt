package com.amarneh.workers.data

import com.amarneh.workers.data.models.User
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    suspend fun signup(user: User): Result<AuthResult> {
        return try {
            val authResult =
                Firebase.auth.createUserWithEmailAndPassword(user.email, user.password).await()
            Firebase.firestore.collection("users").document(authResult.user?.uid.orEmpty())
                .set(user.copy(id = authResult.user?.uid.orEmpty())).await()
            Result.Success(authResult)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(e)
        }
    }

    suspend fun login(email: String, password: String): Result<AuthResult> {
        return try {
            Result.Success(
                Firebase.auth.signInWithEmailAndPassword(email, password).await()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(e)
        }
    }

    fun logout() {
    }
}
