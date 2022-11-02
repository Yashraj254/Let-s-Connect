package com.example.letsconnect.login

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.example.letsconnect.MainActivity
import com.example.letsconnect.R
import com.example.letsconnect.Response
import com.example.letsconnect.databinding.FragmentSignInBinding
import com.example.letsconnect.showSnackBar
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SignInFragment : Fragment() {

    private lateinit var navBar: BottomNavigationView
    private lateinit var binding: FragmentSignInBinding
    private val auth = FirebaseAuth.getInstance()
    private lateinit var googleSignInClient: GoogleSignInClient

    private val viewModel:LoginViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        binding = FragmentSignInBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (auth.currentUser != null)
            Navigation.findNavController(requireView())
                .navigate(R.id.action_signInFragment_to_navigation_home)
        setListeners()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)


        binding.googleSignIn.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            launcher.launch(signInIntent)
        }

    }

    private val launcher =      registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = getSignedInAccountFromIntent(result.data)
            try {
                val googleSignInAccount = task.getResult(ApiException::class.java)
                googleSignInAccount?.apply {
                    idToken?.let { idToken ->
                        signInWithGoogle(idToken)
                    }
                }
            } catch (e: ApiException) {
                showSnackBar(e.message.toString())
            }
        }
    }

    private fun signInWithGoogle(idToken: String) {
        viewModel.signInWithGoogle(idToken).observe(this) { response ->
            when (response) {
                is Response.Loading -> {
                    showSnackBar("Loading")
                }
                is Response.Success<*> -> {
                    val isNewUser = response.data
                    if (isNewUser as Boolean) {
                        viewModel.deleteAccount(idToken).observe(this){
                            when(it){
                                is Response.Loading -> { }
                                is Response.Success -> {
                                    showSnackBar("New account")
                                }
                                is Response.Failure -> {
                                    showSnackBar("Error occured")
                                }
                            }
                        }

                        Navigation.findNavController(requireView())
                            .navigate(R.id.action_signInFragment_to_signUpFragment)

                    } else {
                     showSnackBar("old user")
                        Navigation.findNavController(requireView())
                            .navigate(R.id.action_signInFragment_to_navigation_home)
                    }
                }
                is Response.Failure -> {
                    showSnackBar(response.errorMessage)

                }
            }
        }
    }


    private fun setListeners() {

        binding.createNew.setOnClickListener {
            Navigation.findNavController(requireView())
                .navigate(R.id.action_signInFragment_to_signUpFragment)
        }
        binding.buttonSignIn.setOnClickListener {
            if (isValidSignInDetails()) {
                signIn()
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        navBar = requireActivity().findViewById(R.id.nav_view)
        val actionBar = requireActivity().findViewById<MaterialToolbar>(R.id.materialToolbar)
        actionBar.title = "Sign In"

        navBar.visibility = View.GONE
    }


    private fun signIn() {
        loading(true)

        auth.signInWithEmailAndPassword(binding.inputEmail.text.toString(),
            binding.inputPassword.text.toString())
            .addOnSuccessListener {

                startActivity(Intent(requireActivity(), MainActivity::class.java))
            }
            .addOnFailureListener {
                showSnackBar("Sign In Failed")
                loading(false)
            }

    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun isValidSignInDetails(): Boolean {
        return if (binding.inputEmail.text.toString().trim().isEmpty()) {
            showToast("Enter email")
            false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.text.toString()).matches()) {
            showToast("Enter valid mail")
            false
        } else if (binding.inputPassword.text.toString().trim().isEmpty()) {
            showToast("Enter password")
            false
        } else {
            true
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