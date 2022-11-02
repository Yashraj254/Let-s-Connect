package com.example.letsconnect.repository

import android.content.Context
import android.util.Log
import com.example.letsconnect.Resource
import com.example.letsconnect.Response
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue.serverTimestamp
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class LoginRepository @Inject constructor(
    private val database: FirebaseFirestore,
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context,
) : BaseRepo(context) {

    suspend fun firebaseSignInWithGoogle(idToken: String) = flow {
        Log.d("Login Repo", "firebase sign in: ")

        try {
            emit(Response.Loading)
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            authResult.additionalUserInfo?.apply {
                emit(Response.Success(isNewUser))
            }
        } catch (e: Exception) {
            emit(Response.Failure(e.message ?: "Some error"))
        }
    }

    suspend fun deleteAccount(idToken: String) = flow {
        Log.d("Login Repo", "deleteAccount: ")

        try {
            Log.d("Login Repo", "deleteAccount: try ")
            emit(Response.Loading)
            val user = FirebaseAuth.getInstance().currentUser;
            val credential = GoogleAuthProvider.getCredential(idToken, null)

            user?.reauthenticate(credential)?.addOnCompleteListener {
                Log.d("Login Repo", "account deleted: ")

                user.delete()
            }
//            user?.signOut().apply {
//                emit(Response.Success("User signed out"))
//            }
        } catch (e: Exception) {
            emit(Response.Failure(e.message ?: "Some error"))
            Log.d("Login Repo", "${e.message} ")

        }
    }

    suspend fun createUser() = flow{
        try {
            emit(Response.Loading)
            auth.currentUser?.apply {
                database.collection("users").document(uid).set(mapOf(
                    "name" to displayName,
                    "email" to email,
                    "followers" to 0,
                    "following" to 0,
                    "userId" to uid
                )).await().also {
                    emit(Response.Success(it))
                }
            }
        }catch (e:Exception){
            emit(Response.Failure(e.message?:"Some message"))
        }
    }
}
