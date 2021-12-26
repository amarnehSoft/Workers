package com.yazan.workers.data

import com.yazan.workers.data.models.User
import com.google.firebase.auth.AuthResult

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class LoginRepository(val dataSource: LoginDataSource) {

    // in-memory cache of the loggedInUser object
    var auth: AuthResult? = null
        private set

    val isLoggedIn: Boolean
        get() = auth != null

    init {
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
        auth = null
    }

    fun logout() {
        auth = null
        dataSource.logout()
    }

    suspend fun login(email: String, password: String): Result<AuthResult> {
        // handle login
        val result = dataSource.login(email, password)
        if (result is Result.Success) {
            setLoggedInUser(result.data)
        }
        return result
    }

    suspend fun register(user: User): Result<AuthResult> {
        val result = dataSource.signup(user)
        if (result is Result.Success) {
            setLoggedInUser(result.data)
        }
        return result
    }

    private fun setLoggedInUser(loggedInUser: AuthResult) {
        this.auth = loggedInUser
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }
}
