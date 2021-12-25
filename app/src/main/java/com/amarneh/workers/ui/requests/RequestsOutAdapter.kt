package com.amarneh.workers.ui.requests

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
import com.amarneh.workers.R
import com.amarneh.workers.data.models.User
import com.amarneh.workers.ui.FirestoreAdapter
import com.amarneh.workers.ui.feed.Request
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
open class RequestsOutAdapter(
    query: Query?,
    private val mListener: OnItemSelectedListener,
    val fragment: Fragment
) : FirestoreAdapter<RequestsOutAdapter.ViewHolder?>(query) {
    interface OnItemSelectedListener {
        fun onItemSelected(restaurant: DocumentSnapshot?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(
            inflater.inflate(R.layout.item_request_out, parent, false),
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
        var detailsView: TextView
        var btnCancel: CircularProgressButton
        var btnProfile: CircularProgressButton
        var tvStatus: TextView

        fun bind(
            snapshot: DocumentSnapshot,
            listener: OnItemSelectedListener?
        ) {
            val request = snapshot.toObject(
                Request::class.java
            )
            // Resources resources = itemView.getResources();
            // Load image
            fragment.lifecycleScope.launch {
                try {
                    val url =
                        Firebase.storage.reference.child(request?.toId + ".jpg").downloadUrl.await()
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

            nameView.text =
                itemView.context.getString(R.string.you_sent_invitation_to, request!!.toName)
            // itemView.context.getString(R.string.requested_to_join, request!!.fromName)

            detailsView.text = request.details

// Mohammad amarneh want to join
            // Click listener
            itemView.setOnClickListener { listener?.onItemSelected(snapshot) }

            btnCancel.setOnClickListener {
                fragment.lifecycleScope.launch {
                    btnCancel.startAnimation()
                    Firebase.firestore.collection("requests").document(request.id).delete().await()
                    User.requests = User.requests.filter {
                        it.toId != request.toId
                    }.toMutableList()
                }
            }

            btnProfile.setOnClickListener {
                fragment.findNavController()
                    .navigate(R.id.nav_profile, bundleOf("userId" to request.toId))
            }

            if (request.status != Request.STATUS_PENDING) {
                tvStatus.visibility = View.VISIBLE
                if (request.status == Request.STATUS_APPROVED) {
                    tvStatus.text = itemView.context.getString(R.string.accepted)
                } else {
                    tvStatus.text = itemView.context.getString(R.string.denied)
                }
            } else {
                tvStatus.visibility = View.GONE
            }
        }

        init {
            imageView = itemView.findViewById(R.id.iv_profile)
            nameView = itemView.findViewById(R.id.tv_content)
            detailsView = itemView.findViewById(R.id.tv_details)
            btnCancel = itemView.findViewById(R.id.btn_cancel)
            btnProfile = itemView.findViewById(R.id.btn_profile)
            tvStatus = itemView.findViewById(R.id.tv_status)
        }
    }
}
