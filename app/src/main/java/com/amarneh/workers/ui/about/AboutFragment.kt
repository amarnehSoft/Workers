package com.amarneh.workers.ui.about

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.amarneh.workers.databinding.FragmentAboutBinding
import com.amarneh.workers.databinding.FragmentGalleryBinding
import com.amarneh.workers.ui.login.LoginActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AboutFragment : Fragment() {

    // private lateinit var logoutViewModel: LogoutViewModel
    private var _binding: FragmentAboutBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        logoutViewModel =
//            ViewModelProvider(this).get(LogoutViewModel::class.java)

        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // val textView: TextView = binding.textGallery
        // galleryViewModel.text.observe(viewLifecycleOwner, Observer {
        //     textView.text = it
        // })

        binding.btnLogin.setOnClickListener {
//            Firebase.auth.signOut()
//            activity?.finish()
            startActivity(Intent(context, LoginActivity::class.java))
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
