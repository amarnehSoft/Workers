package com.amarneh.workers.ui.login

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.amarneh.workers.DashboardActivity
import com.amarneh.workers.R
import com.amarneh.workers.data.models.User
import com.amarneh.workers.databinding.ActivityLoginBinding
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        val configuration: Configuration = resources.configuration
        configuration.setLayoutDirection(Locale("ar"))
        resources.updateConfiguration(configuration, resources.displayMetrics)

        super.onCreate(savedInstanceState)

        if (Firebase.auth.currentUser != null) {
            User.retrieveUserInfo(lifecycleScope) {
                startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                finish()
            }
        } else {
            // for changing status bar icon colors
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }

            binding = ActivityLoginBinding.inflate(layoutInflater)
            setContentView(binding.root)

            val username = binding.editTextEmail
            val password = binding.editTextPassword
            val login = binding.cirLoginButton
            val loginGuest = binding.cirGuestButton

            // val loading = binding.loading

            loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
                .get(LoginViewModel::class.java)

            loginViewModel.loginFormState.observe(
                this@LoginActivity,
                Observer {
                    val loginState = it ?: return@Observer

                    // disable login button unless both username / password is valid
                    login.isEnabled = loginState.isDataValid

                    if (loginState.usernameError != null) {
                        username.error = getString(loginState.usernameError)
                    }
                    if (loginState.passwordError != null) {
                        password.error = getString(loginState.passwordError)
                    }
                }
            )

            loginViewModel.loginResult.observe(
                this@LoginActivity,
                Observer {
                    val loginResult = it ?: return@Observer
                    login.revertAnimation()
                    // loading.visibility = View.GONE
                    if (loginResult.error != null) {
                        showLoginFailed(loginResult.error)
                    }
                    if (loginResult.success != null) {
                        updateUiWithUser(loginResult.success)
                    }
                }
            )

            username.afterTextChanged {
                loginViewModel.loginDataChanged(
                    username.text.toString(),
                    password.text.toString()
                )
            }

            password.apply {
                afterTextChanged {
                    loginViewModel.loginDataChanged(
                        username.text.toString(),
                        password.text.toString()
                    )
                }

                setOnEditorActionListener { _, actionId, _ ->
                    when (actionId) {
                        EditorInfo.IME_ACTION_DONE ->
                            loginViewModel.login(
                                username.text.toString(),
                                password.text.toString()
                            )
                    }
                    false
                }

                login.setOnClickListener {
                    // loading.visibility = View.VISIBLE
                    login.startAnimation()
                    loginViewModel.login(username.text.toString(), password.text.toString())
                }
            }

            loginGuest.setOnClickListener {
                loginGuest.startAnimation()
                loginViewModel.login("g@g.com", "123123")
            }

            binding.tvReset.setOnClickListener {
                if (loginViewModel.isEmailValid(binding.editTextEmail.text?.toString().orEmpty())) {
                    lifecycleScope.launch {
                        Firebase.auth.sendPasswordResetEmail(
                            binding.editTextEmail.text?.toString().orEmpty()
                        ).await()
                        Toast.makeText(
                            this@LoginActivity,
                            "Please check your email",
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        "Please enter a valid email",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            }
        }
    }

    fun onRegisterClick(View: View?) {
        startActivity(Intent(this, RegisterActivity::class.java))
        overridePendingTransition(R.anim.slide_in_right, R.anim.stay)
    }

    private fun updateUiWithUser(model: AuthResult) {
        User.retrieveUserInfo(lifecycleScope) {
            startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
            finish()
        }
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}
