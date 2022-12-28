package com.shakilpatel.swipeplayer.activities

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattCharacteristic
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.shakilpatel.swipeplayer.adapters.ReelsAdapter
import com.shakilpatel.swipeplayer.adapters.VideoListAdapter
import com.shakilpatel.swipeplayer.databinding.ActivityReelsBinding
import com.shakilpatel.swipeplayer.models.VideoModel
import java.util.concurrent.TimeUnit

class ReelsActivity : AppCompatActivity() {
    lateinit var binding : ActivityReelsBinding
    lateinit var videoList : ArrayList<VideoModel>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReelsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        videoList = ArrayList<VideoModel>()
        binding.reelsVp.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        if (checkPermission())
            getVideos()
    }
    @SuppressLint("Range")
    fun getVideos() {
        val contentResolver = contentResolver
        val uri: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val cursor: Cursor? = contentResolver.query(
            uri,
            null,
            "${MediaStore.Video.Media.DURATION} <= ?",
            arrayOf(TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES).toString()),
            null
        )

        //looping through all rows and adding to list
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val title: String =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE))
                val duration: String =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DURATION))
                val data: String =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA))
                val videoModel = VideoModel()
                videoModel.videoTitle = title
                videoModel.videoUri = Uri.parse(data)
                videoModel.videoDuration = timeConversion(duration.toLong())
                videoList.add(videoModel)
            } while (cursor.moveToNext())
        }
        videoList.shuffle()
        val adapter = ReelsAdapter(this, videoList)
        binding.reelsVp.adapter = adapter
    }
    fun timeConversion(value: Long): String {
        val videoTime: String
        val dur = value.toInt()
        val hrs = dur / 3600000
        val mns = dur / 60000 % 60000
        val scs = dur % 60000 / 1000
        videoTime = if (hrs > 0) {
            String.format("%02d:%02d:%02d", hrs, mns, scs)
        } else {
            String.format("%02d:%02d", mns, scs)
        }
        return videoTime
    }
    fun checkPermission(): Boolean {
        val READ_EXTERNAL_PERMISSION =
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
        if (READ_EXTERNAL_PERMISSION != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                BluetoothGattCharacteristic.PERMISSION_READ
            )
            return false
        }
        return true
    }
}