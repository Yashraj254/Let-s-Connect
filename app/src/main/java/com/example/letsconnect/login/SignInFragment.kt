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
import androidx.core.view.isVisible
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
import javax.inject.Inject


@AndroidEntryPoint
class SignInFragment : Fragment() {

    private lateinit var navBar: BottomNavigationView
    private  var _binding: FragmentSignInBinding?=null
    private val binding get() = _binding!!

    @Inject
    lateinit var auth:FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private var usernameIsNull = false
    private val viewModel:LoginViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        _binding  = FragmentSignInBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (auth.currentUser != null) {
            viewModel.getCurrentUser().observe(viewLifecycleOwner){
                when(it){
                    is Response.Loading->{
                        binding.progressBar.isVisible = true
                    }
                    is Response.Success->{
                        binding.progressBar.isVisible = false
                        usernameIsNull = it.data?.username ==null
                        if(it.data?.username!=null){
                            Navigation.findNavController(requireView())
                                .navigate(R.id.action_signInFragment_to_navigation_home)
                        }else{
                            Navigation.findNavController(requireView())
                                .navigate(R.id.action_signInFragment_to_navigation_profile)
                        }
                    }
                    is Response.Failure->{
                        showSnackBar("Failed: "+it.errorMessage.toString())
                    }
                }
            }
        }
        else{
            binding.apply {
                tvTitle.isVisible = false
                layoutSignIn.isVisible = true
            }
        }

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

                }
                is Response.Success<*> -> {
                    val isNewUser = response.data
                    if (isNewUser as Boolean) {
                        viewModel.deleteAccount(idToken).observe(this){
                            when(it){
                                is Response.Loading -> { }
                                is Response.Success -> {
                                    auth.signOut()
                                }
                                is Response.Failure -> {
                                    showSnackBar("Error: "+it.errorMessage)
                                }
                            }
                        }

                        Navigation.findNavController(requireView())
                            .navigate(R.id.action_signInFragment_to_signUpFragment)

                    } else {
                        if(usernameIsNull)
                        Navigation.findNavController(requireView())
                            .navigate(R.id.action_signInFragment_to_navigation_profile)
                        else
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
        actionBar.isVisible = false
        actionBar.title = null
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}