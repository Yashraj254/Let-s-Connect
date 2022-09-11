package com.example.letsconnect

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.letsconnect.databinding.ActivityMainBinding
import com.example.letsconnect.ui.addPost.NewPostFragment
import com.example.letsconnect.ui.home.HomeFragment
import com.example.letsconnect.ui.notifications.NotificationsFragment
import com.example.letsconnect.ui.profile.ProfileFragment
import com.example.letsconnect.ui.search.SearchFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


//        binding.navView.setOnItemSelectedListener {
//            when (it.itemId) {
//                R.id.navigation_home -> setFragment(HomeFragment())
//                R.id.navigation_search -> setFragment(SearchFragment())
//                R.id.navigation_post -> setFragment(NewPostFragment())
//                R.id.navigation_notifications -> setFragment(NotificationsFragment())
//                R.id.navigation_profile -> setFragment(ProfileFragment())
//            }
//            return@setOnItemSelectedListener true
//        }

val navController = findNavController(R.id.container_fragment)
        NavigationUI.setupWithNavController(binding.navView,navController)

    }

    private fun setFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container_fragment, fragment)
        transaction.disallowAddToBackStack()
        transaction
            .commit()

    }
}