package com.yazan.workers.ui.users

import com.yazan.workers.data.models.User

class CompaniesFragment : UsersFragment() {

    override fun userType(): Int {
        return User.TYPE_COMPANY
    }

    override fun showProfession(): Boolean {
        return false
    }
}
