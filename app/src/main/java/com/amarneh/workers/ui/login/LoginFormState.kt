package com.amarneh.workers.ui.login

/**
 * Data validation state of the login form.
 */
data class LoginFormState(
    val usernameError: Int? = null,
    val passwordError: Int? = null,
    val isDataValid: Boolean = false
)

data class RegisterFormState(
    val usernameError: Int? = null,
    val passwordError: Int? = null,
    val repeatPasswordError: Int? = null,
    val emailError: Int? = null,
    val phoneError: Int? = null,
    val locationError: Int? = null,
    val professionError: Int? = null,
    val descError: Int? = null,
    val cardIdError: Int? = null,

    val isDataValid: Boolean = false
)
