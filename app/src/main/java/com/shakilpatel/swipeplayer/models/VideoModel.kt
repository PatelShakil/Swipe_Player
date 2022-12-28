package com.shakilpatel.swipeplayer.models

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

class VideoModel : Parcelable{
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
        videoModificationTime: String
    ) {
        this.videoTitle = videoTitle
        this.videoDuration = videoDuration
        this.videoUri = videoUri
        this.videoCreationTime = videoCreationTime
        this.videoModificationTime = videoModificationTime
    }

    override fun describeContents(): Int {
        return 1
    }

    override fun writeToParcel(p0: Parcel?, p1: Int) {
    }

}