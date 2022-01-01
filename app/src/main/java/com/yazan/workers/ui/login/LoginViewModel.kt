package com.yazan.workers.ui.login

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yazan.workers.R
import com.yazan.workers.data.LoginRepository
import com.yazan.workers.data.Result
import com.yazan.workers.data.models.User
import kotlinx.coroutines.launch

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    var userType: Int = 1

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _registerForm = MutableLiveData<RegisterFormState>()
    val registerFormState: LiveData<RegisterFormState> = _registerForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    var profession: String = ""

    fun login(username: String, password: String) {
        // can be launched in a separate asynchronous job
        viewModelScope.launch {
            val result = loginRepository.login(username, password)
            if (result is Result.Success) {
                _loginResult.value =
                    LoginResult(success = result.data)
            } else {
                _loginResult.value = LoginResult(error = R.string.login_failed)
            }
        }
    }

    fun register(
        name: String,
        fatherName: String,
        familyName: String,
        email: String,
        password: String,
        cardId: String,
        location: String,
        phone: String,
        companyName: String,
        description: String,
        profession: String
    ) {
        // can be launched in a separate asynchronous job
        viewModelScope.launch {
            val user = User(
                id = "",
                name = name,
                fatherName = fatherName,
                familyName = familyName,
                email = email,
                password = password,
                cardId = cardId,
                location = location,
                profession = profession,
                desc = description,
                companyName = companyName,
                phone = phone,
                type = userType
            )
            val result = loginRepository.register(user)
            if (result is Result.Success) {
                _loginResult.value =
                    LoginResult(success = result.data)
            } else {
                _loginResult.value = LoginResult(error = R.string.register_failed)
            }
        }
    }

    fun loginDataChanged(email: String, password: String) {
        if (!isEmailValid(email)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    fun registerDataChanged(
        email: String,
        name: String,
        password: String,
        repeatPassword: String,
        phone: String,
        profession: String,
        cardId: String
    ) {
        if (name.isEmpty()) {
            _registerForm.value = RegisterFormState(usernameError = R.string.invalid_username)
        } else if (!isEmailValid(email)) {
            _registerForm.value = RegisterFormState(emailError = R.string.invalid_email)
        } else if (!isPasswordValid(password)) {
            _registerForm.value = RegisterFormState(passwordError = R.string.invalid_password)
        } else if (repeatPassword != password) {
            _registerForm.value =
                RegisterFormState(repeatPasswordError = R.string.invalid_repeat_password)
        } else if (cardId.length != 9) {
            _registerForm.value = RegisterFormState(cardIdError = R.string.invalid_card_id)
        } else if (phone.isEmpty() && userType != User.TYPE_GUEST) {
            _registerForm.value = RegisterFormState(phoneError = R.string.invalid_phone)
        } else if (profession == "المهنة" && userType == User.TYPE_WORKER) {
            _registerForm.value = RegisterFormState(professionError = R.string.invalid_profession)
        } else {
            _registerForm.value = RegisterFormState(isDataValid = true)
        }
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return if (username.contains('@')) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }

    fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
}
