package com.shakilpatel.swipeplayer.activities

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
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
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector.Detections
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.Tracker
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector
import com.shakilpatel.swipeplayer.R
import com.shakilpatel.swipeplayer.databinding.ActivityVideoPlayerBinding
import com.shakilpatel.swipeplayer.models.VideoModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.NonCancellable.start
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.properties.Delegates


open class VideoPlayerActivity : AppCompatActivity() {
    lateinit var binding: ActivityVideoPlayerBinding
    var list = ArrayList<String>()
    lateinit var cameraSource :CameraSource
    lateinit var videoView: VideoView
    lateinit var videoList:ArrayList<String>
    var position by Delegates.notNull<Int>()
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
            videoList = intent.getSerializableExtra("videoList") as ArrayList<String>
            position = intent.extras?.get("pos").toString().toInt()
            binding.videoPlayer.setVideoURI(Uri.parse(videoList[position]))
            binding.videoPlayer.start()
            var check = false
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
            if (position == videoList.size - 1){
                binding.videoPlayer.pause()
                Toast.makeText(this,"No next video",Toast.LENGTH_SHORT).show()
                return@setPrevNextListeners
            }else {
                binding.videoPlayer.setVideoURI(Uri.parse(videoList[position + 1]))
                position++
                binding.videoPlayer.start()
            }
        }) {
            if (position == 0){
                Toast.makeText(this,"No previous video",Toast.LENGTH_SHORT).show()
                return@setPrevNextListeners
            }else {
                binding.videoPlayer.setVideoURI(Uri.parse(videoList[position - 1]))
                position--
                binding.videoPlayer.start()
            }
        }
        binding.videoPlayer.setMediaController(m)
        binding.videoPlayer.setOnLongClickListener {
            binding.root.keepScreenOn = true
            binding.videoPlayer.setVideoURI(Uri.parse(videoList[position + 1]))
            position++
            binding.videoPlayer.start()
            true
        }
        binding.videoPlayer.setOnCompletionListener {
            binding.root.keepScreenOn = binding.videoPlayer.isPlaying

            if (binding.repeatSwitch.isChecked){
                binding.videoPlayer.start()
            }else{
                binding.root.keepScreenOn = true
            if (position < videoList.size) {
                binding.root.keepScreenOn = true
                if (position == videoList.size -1){
                    binding.videoPlayer.pause()
                    Toast.makeText(this,"Video list was finished.",Toast.LENGTH_SHORT).show()
                    return@setOnCompletionListener
                }else {
                    binding.videoPlayer.setVideoURI(Uri.parse(videoList[position + 1]))
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
            intent.putExtra(Intent.EXTRA_STREAM,Uri.parse(videoList[position]))
            startActivity(Intent.createChooser(intent,"Share video on ..."))
            binding.videoPlayer.pause()
        }
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
}