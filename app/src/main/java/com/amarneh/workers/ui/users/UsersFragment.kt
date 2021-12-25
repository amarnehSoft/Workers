package com.amarneh.workers.ui.users

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amarneh.workers.R
import com.amarneh.workers.data.models.User
import com.amarneh.workers.professions
import com.amarneh.workers.ui.login.afterTextChanged
import com.amarneh.workers.ui.login.string
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

open class UsersFragment : Fragment(R.layout.fragment_users), UsersAdapter.OnItemSelectedListener {

    private var query: Query? = null
    private var adapter: UsersAdapter? = null

    open fun userType(): Int = User.TYPE_WORKER
    open fun showProfession(): Boolean = true

    private val selectedProfessions: MutableList<String> = mutableListOf()
    lateinit var profession: Button
    lateinit var search: EditText

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rv = view.findViewById<RecyclerView>(R.id.rv)
        search = view.findViewById(R.id.et_search)
        profession = view.findViewById(R.id.btnProfession)

        profession.visibility = if (showProfession()) View.VISIBLE else View.GONE

        profession.setOnClickListener {
            showAlertDialog()
        }

        rv.layoutManager = LinearLayoutManager(requireContext())

//        val userType = User.user!!.type

        query = Firebase.firestore.collection("users")
            .whereEqualTo("type", userType())
            .orderBy("name", Query.Direction.DESCENDING)

        adapter = object : UsersAdapter(query, this, lifecycleScope, this) {

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
            query = Firebase.firestore.collection("users")
                .whereEqualTo("type", userType())
                .whereGreaterThanOrEqualTo("name", it.lowercase())
                .whereLessThanOrEqualTo("name", it.lowercase() + '\uf8ff')

            if (profession.visibility == View.VISIBLE && selectedProfessions.isNotEmpty()) {
                query = query!!.whereIn("profession", selectedProfessions)
            }
            query = query!!.orderBy("name", Query.Direction.DESCENDING)
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

    private fun showAlertDialog() {
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        alertDialog.setTitle(getString(R.string.profession))
        val items = professions.map { it.first }.toTypedArray()
        val checkedItems = items.map {
            selectedProfessions.contains(it)
        }.toBooleanArray()
        // booleanArrayOf(false, false, false, false, false, false)
        alertDialog.setMultiChoiceItems(
            items,
            checkedItems
        ) { dialog, which, isChecked ->
            val p = items[which]
            if (isChecked) {
                selectedProfessions.add(p)
            } else {
                selectedProfessions.remove(p)
            }
            dialog.dismiss()

            query = Firebase.firestore.collection("users")
                .whereEqualTo("type", userType())
                .whereGreaterThanOrEqualTo("name", search.string().lowercase())
                .whereLessThanOrEqualTo("name", search.string().lowercase() + '\uf8ff')

            if (profession.visibility == View.VISIBLE && selectedProfessions.isNotEmpty()) {
                query = query!!.whereIn("profession", selectedProfessions)
            }
            query = query!!.orderBy("name", Query.Direction.DESCENDING)
            adapter?.setQuery(query)
        }
        val alert: AlertDialog = alertDialog.create()
        alert.show()
    }
}
