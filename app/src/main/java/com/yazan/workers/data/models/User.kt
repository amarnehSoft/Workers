package com.yazan.workers.data.models

import com.yazan.workers.ui.feed.Request
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class User(
    val id: String = "",
    val name: String = "",
    val fatherName: String = "",
    val familyName: String = "",
    val companyName: String = "",
    val email: String = "",
    val password: String = "",
    val location: String = "",
    val profession: String = "",
    val desc: String = "",
    val phone: String = "",
    val cardId: String = "",
    val type: Int = 0 // 0 -> guest, 1 -> worker, 2 -> company
    // val imageUrl: String = ""
) {

    val fullName: String get() = "$name $fatherName $familyName"

    companion object {
        const val TYPE_GUEST = 0
        const val TYPE_WORKER = 1
        const val TYPE_COMPANY = 2

        var user: User? = null
        var requests: MutableList<Request> = mutableListOf()

        fun retrieveUserInfo(scope: CoroutineScope, then: () -> Unit) {
            scope.launch {
                val userId = Firebase.auth.currentUser?.uid.orEmpty()
                user = Firebase.firestore.collection("users").document(userId).get().await()
                    .toObject(User::class.java)

                if (user?.type != TYPE_GUEST) {
                    requests =
                        Firebase.firestore.collection("requests").whereEqualTo("fromId", userId)
                            .get().await().toList().map {
                                it.toObject(Request::class.java)
                            }.toMutableList()

                    if (user!!.type == TYPE_COMPANY) {
                        val team =
                            Firebase.firestore.collection(userId).get().await().toList().map {
                                it.toObject(User::class.java)
                            }.map {
                                Request(
                                    id = UUID.randomUUID().toString(),
                                    fromId = userId,
                                    toId = it.id,
                                    status = Request.STATUS_APPROVED
                                )
                            }

                        requests.addAll(team)
                    }
                }
                then.invoke()
            }
        }
    }
}
