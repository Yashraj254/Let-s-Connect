package com.example.letsconnect.repository

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import com.example.letsconnect.*
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class FirebaseRepository @Inject constructor(
    db: FirebaseFirestore,
    private var storageRef: StorageReference,
    @ApplicationContext private val context: Context,
) : BaseRepo(context) {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseFirestore.getInstance()
    private val databaseRef = database.collection("chats")
    private val allPostRef = database.collection(KEY_ALL_POSTS)


    fun sendAndReceiveMessage(textMessage: String, receiverUid: String) {
        val senderUid = auth.uid.toString()
        val randomKey = databaseRef.document().id
        val message = HashMap<String, Any>()
        val senderRoom = senderUid + receiverUid
        val receiverRoom = receiverUid + senderUid

        message["message"] = textMessage
        message["senderId"] = senderUid
        message["timeStamp"] = Date().time
        message["sentTime"] = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

        databaseRef.document(senderRoom).collection("message")
            .document(randomKey).set(message)
        databaseRef.document(receiverRoom).collection("message")
            .document(randomKey).set(message)
    }

    suspend fun addNewPost(postMessage: String) = safeApiCall {
        database.collection(KEY_COLLECTION_USERS).document(auth.currentUser!!.uid).get()
            .onSuccessTask {
                val date = Date()
                val username = it.getString(KEY_USER_NAME)
                val userId = auth.currentUser!!.uid
                val profileImage = it.getString(KEY_PROFILE_IMAGE)
                val post = HashMap<String, Any>()
                post[KEY_USER_ID] = userId
                post[KEY_USER_NAME] = username!!
                if (profileImage != null) {
                    post[KEY_PROFILE_IMAGE] = it.getString(KEY_PROFILE_IMAGE)!!
                }
                post[KEY_EMAIL] = auth.currentUser!!.email!!
                post[KEY_TOTAL_VIEWS] = 0
                post[KEY_TOTAL_COMMENTS] = 0
                post[KEY_TOTAL_LIKES] = 0
                post[KEY_UPLOAD_TIME] = date.time
                post[KEY_POST_MESSAGE] = postMessage
                val postId = database.collection(KEY_ALL_POSTS).document()
                post["postId"] = postId.id
                postId.set(post).addOnSuccessListener {
                    database.collection("users").document(userId).collection("myPosts")
                        .document(postId.id).set(post)

                }
            }
    }

    suspend fun postComment(
        postId: String,
        message: String,
        ) = safeApiCall {
        val userId = auth.currentUser!!.uid
        database.collection(KEY_COLLECTION_USERS).document(userId).get().onSuccessTask {
            val comment = HashMap<String, Any>()
            comment[KEY_USER_ID] = userId
            comment[KEY_USER_NAME] = it.getString(KEY_USER_NAME)!!
            comment[KEY_EMAIL] = it.getString(KEY_EMAIL)!!
            comment["commentMessage"] = message
            comment[KEY_PROFILE_IMAGE] = it.getString(KEY_PROFILE_IMAGE)!!
            val date = Date()
            comment[KEY_UPLOAD_TIME] = date.time
            database.collection(KEY_ALL_POSTS).document(postId).collection("comments")
                .add(comment).onSuccessTask {
                    database.collection(KEY_ALL_POSTS).document(postId).collection("comments").get().onSuccessTask {
                        val totalComments = it.size()
                        val obj = mutableMapOf<String, Int>()
                        obj[KEY_TOTAL_COMMENTS] = totalComments
                        database.collection(KEY_ALL_POSTS).document(postId)
                            .update(obj as Map<String, Any>)
                    }
                }
        }


//        database.collection(KEY_ALL_POSTS).document(postId).collection("comments")
//            .add(comment).onSuccessTask {
//                val obj = mutableMapOf<String, Int>()
//                obj["totalComments"] = totalPosts
//                allPostRef.document(postId).update(obj as Map<String, Any>)
//            }
    }

    suspend fun getAllComments(postId: String): Resource<QuerySnapshot> = safeApiCall {
        database.collection(KEY_ALL_POSTS).document(postId).collection("comments")
            .orderBy("uploadTime").get()
    }

       suspend fun getAllUsers(): Resource<QuerySnapshot> = safeApiCall {
        database.collection("users").get()
    }


       suspend fun getAllFollowers(userId: String): Resource<QuerySnapshot> = safeApiCall {
        database.collection("users").document(userId)
            .collection("followers").get()
    }


       suspend fun getAllFollowing(userId: String): Resource<QuerySnapshot> = safeApiCall {
        database.collection("users").document(userId)
            .collection("following").get()
    }


    suspend fun showCurrentPost(postId: String): Resource<DocumentSnapshot> = safeApiCall {
        database.collection(KEY_ALL_POSTS).document(postId).get()
    }


    suspend fun getAllPosts(): Resource<QuerySnapshot> = safeApiCall {
        allPostRef.orderBy("uploadTime", Query.Direction.DESCENDING).get()
    }

    suspend fun getLikedPosts(): Resource<QuerySnapshot> = safeApiCall {
        database.collection("users").document(auth.currentUser!!.uid)
            .collection("likedPosts").orderBy("uploadTime", Query.Direction.DESCENDING).get()
    }

    suspend fun showCurrentUserDetails(userId: String): Resource<DocumentSnapshot> = safeApiCall {
        database.collection(KEY_COLLECTION_USERS).document(userId).get()
    }


    suspend fun showCurrentUserPosts(userId: String): Resource<QuerySnapshot> = safeApiCall {
        database.collection(KEY_COLLECTION_USERS).document(userId)
            .collection("myPosts").get()
    }

    suspend fun followUser(userId: String, user: HashMap<String, Any>) = safeApiCall {
        database.collection(KEY_COLLECTION_USERS).document(auth.currentUser!!.uid)
            .collection("following")
            .document(userId).set(user).onSuccessTask {
                database.collection(KEY_COLLECTION_USERS).document(auth.currentUser!!.uid)
                    .collection("following").get().onSuccessTask {
                        val following = HashMap<String, Any>()
                        following[KEY_FOLLOWING] = it.size()
                        database.collection(KEY_COLLECTION_USERS).document(auth.currentUser!!.uid)
                            .update(following)
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
        database.collection(KEY_COLLECTION_USERS).document(auth.currentUser!!.uid)
            .collection("following")
            .get()
    }
}

