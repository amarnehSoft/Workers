package com.yazan.workers.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.lifecycleScope
import com.yazan.workers.data.models.User
import com.yazan.workers.databinding.ActivityEditProfileBinding
import com.yazan.workers.professions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.yazan.workers.R
import com.yazan.workers.ui.login.LoginResult
import com.yazan.workers.ui.login.string
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding

    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userId = Firebase.auth.currentUser?.uid.orEmpty()
        initUser(User.user!!)

        binding.cirRegisterButton.setOnClickListener {
            val newUser = user.copy(
                name = binding.editTextName.string(),
                fatherName = binding.editTextFamily.string(),
                familyName = binding.editTextFamily.string(),
                email = binding.editTextEmail.string(),
                phone = binding.editTextMobile.string(),
                desc = binding.editTextDescription.string(),
                location = binding.editTextLocation.string(),
                profession = binding.btnProfession.text.toString(),
                cardId = binding.editTextCardId.string(),
                companyName = binding.editTextCompany.string()
            )
            lifecycleScope.launch {

                val users =
                    Firebase.firestore.collection("users").get().await().toObjects(User::class.java)
                val cardIdError = users.filterNot {
                    it.cardId == user.cardId
                }.map {
                    it.cardId
                }.contains(newUser.cardId)

                if (cardIdError) {
                    Toast.makeText(
                        this@EditProfileActivity,
                        R.string.card_id_error,
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    binding.cirRegisterButton.startAnimation()
                    Firebase.firestore.collection("users").document(userId).set(newUser).await()
                    User.user = newUser
                    binding.cirRegisterButton.revertAnimation()
                    finish()
                }
            }
        }

        binding.btnProfession.setOnClickListener {
            val menu = PopupMenu(this, binding.btnProfession)

            professions.forEach { profession ->
                menu.menu.add(profession).setOnMenuItemClickListener {
                    binding.btnProfession.text = profession
                    true
                }
            }
            menu.show()
        }
    }

    private fun initUser(user: User) {
        this.user = user
        with(binding) {
            editTextName.setText(user.name)
            editTextFather.setText(user.fatherName)
            editTextFamily.setText(user.familyName)
            editTextCompany.setText(user.companyName)
            editTextCardId.setText(user.cardId)
            editTextEmail.setText(user.email)
            editTextMobile.setText(user.phone)
            editTextDescription.setText(user.desc)
            editTextLocation.setText(user.location)
            btnProfession.text = user.profession

            if (user.type == User.TYPE_GUEST) {
                textInputMobile.visibility = View.GONE
                textInputDescription.visibility = View.GONE
                textInputLocation.visibility = View.GONE
                btnProfession.visibility = View.GONE
            } else {
                textInputMobile.visibility = View.VISIBLE
                textInputDescription.visibility = View.VISIBLE
                textInputLocation.visibility = View.VISIBLE
                if (user.type == User.TYPE_COMPANY) {
                    btnProfession.visibility = View.VISIBLE
                } else {
                    btnProfession.visibility = View.GONE
                }
            }

            textInputEmail.visibility = View.GONE
        }
    }
}
