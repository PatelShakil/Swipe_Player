package com.shakilpatel.swipeplayer.activities

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModel
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector.Detections
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.Tracker
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.shakilpatel.swipeplayer.R
import com.shakilpatel.swipeplayer.classes.Constants.Companion.videoList
import com.shakilpatel.swipeplayer.database.MyApp
import com.shakilpatel.swipeplayer.database.Video
import com.shakilpatel.swipeplayer.databinding.ActivityVideoPlayerBinding
import com.shakilpatel.swipeplayer.models.VideoModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.NonCancellable.start
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.concurrent.CountDownLatch
import kotlin.properties.Delegates


open class VideoPlayerActivity : AppCompatActivity() {
    lateinit var binding: ActivityVideoPlayerBinding
    var list = ArrayList<String>()
    lateinit var cameraSource :CameraSource
    lateinit var videoView: VideoView
    var position by Delegates.notNull<Int>()
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        list = ArrayList()
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), 1)
            Toast.makeText(this, "Grant Permission and restart app", Toast.LENGTH_SHORT).show()
        } else {
            videoView = findViewById<VideoView>(R.id.video_player)
            position = intent.extras?.get("pos").toString().toInt()

            binding.videoPlayer.setVideoURI(Uri.parse(videoList.get(position).videoUri.toString()))
            var isFav = false
            val l = CountDownLatch(1)
            GlobalScope.launch {
                isFav = isFav()
                l.countDown()
            }.start()
            l.await()
            if(isFav){
                binding.addFavBtn.foreground = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_heart_filled,
                    resources.newTheme()
                )
            }else{
                binding.addFavBtn.foreground = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_heart_outline,
                    resources.newTheme()
                )
            }
            binding.videoPlayer.start()
            binding.cameraSwitch.setOnCheckedChangeListener { compoundButton, b ->
                if (binding.cameraSwitch.isChecked){
                    GlobalScope.launch {
                    createCameraSource()
                    }.start()
                }else {
                    cameraSource.release()
                    binding.videoPlayer.start()
                }
            }
        }
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        val m = MediaController(this)
        m.setPrevNextListeners({
            prevClick(binding.videoPlayer)
        }) {
            nextClick(binding.videoPlayer)
        }
        binding.addFavBtn.setOnClickListener {
            // Insert a video into the database
//        val video = Video(title = "Sample Video", path = "/path/to/video.mp4")
            val v = videoList[position]
            val video = Video(0,v.videoTitle,v.videoDuration,v.videoUri.toString(),v.videoCreationTime,v.videoModificationTime,v.size)
            var check = ""
            GlobalScope.launch {
                val count = MyApp.database.videoDao().checkVideoExists(v.videoUri.toString())
                if (count > 0) {
                    // Video exists in the table
                    // Delete a video from the database
                    MyApp.database.videoDao().deleteVideoByUri(video.videoUri)
                    Log.d(TAG,"Removed Successfully")
                    check = "del"

                } else {
                    // Video does not exist in the table
                    MyApp.database.videoDao().insertVideo(video)
                    Log.d(TAG,"Added Successfully")
                    check = "add"
                }
            }
            var isFav = false
            val l = CountDownLatch(1)
            GlobalScope.launch {
                isFav = isFav()
                l.countDown()
            }.start()
            l.await()
            if(isFav){
                binding.addFavBtn.foreground = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_heart_filled,
                    resources.newTheme()
                )
            }else{
                binding.addFavBtn.foreground = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_heart_outline,
                    resources.newTheme()
                )
            }
        }
        binding.videoPlayer.setMediaController(m)
        binding.videoPlayer.setOnCompletionListener {
            binding.root.keepScreenOn = binding.videoPlayer.isPlaying

            if (binding.repeatSwitch.isChecked){
                binding.videoPlayer.start()
            }else{
                binding.root.keepScreenOn = true
            if (position < videoList.size) {
                binding.root.keepScreenOn = true
                if (position == videoList.size - 1){
                    binding.videoPlayer.pause()
                    Toast.makeText(this,"Video list was finished.",Toast.LENGTH_SHORT).show()
                    return@setOnCompletionListener
                }else {

                    binding.videoPlayer.setVideoURI(Uri.parse(videoList[position + 1].videoUri.toString()))
                    var isFav = false
                    val l = CountDownLatch(1)
                    GlobalScope.launch {
                        isFav = isFav()
                        l.countDown()
                    }.start()
                    l.await()
                    if(isFav){
                        binding.addFavBtn.foreground = ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.ic_heart_filled,
                            resources.newTheme()
                        )
                    }else{
                        binding.addFavBtn.foreground = ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.ic_heart_outline,
                            resources.newTheme()
                        )
                    }
                    position++
                    binding.videoPlayer.start()
                }
            }else{
                binding.videoPlayer.pause()
                binding.root.keepScreenOn = false
            }
        }
        }
        var check = true
        binding.videoPlayer.setOnClickListener {
            if (check){
                binding.videoPlayer.pause()
                check = false
            }else{
                binding.videoPlayer.start()
                check = true
            }
            binding.videoPlayer.keepScreenOn = binding.videoPlayer.isPlaying
        }
        binding.shareBtn.setOnClickListener{
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = ("video/mp4")
            intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(this,
                "$packageName.provider", File(videoList[position].videoUri.toString())))
            startActivity(Intent.createChooser(intent,"Share video on ..."))
            binding.videoPlayer.pause()
        }
    }
    @OptIn(DelicateCoroutinesApi::class)
    suspend fun isFav(): Boolean = withContext(Dispatchers.IO) {
        val exists = MyApp.database.videoDao().checkVideoExists(videoList[position].videoUri.toString()) > 0
        val check = if (exists) "y" else "n"
        Log.d("Check", check)
        check == "y"
    }

    class EyesTracker : Tracker<Face> {
        lateinit var binding : ActivityVideoPlayerBinding
        private val THRESHOLD = 0.5f

        constructor(binding: ActivityVideoPlayerBinding) : this() {
            this.binding = binding
        }

        constructor()

        override fun onUpdate(detections: Detections<Face>, face: Face) {
            if (face.isLeftEyeOpenProbability > THRESHOLD || face.isRightEyeOpenProbability > THRESHOLD) {
                Log.i(TAG, "onUpdate: Eyes Detected")
                if (!binding.videoPlayer.isPlaying)
                    binding.videoPlayer.start()
            } else {
                if (binding.videoPlayer.isPlaying())
                    binding.videoPlayer.pause()
            }
        }

        override fun onMissing(detections: Detections<Face>) {
            super.onMissing(detections)
            binding.videoPlayer.pause()
        }

        override fun onDone() {
            super.onDone()
        }
    }
    class FaceTrackerFactory : MultiProcessor.Factory<Face> {
        var binding: ActivityVideoPlayerBinding
        constructor(binding: ActivityVideoPlayerBinding){
            this.binding = binding
        }
        override fun create(face: Face): Tracker<Face> {
            return EyesTracker(binding)
        }
    }
    suspend fun createCameraSource() {
        val detector: FaceDetector = FaceDetector.Builder(this)
            .setTrackingEnabled(true)
            .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
            .setMode(FaceDetector.FAST_MODE)
            .build()
        detector.setProcessor(MultiProcessor.Builder(FaceTrackerFactory(binding)).build())
        cameraSource = CameraSource.Builder(this, detector)
            .setRequestedPreviewSize(1024, 768)
            .setFacing(CameraSource.CAMERA_FACING_FRONT)
            .setRequestedFps(30.0f)
            .build()
        try {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            cameraSource.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onStop() {
        super.onStop()
        binding.videoPlayer.stopPlayback()
    }

    override fun onResume() {
        super.onResume()
        binding.videoPlayer.start()
    }

    override fun onPause() {
        super.onPause()
        binding.videoPlayer.pause()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        binding.videoPlayer.stopPlayback()
    }

    fun prevClick(view: View) {

        if (position == 0){
            binding.videoPlayer.pause()
            Toast.makeText(this,"No previous video",Toast.LENGTH_SHORT).show()
        }else {
            binding.videoPlayer.setVideoURI(Uri.parse(videoList[position - 1].videoUri.toString()))
            var isFav = false
            val l = CountDownLatch(1)
            GlobalScope.launch {
                isFav = isFav()
                l.countDown()
            }.start()
            l.await()
            if(isFav){
                binding.addFavBtn.foreground = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_heart_filled,
                    resources.newTheme()
                )
            }else{
                binding.addFavBtn.foreground = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_heart_outline,
                    resources.newTheme()
                )
            }
            position--
            binding.videoPlayer.start()
        }
    }
    @OptIn(DelicateCoroutinesApi::class)
    fun nextClick(view: View) {

        if (position == videoList.size - 1){
            Toast.makeText(this,"No next video",Toast.LENGTH_SHORT).show()
            return
        }else {
            binding.videoPlayer.setVideoURI(Uri.parse(videoList[position + 1].videoUri.toString()))
            var isFav = false
            val l = CountDownLatch(1)
            GlobalScope.launch {
                isFav = isFav()
                l.countDown()
            }.start()
            l.await()
            if(isFav){
                binding.addFavBtn.foreground = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_heart_filled,
                    resources.newTheme()
                )
            }else{
                binding.addFavBtn.foreground = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_heart_outline,
                    resources.newTheme()
                )
            }
            position++
            binding.videoPlayer.start()
        }
    }

}