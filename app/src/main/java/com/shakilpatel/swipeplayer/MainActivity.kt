package com.shakilpatel.swipeplayer

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattCharacteristic.PERMISSION_READ
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import android.widget.SearchView.OnQueryTextListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isEmpty
import androidx.core.view.isNotEmpty
import com.google.ar.core.dependencies.e
import com.google.ar.core.dependencies.i
import com.shakilpatel.swipeplayer.activities.CameraActivity
import com.shakilpatel.swipeplayer.activities.ReelsActivity
import com.shakilpatel.swipeplayer.adapters.VideoListAdapter
import com.shakilpatel.swipeplayer.classes.Constants
import com.shakilpatel.swipeplayer.classes.Constants.Companion.videoList
import com.shakilpatel.swipeplayer.database.MyApp
import com.shakilpatel.swipeplayer.database.Video
import com.shakilpatel.swipeplayer.databinding.ActivityMainBinding
import com.shakilpatel.swipeplayer.models.VideoModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var adapter : VideoListAdapter
    lateinit var videoList : ArrayList<VideoModel>
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.mainToolbar)
        binding.mainToolbar.setOnClickListener {
            startActivity(Intent(this,CameraActivity::class.java))
        }
        binding.reelsBtn.setOnClickListener {
            startActivity(Intent(this,ReelsActivity::class.java))
        }
        binding.reelsBtn.setOnLongClickListener{
            videoList = ArrayList()
            if (checkPermission())
                getVideos()
            true
        }
        videoList = ArrayList()

        var favCheck = true
        binding.favsBtn.setOnClickListener            {
            if(favCheck) {
                favCheck = false
                videoList.clear()
                var vList: List<Video>? = null
                GlobalScope.launch {
                    vList = MyApp.database.videoDao().getAllVideos()
                    if (vList?.isNotEmpty() == true) {
                        vList?.forEach { video ->
                            Log.d(video.id.toString(), video.videoUri)
                            videoList.add(
                                VideoModel(
                                    video.videoTitle,
                                    video.videoDuration,
                                    Uri.parse(video.videoUri),
                                    video.videoCreationTime,
                                    video.videoModificationTime,
                                    video.size
                                )
                            )
                        }
                    }
                    adapter = VideoListAdapter(binding.root.context, videoList)
                    adapter.notifyDataSetChanged()
                }

            }else{
                favCheck = true
                videoList.clear()
                getVideos()
            }
            binding.videoListRv.adapter = adapter
                    binding.totalVideos.text =
                "Swipe Player" + "\n" + "You have Total " + videoList.size.toString() + " videos."

        }

    }
    @SuppressLint("Range")
    fun getVideos() {
        val contentResolver = contentResolver
        val uri: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val cursor: Cursor? = contentResolver.query(
            uri,
            null,
            "${MediaStore.Video.Media.DURATION} <= ?",
            arrayOf(TimeUnit.MILLISECONDS.convert(3,TimeUnit.MINUTES).toString()),
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
                val dateadded : Long = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DATE_TAKEN))
                val size  = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.SIZE))
                val datemodified : Long = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DATE_MODIFIED))
                val videoModel = VideoModel(title,timeConversion(duration.toLong()),Uri.parse(data),convertLongToTime(dateadded),convertLongToTime(datemodified),sizeConversion(size))
                videoList.add(videoModel)
            } while (cursor.moveToNext())
        }
        videoList.shuffle()
        adapter = VideoListAdapter(this, videoList)
        binding.videoListRv.adapter = adapter
        adapter.notifyDataSetChanged()
        binding.totalVideos.text = "Swipe Player"+"\n"+"You have Total " + videoList.size.toString() +" videos."
    }
    fun sizeConversion(size : String): String{
        if (Integer.parseInt(size) < 1000) {
            return size + " bytes"
        }
        else if (Integer.parseInt(size) < 100000){
            return size.substring(0,3) + " kb"
            }
        else if (Integer.parseInt(size) > 1000000)    {
            return size.substring(0,3) + " mb"
            }
        else
            return size
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

    fun convertLongToTime(time: Long): String {
        val date = Date(time)
        return SimpleDateFormat("hh:mm:ss a ,dd ,MMM, yyyy",Locale.getDefault()).format(date)
    }
    fun readableDate(date:Date):String{
        return SimpleDateFormat("hh:mm a - MMM dd, yyyy",Locale.getDefault()).format(date)
    }
    fun checkPermission(): Boolean {
        val permissions = if(Build.VERSION.SDK_INT >= 33)
            arrayOf(Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_AUDIO)
        else
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

        val READ_EXTERNAL_PERMISSION = if(Build.VERSION.SDK_INT >= 33)
            ActivityCompat.checkSelfPermission(this,Manifest.permission.READ_MEDIA_VIDEO)
        else
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
        if (READ_EXTERNAL_PERMISSION != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
               this,
                permissions,
                PERMISSION_READ
            )
            return false
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.home_menu,menu)
        if (checkPermission())
            getVideos()
        val searchItem = menu.findItem(R.id.home_search)
        val searchBar : SearchView = searchItem.actionView as SearchView
        searchBar.queryHint = "Search Any Video"
        if (searchBar.query.isEmpty()) {
            adapter = VideoListAdapter(this, videoList)
            binding.videoListRv.adapter = adapter
        }
        if (!searchBar.equals(null)){
            searchBar.setOnQueryTextListener(object :OnQueryTextListener{
                override fun onQueryTextSubmit(p0: String?): Boolean {
                    search(p0)
                    return false
                }

                override fun onQueryTextChange(p0: String?): Boolean {
                    search(p0)
                    return false
                }
            })
        }
        binding.totalVideos.text = "Swipe Player"+"\n"+"You have Total " + videoList.size.toString() +" videos."
        searchBar.setOnQueryTextFocusChangeListener { view, b ->
            if (searchBar.query.isEmpty()) {
                val adapter = VideoListAdapter(this, videoList)
                binding.videoListRv.adapter = adapter
            }else
                Log.d("Else",searchBar.query.toString())
        }
        return super.onCreateOptionsMenu(menu)
    }
    private fun search(s: String?) {
        Log.d("searchFun",s.toString())
        val list = ArrayList<VideoModel>()
        for (i in videoList){
            if (i.videoTitle.lowercase().contains(s?.lowercase()?.trim()!!)){
                list.add(i)
            }
        }
        adapter = VideoListAdapter(this,list)
        adapter.notifyDataSetChanged()
        binding.totalVideos.text = list.size.toString()
        binding.videoListRv.adapter = adapter
    }
}