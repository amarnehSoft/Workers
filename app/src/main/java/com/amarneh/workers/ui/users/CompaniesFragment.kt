package com.amarneh.workers.ui.users

import com.amarneh.workers.data.models.User

class CompaniesFragment : UsersFragment() {

    override fun userType(): Int {
        return User.TYPE_COMPANY
    }

    override fun showProfession(): Boolean {
        return false
    }
}
