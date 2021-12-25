package com.amarneh.workers.ui.users

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
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
import java.util.UUID

/**
 * RecyclerView adapter for a list of Restaurants.
 */
open class UsersAdapter(
    query: Query?,
    private val mListener: OnItemSelectedListener,
    val scope: CoroutineScope,
    val fragment: Fragment
) : FirestoreAdapter<UsersAdapter.ViewHolder?>(query) {
    interface OnItemSelectedListener {
        fun onItemSelected(restaurant: DocumentSnapshot?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(
            inflater.inflate(R.layout.item_worker, parent, false),
            scope,
            fragment,
            this
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getSnapshot(position), mListener)
    }

    class ViewHolder(
        itemView: View,
        val scope: CoroutineScope,
        val fragment: Fragment,
        val adapter: UsersAdapter
    ) :
        RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView
        var nameView: TextView
        var professionView: TextView
        var locationView: TextView
        var btnRequest: CircularProgressButton
        var btnProfile: CircularProgressButton
        var ivCall: AppCompatImageView

        fun bind(
            snapshot: DocumentSnapshot,
            listener: OnItemSelectedListener?
        ) {
            val user = snapshot.toObject(
                User::class.java
            )
            // Resources resources = itemView.getResources();

            // Load image
            scope.launch {
                try {
                    val url =
                        Firebase.storage.reference.child(user?.id.orEmpty() + ".jpg").downloadUrl.await()
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

            nameView.text = user!!.fullName
            professionView.text = user.profession
            locationView.text = user.location

            // Click listener
            itemView.setOnClickListener { listener?.onItemSelected(snapshot) }

            if (user.type == User.TYPE_COMPANY) {
                professionView.visibility = View.GONE
            } else {
                professionView.visibility = View.VISIBLE
            }

            if (User.requests.none { it.toId == user.id } && User.user?.type != User.TYPE_GUEST) {
                btnRequest.visibility = View.VISIBLE
            } else {
                btnRequest.visibility = View.GONE
            }

            btnRequest.setOnClickListener {
                showDialog(btnRequest.context, user)
            }

            btnProfile.setOnClickListener {
                fragment.findNavController()
                    .navigate(R.id.nav_profile, bundleOf("userId" to user.id))
            }

            ivCall.setOnClickListener {
//                val intent = Intent(Intent.ACTION_CALL)
//
//                intent.data = Uri.parse("tel:" + user.phone)
//                it.context.startActivity(intent)

                val number = "tel:" + user.phone
                val mIntent = Intent(Intent.ACTION_CALL)
                mIntent.setData(Uri.parse(number))
                if (ContextCompat.checkSelfPermission(
                        fragment.requireActivity(),
                        Manifest.permission.CALL_PHONE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        fragment.requireActivity(), arrayOf(Manifest.permission.CALL_PHONE), 1
                    )

                    // MY_PERMISSIONS_REQUEST_CALL_PHONE is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                } else {
                    // You already have permission
                    try {
                        fragment.requireActivity().startActivity(mIntent)
                    } catch (e: SecurityException) {
                        e.printStackTrace()
                    }
                }
            }
        }

        init {
            imageView = itemView.findViewById(R.id.iv_profile)
            nameView = itemView.findViewById(R.id.tv_name)
            professionView = itemView.findViewById(R.id.tv_profession)
            locationView = itemView.findViewById(R.id.tv_location)
            btnRequest = itemView.findViewById(R.id.btn_request)
            btnProfile = itemView.findViewById(R.id.btn_profile)
            ivCall = itemView.findViewById(R.id.iv_call)
        }

        fun showDialog(context: Context, user: User) {
            val alert: AlertDialog.Builder = AlertDialog.Builder(context)
            val edittext = EditText(context)
            alert.setMessage(context.getString(R.string.details))
            alert.setTitle(context.getString(R.string.details))

            alert.setView(edittext)

            alert.setPositiveButton(
                context.getString(R.string.request)
            ) { dialog, whichButton -> // What ever you want to do with the value
                val details = edittext.text

                btnRequest.startAnimation()
                val id = UUID.randomUUID().toString()
                // val fromId = Firebase.auth.currentUser?.uid.orEmpty()
                val fromUser = User.user!!
                scope.launch {
                    val r = Request(
                        id = id,
                        fromId = fromUser.id,
                        toId = user.id,
                        fromName = fromUser.fullName,
                        toName = user.fullName,
                        creationDate = System.currentTimeMillis(),
                        status = Request.STATUS_PENDING,
                        updateDate = System.currentTimeMillis(),
                        details = details.toString()
                    )

                    Firebase.firestore.collection("requests").document(id).set(r).await()
                    User.requests.add(r)
                    btnRequest.visibility = View.GONE
                    adapter.notifyDataSetChanged()
                }
            }

            alert.setNegativeButton(
                context.getString(R.string.cancel)
            ) { dialog, whichButton ->
                dialog.dismiss()
            }

            alert.show()
        }
    }
}
