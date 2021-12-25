package com.amarneh.workers.ui.login

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.amarneh.workers.DashboardActivity
import com.amarneh.workers.R
import com.amarneh.workers.data.models.User
import com.amarneh.workers.databinding.ActivityRegisterBinding
import com.amarneh.workers.professions
import com.google.firebase.auth.AuthResult

class RegisterActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        changeStatusBarColor()

        val email = binding.editTextEmail
        val name = binding.editTextName
        val password = binding.editTextPassword
        val register = binding.cirRegisterButton

        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        loginViewModel.registerFormState.observe(
            this@RegisterActivity,
            Observer {
                val loginState = it ?: return@Observer

                // disable login button unless both username / password is valid
                register.isEnabled = loginState.isDataValid

                if (loginState.usernameError != null) {
                    name.error = getString(loginState.usernameError)
                }
                if (loginState.passwordError != null) {
                    password.error = getString(loginState.passwordError)
                }
                if (loginState.repeatPasswordError != null) {
                    binding.editTextRepeatPassword.error = getString(loginState.repeatPasswordError)
                }
                if (loginState.emailError != null) {
                    email.error = getString(loginState.emailError)
                }
                if (loginState.cardIdError != null) {
                    binding.editTextCardId.error = getString(loginState.cardIdError)
                }
                if (loginState.phoneError != null) {
                    binding.editTextMobile.error = getString(loginState.phoneError)
                }
                if (loginState.locationError != null) {
                    binding.editTextLocation.error = getString(loginState.locationError)
                }
                if (loginState.professionError != null) {
                    binding.btnProfession.error = getString(loginState.professionError)
                }
                if (loginState.descError != null) {
                    binding.editTextDescription.error = getString(loginState.descError)
                }
            }
        )

        loginViewModel.loginResult.observe(
            this@RegisterActivity,
            Observer {
                val loginResult = it ?: return@Observer

                register.revertAnimation()
                // loading.visibility = View.GONE
                if (loginResult.error != null) {
                    showLoginFailed(loginResult.error)
                }
                if (loginResult.success != null) {
                    updateUiWithUser(loginResult.success)
                }
            }
        )

        name.addChangeListener()
        email.addChangeListener()
        password.addChangeListener()
        binding.editTextRepeatPassword.addChangeListener()
        binding.editTextMobile.addChangeListener()
        binding.editTextLocation.addChangeListener()
        binding.editTextDescription.addChangeListener()

        binding.btnProfession.setOnClickListener {

            val menu = PopupMenu(this, binding.btnProfession)

            professions.map {
                it.first
            }.forEach { profession ->
                menu.menu.add(profession).setOnMenuItemClickListener {
                    binding.btnProfession.text = profession

                    loginViewModel.registerDataChanged(
                        email = binding.editTextEmail.string(),
                        name = binding.editTextName.string(),
                        password = binding.editTextPassword.string(),
                        repeatPassword = binding.editTextRepeatPassword.string(),
                        phone = binding.editTextMobile.string(),
                        profession = binding.btnProfession.text.toString(),
                        cardId = binding.editTextCardId.string()
                    )

                    binding.btnCategory.text = "شامل"
                    true
                }
            }
            menu.show()
        }

        binding.btnCategory.setOnClickListener {

            val menu = PopupMenu(this, binding.btnCategory)

            professions.find {
                it.first == binding.btnProfession.text.toString()
            }?.second?.forEach { category ->
                menu.menu.add(category).setOnMenuItemClickListener {
                    binding.btnCategory.text = category

//                    loginViewModel.registerDataChanged(
//                        email = binding.editTextEmail.string(),
//                        name = binding.editTextName.string(),
//                        password = binding.editTextPassword.string(),
//                        repeatPassword = binding.editTextRepeatPassword.string(),
//                        phone = binding.editTextMobile.string(),
//                        profession = binding.btnProfession.text.toString(),
//                        cardId = binding.editTextCardId.string()
//                    )

                    true
                }
            }
            menu.show()
        }

        register.setOnClickListener {
            // loading.visibility = View.VISIBLE
            register.startAnimation()
            loginViewModel.register(
                name = name.string(),
                fatherName = binding.editTextFather.string(),
                familyName = binding.editTextFamily.string(),
                email = email.string(),
                password = password.string(),
                cardId = binding.editTextCardId.string(),
                location = binding.editTextLocation.string(),
                phone = binding.editTextMobile.string(),
                description = binding.editTextDescription.string(),
                profession = binding.btnProfession.text.toString(),
                category = binding.btnCategory.text.toString(),
                companyName = binding.editTextCompany.string()
            )
        }

        updateTabsUI()
        binding.worker.setOnClickListener {
            loginViewModel.userType = User.TYPE_WORKER
            updateTabsUI()
        }

        binding.company.setOnClickListener {
            loginViewModel.userType = User.TYPE_COMPANY
            updateTabsUI()
        }

//        binding.tvGuest.setOnClickListener {
//            loginViewModel.userType = User.TYPE_GUEST
//            updateTabsUI()
//        }
    }

    private fun updateTabsUI() {
        when (loginViewModel.userType) {
            User.TYPE_WORKER -> {
                binding.worker.background =
                    ContextCompat.getDrawable(this, R.drawable.bg_manager_mode_toggle_right)
                binding.worker.setTypeface(null, Typeface.BOLD)
                binding.company.background = null
                binding.company.setTypeface(null, Typeface.NORMAL)
//                binding.tvGuest.background = null
//                binding.tvGuest.setTypeface(null, Typeface.NORMAL)

                binding.textInputMobile.visibility = View.VISIBLE
                binding.textInputLocation.visibility = View.VISIBLE
                binding.btnProfession.visibility = View.VISIBLE
                binding.btnCategory.visibility = View.VISIBLE
                binding.textInputDescription.visibility = View.VISIBLE
            }
            User.TYPE_COMPANY -> {
                binding.worker.background = null
                binding.worker.setTypeface(null, Typeface.NORMAL)
                binding.company.background =
                    ContextCompat.getDrawable(this, R.drawable.bg_manager_mode_toggle_left)
                binding.company.setTypeface(null, Typeface.BOLD)
//                binding.tvGuest.background = null
//                binding.tvGuest.setTypeface(null, Typeface.NORMAL)

                binding.textInputMobile.visibility = View.VISIBLE
                binding.textInputLocation.visibility = View.VISIBLE
                binding.btnProfession.visibility = View.GONE
                binding.btnCategory.visibility = View.GONE
                binding.textInputDescription.visibility = View.VISIBLE
            }
            else -> {
                binding.worker.background = null
                binding.worker.setTypeface(null, Typeface.NORMAL)
                binding.company.background = null
                binding.company.setTypeface(null, Typeface.NORMAL)
//                binding.tvGuest.background =
//                    ContextCompat.getDrawable(this, R.drawable.bg_manager_mode_toggle_right)
//                binding.tvGuest.setTypeface(null, Typeface.BOLD)

                binding.textInputMobile.visibility = View.GONE
                binding.textInputLocation.visibility = View.GONE
                binding.btnProfession.visibility = View.GONE
                binding.btnCategory.visibility = View.GONE
                binding.textInputDescription.visibility = View.GONE
            }
        }
    }

    private fun changeStatusBarColor() {
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        //            window.setStatusBarColor(Color.TRANSPARENT);
        window.statusBarColor = resources.getColor(R.color.register_bk_color)
    }

    fun onLoginClick(View: View?) {
        startActivity(Intent(this, LoginActivity::class.java))
        overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right)
    }

    private fun updateUiWithUser(model: AuthResult) {
        User.retrieveUserInfo(lifecycleScope) {
            startActivity(Intent(this@RegisterActivity, DashboardActivity::class.java))
            finish()
        }
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }

    private fun EditText.addChangeListener() {
        this.afterTextChanged {
            loginViewModel.registerDataChanged(
                email = binding.editTextEmail.string(),
                name = binding.editTextName.string(),
                password = binding.editTextPassword.string(),
                repeatPassword = binding.editTextRepeatPassword.string(),
                phone = binding.editTextMobile.string(),
                profession = binding.btnProfession.text.toString(),
                cardId = binding.editTextCardId.string()
            )
        }
    }
}

fun EditText.string(): String {
    return this.text?.toString().orEmpty()
}
