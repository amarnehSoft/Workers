package com.yazan.workers.ui.team

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton
import com.yazan.workers.R
import com.yazan.workers.data.models.User
import com.yazan.workers.ui.FirestoreAdapter
import com.bumptech.glide.Glide
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * RecyclerView adapter for a list of Restaurants.
 */
open class TeamAdapter(
    query: Query?,
    private val mListener: OnItemSelectedListener,
    val fragment: Fragment
) : FirestoreAdapter<TeamAdapter.ViewHolder?>(query) {
    interface OnItemSelectedListener {
        fun onItemSelected(restaurant: DocumentSnapshot?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(
            inflater.inflate(R.layout.item_team, parent, false),
            fragment
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getSnapshot(position), mListener)
    }

    class ViewHolder(itemView: View, val fragment: Fragment) :
        RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView
        var nameView: TextView
        var btnCancel: CircularProgressButton
        var btnProfile: CircularProgressButton

        fun bind(
            snapshot: DocumentSnapshot,
            listener: OnItemSelectedListener?
        ) {
            val worker = snapshot.toObject(
                User::class.java
            )
            // Resources resources = itemView.getResources();
            // Load image
            fragment.lifecycleScope.launch {
                try {
                    val url =
                        Firebase.storage.reference.child(worker?.id + ".jpg").downloadUrl.await()
                    Glide
                        .with(itemView)
                        .load(url)
                        .circleCrop()
                        .placeholder(R.drawable.ic_account_circle_black_48dp)
                        .error(R.drawable.ic_account_circle_black_48dp)
                        .into(imageView)
                } catch (e: Exception) {
                    Glide.with(itemView)
                        .load(R.drawable.ic_account_circle_black_48dp)
                        .circleCrop()
                        .into(imageView)
                }
            }

            nameView.text = worker?.fullName
            // itemView.context.getString(R.string.requested_to_join, request!!.fromName)

// Mohammad amarneh want to join
            // Click listener
            itemView.setOnClickListener { listener?.onItemSelected(snapshot) }

            btnCancel.setOnClickListener {
                fragment.lifecycleScope.launch {
                    btnCancel.startAnimation()
                    val companyId = User.user!!.id
                    Firebase.firestore.collection(companyId).document(worker!!.id).delete().await()
                    User.requests = User.requests.filter {
                        it.toId != worker.id
                    }.toMutableList()
                }
            }

            btnProfile.setOnClickListener {
                fragment.findNavController()
                    .navigate(R.id.nav_profile, bundleOf("userId" to worker!!.id))
            }
        }

        init {
            imageView = itemView.findViewById(R.id.iv_profile)
            nameView = itemView.findViewById(R.id.tv_name)
            btnCancel = itemView.findViewById(R.id.btn_delete)
            btnProfile = itemView.findViewById(R.id.btn_profile)
        }
    }
}
