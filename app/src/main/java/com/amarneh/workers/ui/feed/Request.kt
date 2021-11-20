package com.amarneh.workers.ui.feed

data class Request(
    val id: String = "",
    val fromId: String = "",
    val toId: String = "",
    val fromName: String = "",
    val toName: String = "",
    val creationDate: Long = 0,
    val status: Int = 0, // 0 pending, 1 approved, 2 denied
    val updateDate: Long = 0
) {
    companion object {
        const val STATUS_PENDING = 0
        const val STATUS_APPROVED = 1
        const val STATUS_DENIED = 2
    }
}
