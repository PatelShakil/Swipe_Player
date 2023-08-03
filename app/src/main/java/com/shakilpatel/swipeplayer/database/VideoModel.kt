package com.shakilpatel.swipeplayer.database

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fav_videos")
data class Video(
    @PrimaryKey(autoGenerate = true)
    val id : Int = 0,
    @ColumnInfo(name = "videoTitle")
    val videoTitle : String,
    @ColumnInfo(name = "videoDuration")
    val videoDuration : String,
    @ColumnInfo(name = "videoUri")
    val videoUri : String,
    @ColumnInfo(name = "videoCreationTime")
    val videoCreationTime : String,
    @ColumnInfo(name = "videoModificationTime")
    val videoModificationTime : String,
    @ColumnInfo(name = "size")
    val size: String
)