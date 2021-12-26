package com.yazan.workers.ui.requests

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yazan.workers.R
import com.yazan.workers.ui.feed.Request
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RequestsFragment :
    Fragment(R.layout.fragment_requests),
    RequestsAdapter.OnItemSelectedListener,
    RequestsOutAdapter.OnItemSelectedListener {

    private var queryIncoming: Query? = null
    private var adapterIncoming: RequestsAdapter? = null

    private var queryOut: Query? = null
    private var adapterOut: RequestsOutAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rvIncoming = view.findViewById<RecyclerView>(R.id.rv_incoming)
        val rvOut = view.findViewById<RecyclerView>(R.id.rv_outgoing)

        rvIncoming.layoutManager = LinearLayoutManager(requireContext())
        rvOut.layoutManager = LinearLayoutManager(requireContext())

        val userId = Firebase.auth.currentUser?.uid.orEmpty()

        queryIncoming = Firebase.firestore.collection("requests")
            .whereEqualTo("toId", userId)
            .whereEqualTo("status", Request.STATUS_PENDING)
        // .orderBy("name", Query.Direction.DESCENDING)

        queryOut = Firebase.firestore.collection("requests")
            .whereEqualTo("fromId", userId)

        adapterIncoming = object : RequestsAdapter(queryIncoming, this, lifecycleScope, this) {

            override fun onDataChanged() {
                val empty = view.findViewById<View>(R.id.tv_empty1)
                if (itemCount == 0) {
                    rvIncoming.visibility = View.GONE
                    empty.visibility = View.VISIBLE
                } else {
                    rvIncoming.visibility = View.VISIBLE
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

        adapterOut = object : RequestsOutAdapter(queryOut, this, this) {

            override fun onDataChanged() {
                val empty = view.findViewById<View>(R.id.tv_empty2)
                if (itemCount == 0) {
                    rvOut.visibility = View.GONE
                    empty.visibility = View.VISIBLE
                } else {
                    rvOut.visibility = View.VISIBLE
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

        rvIncoming.adapter = adapterIncoming
        rvOut.adapter = adapterOut
    }

    override fun onStart() {
        super.onStart()
        adapterIncoming?.startListening()
        adapterOut?.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapterIncoming?.stopListening()
        adapterOut?.stopListening()
    }

    override fun onItemSelected(user: DocumentSnapshot?) {
    }
}
