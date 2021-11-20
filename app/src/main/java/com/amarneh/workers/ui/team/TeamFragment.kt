package com.amarneh.workers.ui.team

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amarneh.workers.R
import com.amarneh.workers.data.models.User
import com.amarneh.workers.ui.login.afterTextChanged
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

open class TeamFragment : Fragment(R.layout.fragment_users), TeamAdapter.OnItemSelectedListener {

    private var query: Query? = null
    private var adapter: TeamAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rv = view.findViewById<RecyclerView>(R.id.rv)
        val search = view.findViewById<EditText>(R.id.et_search)
        val profession = view.findViewById<Button>(R.id.btnProfession)
        profession.visibility = View.GONE

        rv.layoutManager = LinearLayoutManager(requireContext())

        val companyId = User.user!!.id

        query = Firebase.firestore.collection(companyId)

        adapter = object : TeamAdapter(query, this, this) {

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
            query = Firebase.firestore.collection(companyId)
                .whereGreaterThanOrEqualTo("name", it.lowercase())
                .whereLessThanOrEqualTo("name", it.lowercase() + '\uf8ff')
            adapter?.setQuery(query)
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
