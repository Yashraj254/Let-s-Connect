package com.example.letsconnect.repository

import android.content.Context
import android.widget.Toast
import com.example.letsconnect.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val database: FirebaseFirestore,
    private var storageRef: StorageReference,
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context,
) : BaseRepo(context) {
    @Inject
    lateinit var currentUser: String
    suspend fun getAllFollowers(userId: String): Resource<QuerySnapshot> = safeApiCall {
        database.collection("users").document(userId)
            .collection("followers").get()
    }


    suspend fun getAllFollowing(userId: String): Resource<QuerySnapshot> = safeApiCall {
        database.collection("users").document(userId)
            .collection("following").get()
    }

    suspend fun showCurrentUserDetails(userId: String): Resource<DocumentSnapshot> = safeApiCall {
        database.collection(KEY_COLLECTION_USERS).document(userId).get()
    }

    suspend fun unfollowUser(userId: String) = safeApiCall{
        database.collection("users").document(currentUser)
            .collection("following").document(userId).delete().onSuccessTask {
                database.collection("users").document(currentUser)
                    .collection("following").get().onSuccessTask {
                        val following = HashMap<String, Any>()
                        following[KEY_FOLLOWING] = it.size()
                        database.collection(KEY_COLLECTION_USERS).document(currentUser)
                            .update(following)
                    }
            }
    }

    suspend fun removeFollower(userId: String) = safeApiCall {
        database.collection("users").document(currentUser)
            .collection("followers").document(userId).delete().onSuccessTask {
                database.collection("users").document(currentUser)
                    .collection("followers").get().onSuccessTask {
                        val following = HashMap<String, Any>()
                        following[KEY_FOLLOWING] = it.size()
                        database.collection(KEY_COLLECTION_USERS).document(currentUser)
                            .update(following)
                    }
            }
    }

    suspend fun followUser(userId: String, user: HashMap<String, Any>) = safeApiCall {
        database.collection(KEY_COLLECTION_USERS).document(currentUser)
            .collection("following")
            .document(userId).set(user).onSuccessTask {
                database.collection(KEY_COLLECTION_USERS).document(currentUser)
                    .collection("followers").get().onSuccessTask {
                        val followers = HashMap<String, Any>()
                        followers[KEY_FOLLOWERS] = it.size()
                        database.collection(KEY_COLLECTION_USERS).document(currentUser)
                            .update(followers)
                    }
            }

        database.collection(KEY_COLLECTION_USERS).document(userId).collection("followers")
            .document(auth.currentUser!!.uid).set(user).onSuccessTask {
                database.collection(KEY_COLLECTION_USERS).document(userId)
                    .collection("followers").get().onSuccessTask {
                        val followers = HashMap<String, Any>()
                        followers[KEY_FOLLOWERS] = it.size()
                        database.collection(KEY_COLLECTION_USERS).document(userId)
                            .update(followers)
                    }
            }
    }

    suspend fun checkIfFollowing(userId: String): Resource<QuerySnapshot> = safeApiCall {
        database.collection(KEY_COLLECTION_USERS).document(currentUser)
            .collection("following")
            .get()
    }

    suspend fun getAllUsers(): Resource<QuerySnapshot> = safeApiCall {
        database.collection("users").get()
    }


    suspend fun sendAndReceiveMessage(textMessage: String, receiverUid: String) = safeApiCall {
        val senderUid = auth.uid.toString()
        val randomKey = database.collection("chats").document().id
        val message = HashMap<String, Any>()
        val senderRoom = senderUid + receiverUid
        val receiverRoom = receiverUid + senderUid

        message["message"] = textMessage
        message["senderId"] = senderUid
        message["timeStamp"] = Date().time
        message["sentTime"] = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

        database.collection("chats").document(senderRoom).collection("message")
            .document(randomKey).set(message)
        database.collection("chats").document(receiverRoom).collection("message")
            .document(randomKey).set(message)
    }

    suspend fun getAllChats(senderRoom: String): Resource<QuerySnapshot> = safeApiCall {
        database.collection("chats").document(senderRoom).collection("message").orderBy("timeStamp")
            .get()
    }

    suspend fun uploadProfilePic(encodedImage: ByteArray) = safeApiCall {
        storageRef.putBytes(encodedImage).addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener {
                val obj = mutableMapOf<String, String>()
                obj[KEY_PROFILE_IMAGE] = it.toString()
                database.collection("users").document(currentUser)
                    .update(obj as Map<String, Any>).addOnSuccessListener {
                    Toast.makeText(context, "Profile Picture Uploaded", Toast.LENGTH_SHORT).show()
                }
            }
        }.addOnFailureListener {

            Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
        }
    }
}