package com.amarneh.workers.ui.feed

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton
import com.amarneh.workers.R
import com.amarneh.workers.data.models.Feed
import com.amarneh.workers.data.models.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class CreatePostActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val btnPost = findViewById<CircularProgressButton>(R.id.btn_post)
        btnPost.setOnClickListener {
            lifecycleScope.launch {
                btnPost.startAnimation()
                val id = UUID.randomUUID().toString()
                val userId = Firebase.auth.currentUser?.uid.orEmpty()
                val user = Firebase.firestore.collection("users").document(userId).get().await()
                    .toObject(User::class.java)
                val content = findViewById<EditText>(R.id.et_content).text.toString()
                val feed = Feed(
                    id = id,
                    userId = userId,
                    userName = user?.name.orEmpty(),
                    content = content,
                    creationDate = System.currentTimeMillis()
                )
                Firebase.firestore.collection("feeds").document(id).set(feed).await()
                btnPost.revertAnimation()
                finish()
            }
        }
    }
}
