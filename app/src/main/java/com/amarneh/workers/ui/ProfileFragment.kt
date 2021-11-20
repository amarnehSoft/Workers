package com.amarneh.workers.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.amarneh.workers.DashboardActivity
import com.amarneh.workers.R
import com.amarneh.workers.data.models.User
import com.amarneh.workers.databinding.ActivityProfileBinding
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileFragment : Fragment() {

    private lateinit var binding: ActivityProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ActivityProfileBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar: Toolbar = view.findViewById<View>(R.id.toolbar) as Toolbar
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)

        val userId = arguments?.getString("userId") ?: Firebase.auth.currentUser?.uid.orEmpty()

        binding.tvBack.setOnClickListener {
            findNavController().popBackStack()
        }

        if (userId == Firebase.auth.currentUser?.uid.orEmpty()) {
            binding.fab.visibility = View.VISIBLE
            binding.ivProfile.setOnClickListener {
                chooseImage()
            }
            binding.fab.setOnClickListener {
                startActivity(Intent(context, EditProfileActivity::class.java))
            }
        } else {
            binding.fab.visibility = View.GONE
        }

        initImage(userId)
    }

    override fun onResume() {
        super.onResume()
        val userId = arguments?.getString("userId") ?: Firebase.auth.currentUser?.uid.orEmpty()
        lifecycleScope.launch {
            Firebase.firestore.collection("users").document(userId).get().await()
                .toObject(User::class.java)?.let {
                    initUser(it)
                }
        }
    }

    private fun initUser(user: User) {
        binding.tvName.text = user.name
        binding.tvProfession.text = user.profession

        with(binding.layout) {
            tvEmail.text = user.email
            tvMobile.text = user.phone
            tvDesc.text = user.desc
            tvLocation.text = user.location

            if (user.type == User.TYPE_GUEST) {
                mobileLayout.visibility = View.GONE
                mobileLine.visibility = View.GONE
                descriptionLayout.visibility = View.GONE
                descriptionLine.visibility = View.GONE
                addressLayout.visibility = View.GONE
                addressLine.visibility = View.GONE
            } else {
                mobileLayout.visibility = View.VISIBLE
                mobileLine.visibility = View.VISIBLE
                descriptionLayout.visibility = View.VISIBLE
                descriptionLine.visibility = View.VISIBLE
                addressLayout.visibility = View.VISIBLE
                addressLine.visibility = View.VISIBLE
            }
        }

        if (user.type == User.TYPE_COMPANY) {
            binding.tvProfession.visibility = View.VISIBLE
        } else {
            binding.tvProfession.visibility = View.GONE
        }
    }

    private fun chooseImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            data?.data?.let {
                lifecycleScope.launch {
                    val userId = Firebase.auth.currentUser?.uid.orEmpty()
                    Firebase.storage.reference.child("$userId.jpg")
                        .putFile(it).await()

                    // val url =
                    //     Firebase.storage.reference.child("$userId.jpg").downloadUrl.await().path

                    // val user = Firebase.firestore.collection("users").document(userId).get().await().toObject(User::class.java)

                    // Firebase.firestore.collection("users").document(userId)
                    //     .update("imageUrl", url)
                    //     // .set(user!!.copy(imageUrl = url))
                    //     .await()

                    initImage(userId)
                    (activity as DashboardActivity).initImage()
                }
            }
        }
    }

    private fun initImage(userId: String) {
        lifecycleScope.launch {
            try {
                val url =
                    Firebase.storage.reference.child("$userId.jpg").downloadUrl.await()
                Glide
                    .with(this@ProfileFragment)
                    .load(url)
                    .circleCrop()
                    .placeholder(R.drawable.ic_account_circle_black_48dp)
                    .error(R.drawable.ic_account_circle_black_48dp)
                    .into(binding.ivProfile)
            } catch (e: Exception) {
                Glide
                    .with(this@ProfileFragment)
                    .load(R.drawable.ic_account_circle_black_48dp)
                    .circleCrop()
                    .into(binding.ivProfile)
            }
        }
    }
}
