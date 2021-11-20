package com.amarneh.workers.ui.feed

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
import com.amarneh.workers.data.models.Feed
import com.amarneh.workers.data.models.User
import com.amarneh.workers.ui.FirestoreAdapter
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * RecyclerView adapter for a list of Restaurants.
 */
open class FeedsAdapter(
    query: Query?,
    private val mListener: OnItemSelectedListener,
    val scope: CoroutineScope,
    val fragment: Fragment
) : FirestoreAdapter<FeedsAdapter.ViewHolder?>(query) {
    interface OnItemSelectedListener {
        fun onItemSelected(restaurant: DocumentSnapshot?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(
            inflater.inflate(R.layout.item_feed, parent, false),
            scope,
            this,
            fragment
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getSnapshot(position), mListener)
    }

    class ViewHolder(
        itemView: View,
        val scope: CoroutineScope,
        val adapter: FeedsAdapter,
        val fragment: Fragment
    ) :
        RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView
        var nameView: TextView
        var contentView: TextView
        var btnApply: CircularProgressButton
        var btnProfile: CircularProgressButton
        var btnDelete: CircularProgressButton

        fun bind(
            snapshot: DocumentSnapshot,
            listener: OnItemSelectedListener?
        ) {
            val feed = snapshot.toObject(
                Feed::class.java
            )
            // Resources resources = itemView.getResources();

            // Load image
            scope.launch {
                try {
                    val url =
                        Firebase.storage.reference.child(feed?.userId + ".jpg").downloadUrl.await()
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

            nameView.text = feed!!.userName
            contentView.text = feed.content
            // phoneView.text = user.phone

            // Click listener
            itemView.setOnClickListener { listener?.onItemSelected(snapshot) }

            // feed?.userId.orEmpty() in User.requests
            if (User.user?.type == User.TYPE_WORKER && User.requests.none {
                it.toId == feed.userId
            }
            ) {
                btnApply.visibility = View.VISIBLE
            } else {
                btnApply.visibility = View.GONE
            }

            btnApply.setOnClickListener {
                btnApply.startAnimation()
                val id = UUID.randomUUID().toString()
                val fromId = Firebase.auth.currentUser?.uid.orEmpty()
                scope.launch {
                    val user = Firebase.firestore.collection("users").document(fromId).get().await()
                        .toObject(User::class.java)
                    val r = Request(
                        id = id,
                        fromId = fromId,
                        toId = feed.userId,
                        fromName = user?.name.orEmpty(),
                        toName = feed.userName,
                        creationDate = System.currentTimeMillis(),
                        status = Request.STATUS_PENDING,
                        updateDate = System.currentTimeMillis()
                    )

                    Firebase.firestore.collection("requests").document(id).set(r).await()
                    User.requests.add(r)
                    btnApply.visibility = View.GONE
                    adapter.notifyDataSetChanged()
                }
            }

            val currentUser = Firebase.auth.currentUser?.uid.orEmpty()
            if (currentUser == feed.userId) {
                btnProfile.visibility = View.GONE
            } else {
                btnProfile.visibility = View.VISIBLE
            }

            btnProfile.setOnClickListener {
                fragment.findNavController()
                    .navigate(R.id.nav_profile, bundleOf("userId" to feed.userId))
            }

            if (currentUser == feed.userId) {
                btnDelete.visibility = View.VISIBLE
            } else {
                btnDelete.visibility = View.GONE
            }

            btnDelete.setOnClickListener {
                scope.launch {
                    btnDelete.startAnimation()
                    Firebase.firestore.collection("feeds").document(feed.id).delete().await()
                    btnDelete.revertAnimation()
                }
            }
        }

        init {
            imageView = itemView.findViewById(R.id.iv_profile)
            nameView = itemView.findViewById(R.id.tv_name)
            contentView = itemView.findViewById(R.id.tv_content)
            btnApply = itemView.findViewById(R.id.btn_apply)
            btnProfile = itemView.findViewById(R.id.btn_profile)
            btnDelete = itemView.findViewById(R.id.btn_delete)
        }
    }
}
