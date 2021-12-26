package com.yazan.workers.ui.feed

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yazan.workers.R
import com.yazan.workers.data.models.User
import com.yazan.workers.ui.login.afterTextChanged
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FeedFragment : Fragment(R.layout.fragment_feeds), FeedsAdapter.OnItemSelectedListener {

    private var query: Query? = null
    private var adapter: FeedsAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rv = view.findViewById<RecyclerView>(R.id.rv)
        val search = view.findViewById<EditText>(R.id.et_search)

        rv.layoutManager = LinearLayoutManager(requireContext())

        query = Firebase.firestore.collection("feeds")
        // .orderBy("creationDate", Query.Direction.ASCENDING)

        adapter = object : FeedsAdapter(query, this, lifecycleScope, this) {

            override fun onDataChanged() {
                val empty = view.findViewById<View>(R.id.tv_empty)
                if (itemCount == 0) {
                    rv.visibility = View.GONE
                    empty.visibility = View.VISIBLE
                } else {
                    rv.visibility = View.VISIBLE
                    empty.visibility = View.GONE
                }
            }

            override fun onError(e: FirebaseFirestoreException?) {
                // Show a snackbar on errors
                Snackbar.make(
                    view,
                    "Error: check logs for info.", Snackbar.LENGTH_LONG
                ).show()
            }
        }

        rv.adapter = adapter

        search.afterTextChanged {
            query = Firebase.firestore.collection("feeds")
                // .orderBy("creationDate", Query.Direction.ASCENDING)
                .whereGreaterThanOrEqualTo("userName", it.lowercase())
                .whereLessThanOrEqualTo("userName", it.lowercase() + '\uf8ff')
            adapter?.setQuery(query)
        }

        val btnPost = view.findViewById<View>(R.id.btn_post)
        if (User.user?.type == User.TYPE_COMPANY) {
            btnPost.visibility = View.VISIBLE
        } else {
            btnPost.visibility = View.GONE
        }
        btnPost.setOnClickListener {
            startActivity(Intent(context, CreatePostActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        adapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
    }

    override fun onItemSelected(user: DocumentSnapshot?) {
    }
}
