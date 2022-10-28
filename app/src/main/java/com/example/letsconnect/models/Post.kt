package com.example.letsconnect.models

class Post {
    constructor()
    constructor(
        userId: String,
        postId:String,
        profileImage:String?,
        username: String?,
        name: String?,
        email: String?,
        totalViews: Int,
        uploadTime: Long,
        postMessage: String?,
        totalLikes: Int,
        totalComments: Int,
        likedBy:ArrayList<String>
    ) {
        this.userId = userId
        this.postId = postId
        this.username = username
        this.name = name
        this.email = email
        this.profileImage = profileImage
        this.totalViews = totalViews
        this.uploadTime = uploadTime
        this.postMessage = postMessage
        this.totalLikes = totalLikes
        this.totalComments = totalComments
        this.likedBy = likedBy
    }

    lateinit var userId: String
    lateinit var postId: String
    var username: String? = null
    var name: String? = null
    var email: String? = null
    var profileImage:String? = null
    var totalViews: Int = 0
    var uploadTime: Long = 0
    var postMessage: String? = null
    var totalLikes: Int = 0
    var totalComments: Int = 0
     var likedBy=ArrayList<String>()
}
