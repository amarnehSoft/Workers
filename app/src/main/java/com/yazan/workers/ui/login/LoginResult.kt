package com.yazan.workers.ui.login

import com.google.firebase.auth.AuthResult

/**
 * Authentication result : success (user details) or error message.
 */
data class LoginResult(
    val success: AuthResult? = null,
    val error: Int? = null
)