package com.amarneh.workers

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.amarneh.workers.data.models.User
import com.amarneh.workers.ui.login.LoginViewModel
import com.amarneh.workers.ui.login.LoginViewModelFactory
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SplashActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        if (Firebase.auth.currentUser != null) {
            User.retrieveUserInfo(lifecycleScope) {
                startActivity(Intent(this, DashboardActivity::class.java))
                finish()
            }
        } else {
            // login as a guest
            loginViewModel.login("g@g.com", "123123")
        }

        loginViewModel.loginResult.observe(
            this,
            Observer {
                val loginResult = it ?: return@Observer
                // login.revertAnimation()
                // loading.visibility = View.GONE
                if (loginResult.error != null) {
                    showLoginFailed(loginResult.error)
                }
                if (loginResult.success != null) {
                    updateUiWithUser(loginResult.success)
                }
            }
        )
    }

    private fun updateUiWithUser(model: AuthResult) {
        User.retrieveUserInfo(lifecycleScope) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }
}