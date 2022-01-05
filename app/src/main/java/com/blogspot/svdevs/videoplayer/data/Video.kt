package com.blogspot.svdevs.videoplayer.data

import android.net.Uri

data class Video(
    val id: String,
    //val duration: Long = 0,
    val title: String,
    val path: String,
    val size: String,
    val icon:Uri
)