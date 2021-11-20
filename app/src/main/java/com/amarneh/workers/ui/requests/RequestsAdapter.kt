package com.amarneh.workers.ui.requests

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Exception

/**
 * RecyclerView adapter for a list of Restaurants.
 */
open class RequestsAdapter(
    query: Query?,
    private val mListener: OnItemSelectedListener,
    val scope: CoroutineScope,
    val fragment: Fragment
) : FirestoreAdapter<RequestsAdapter.ViewHolder?>(query) {
    interface OnItemSelectedListener {
        fun onItemSelected(restaurant: DocumentSnapshot?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.item_request, parent, false), scope, fragment)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getSnapshot(position), mListener)
    }

    class ViewHolder(itemView: View, val scope: CoroutineScope, val fragment: Fragment) :
        RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView
        var nameView: TextView
        var btnAccept: CircularProgressButton
        var btnDeny: CircularProgressButton
        var btnProfile: CircularProgressButton

        fun bind(
            snapshot: DocumentSnapshot,
            listener: OnItemSelectedListener?
        ) {
            val request = snapshot.toObject(
                Request::class.java
            )
            // Resources resources = itemView.getResources();
            // Load image
            scope.launch {
                try {
                    val url =
                        Firebase.storage.reference.child(request?.fromId + ".jpg").downloadUrl.await()
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
                itemView.context.getString(R.string.requested_to_join, request!!.fromName)

// Mohammad amarneh want to join
            // Click listener
            itemView.setOnClickListener { listener?.onItemSelected(snapshot) }

            btnProfile.setOnClickListener {
                fragment.findNavController()
                    .navigate(R.id.nav_profile, bundleOf("userId" to request.fromId))
            }

            btnDeny.setOnClickListener {
                btnDeny.startAnimation()
                val newRequest = request.copy(
                    status = Request.STATUS_DENIED,
                    updateDate = System.currentTimeMillis()
                )
                scope.launch {
                    Firebase.firestore.collection("requests").document(request.id)
                        .set(newRequest).await()
                    btnDeny.revertAnimation()
                    User.requests = User.requests.filter {
                        it.id != newRequest.id
                    }.toMutableList().apply {
                        add(newRequest)
                    }
                }
            }

            btnAccept.setOnClickListener {
                btnAccept.startAnimation()
                val newRequest = request.copy(
                    status = Request.STATUS_APPROVED,
                    updateDate = System.currentTimeMillis()
                )
                scope.launch {
                    Firebase.firestore.collection("requests").document(request.id).set(newRequest)
                        .await()
                    User.requests = User.requests.filter {
                        it.id != newRequest.id
                    }.toMutableList().apply {
                        add(newRequest)
                    }

                    if (User.user?.type == User.TYPE_COMPANY) {
                        val companyId = request.toId
                        val workerId = request.fromId

                        val worker =
                            Firebase.firestore.collection("users").document(workerId).get().await()
                                .toObject(User::class.java)
                        Firebase.firestore.collection(companyId).document(workerId).set(worker!!)
                            .await()
                    } else {
                        val companyId = request.fromId
                        val workerId = request.toId

                        val worker =
                            Firebase.firestore.collection("users").document(workerId).get().await()
                                .toObject(User::class.java)
                        Firebase.firestore.collection(companyId).document(workerId).set(worker!!)
                            .await()
                    }
                    btnAccept.revertAnimation()
                }
            }
        }

        init {
            imageView = itemView.findViewById(R.id.iv_profile)
            nameView = itemView.findViewById(R.id.tv_content)
            btnAccept = itemView.findViewById(R.id.btn_accept)
            btnDeny = itemView.findViewById(R.id.btn_deny)
            btnProfile = itemView.findViewById(R.id.btn_profile)
        }
    }
}
