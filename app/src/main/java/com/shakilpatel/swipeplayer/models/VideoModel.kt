package com.shakilpatel.swipeplayer.models

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

class VideoModel {
    lateinit var videoTitle : String
    lateinit var videoDuration : String
    lateinit var videoUri : Uri
    lateinit var videoCreationTime : String
    lateinit var videoModificationTime : String
    lateinit var size: String
    constructor()
    constructor(
        videoTitle: String,
        videoDuration: String,
        videoUri: Uri,
        videoCreationTime: String,
        videoModificationTime: String,
        size : String
    ) {
        this.videoTitle = videoTitle
        this.videoDuration = videoDuration
        this.videoUri = videoUri
        this.videoCreationTime = videoCreationTime
        this.videoModificationTime = videoModificationTime
        this.size = size
    }
}