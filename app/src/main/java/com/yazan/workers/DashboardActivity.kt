package com.yazan.workers

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.yazan.workers.data.models.User
import com.yazan.workers.databinding.ActivityDashboardBinding
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DashboardActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityDashboardBinding
    private var toolbarHidden: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarDashboard.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_dashboard)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        val user = User.user

        appBarConfiguration = AppBarConfiguration(
            when (user?.type) {
                User.TYPE_WORKER -> {
                    setOf(
                        R.id.nav_news_feed,
                        R.id.nav_companies,
                        R.id.nav_requests,
                        R.id.nav_profile,
                        R.id.nav_logout
                    )
                }
                User.TYPE_COMPANY -> {
                    setOf(
                        R.id.nav_news_feed,
                        R.id.nav_users,
                        R.id.nav_requests,
                        R.id.nav_profile,
                        R.id.nav_team,
                        R.id.nav_logout
                    )
                }
                else -> {
                    setOf(
                        R.id.nav_news_feed,
                        R.id.nav_users,
                        R.id.nav_companies,
                        R.id.nav_about
                    )
                }
            },
            drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            if (destination.id == R.id.nav_profile) {
                hideToolbar()
            } else {
                if (toolbarHidden) {
                    setSupportActionBar(binding.appBarDashboard.toolbar)
                    binding.appBarDashboard.toolbar.visibility = View.VISIBLE
                    toolbarHidden = false
                }
            }
        }

        initImage()

        when (user?.type) {
            User.TYPE_WORKER -> {
                navView.inflateMenu(R.menu.activity_main_drawer_worker)
                navController.navigate(R.id.nav_companies)
            }
            User.TYPE_COMPANY -> {
                navView.inflateMenu(R.menu.activity_main_drawer_company)
                navController.navigate(R.id.nav_users)
            }
            else -> {
                navView.inflateMenu(R.menu.activity_main_guest)
                navController.navigate(R.id.nav_about)
            }
        }

        initUser(user)
    }

    fun hideToolbar() {
        binding.appBarDashboard.toolbar.visibility = View.GONE
        toolbarHidden = true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_dashboard)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun initUser(user: User?) {
        binding.root.postDelayed({
            binding.navView.findViewById<TextView>(R.id.tv_name).text = user?.fullName
        }, 500)
    }

    fun initImage() {
        lifecycleScope.launch {
            try {
                val url =
                    Firebase.storage.reference.child(Firebase.auth.currentUser?.uid.orEmpty() + ".jpg").downloadUrl.await()
                Glide
                    .with(this@DashboardActivity)
                    .load(url)
                    .circleCrop()
                    .placeholder(R.drawable.ic_account_circle_black_48dp)
                    .error(R.drawable.ic_account_circle_black_48dp)
                    .into(binding.navView.findViewById<ImageView>(R.id.iv_profile))
            } catch (e: Exception) {
                Glide
                    .with(this@DashboardActivity)
                    .load(R.drawable.ic_account_circle_black_48dp)
                    .circleCrop()
                    .into(binding.navView.findViewById<ImageView>(R.id.iv_profile))
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {

                    // permission was granted, yay! Do the phone call
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }
        }
    }
}
