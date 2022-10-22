package com.example.letsconnect.login

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.letsconnect.MainActivity
import com.example.letsconnect.R
import com.example.letsconnect.databinding.FragmentSignInBinding
import com.example.letsconnect.showSnackBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth


class SignInFragment : Fragment() {

    private lateinit var navBar: BottomNavigationView
    private lateinit var binding: FragmentSignInBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        binding = FragmentSignInBinding.inflate(inflater, container, false)



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        if(auth.currentUser!=null)
            Navigation.findNavController(requireView()).navigate(R.id.action_signInFragment_to_navigation_home)
        setListeners()
    }


    private fun setListeners() {

        binding.createNew.setOnClickListener {
            Navigation.findNavController(requireView()).navigate(R.id.action_signInFragment_to_signUpFragment)
        }
        binding.buttonSignIn.setOnClickListener {
            if (isValidSignInDetails()) {
            signIn()
            }
        }
    }



    private  fun signIn() {
        loading(true)

        auth.signInWithEmailAndPassword( binding.inputEmail.text.toString(),binding.inputPassword.text.toString())
            .addOnSuccessListener {

                startActivity(Intent(requireActivity(), MainActivity::class.java))        }
            .addOnFailureListener {
                showSnackBar("Sign In Failed")
                loading(false)
            }

    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun isValidSignInDetails(): Boolean {
        if (binding.inputEmail.text.toString().trim().isEmpty()) {
            showToast("Enter email")
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.text.toString()).matches()) {
            showToast("Enter valid mail")
            return false
        } else if (binding.inputPassword.text.toString().trim().isEmpty()) {
            showToast("Enter password")
            return false
        } else {
            return true
        }
    }

    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding.buttonSignIn.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
            binding.buttonSignIn.visibility = View.VISIBLE
        }
    }

}