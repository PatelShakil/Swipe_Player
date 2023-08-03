package com.shakilpatel.swipeplayer.database

import android.net.Uri
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface VideoDao {
    @Query("SELECT * FROM fav_videos")
    fun getAllVideos(): List<Video>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertVideo(video: Video)

    @Query("DELETE FROM fav_videos WHERE videoUri = :videoUri")
    fun deleteVideoByUri(videoUri: String)

    @Query("SELECT COUNT(*) FROM fav_videos WHERE videoUri = :videoPath")
    fun checkVideoExists(videoPath: String): Int
}