package com.shakilpatel.swipeplayer.activities
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.MediaController
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.otaliastudios.cameraview.VideoResult
import com.otaliastudios.cameraview.size.AspectRatio
import com.shakilpatel.swipeplayer.R

class VideoPreviewActivity : AppCompatActivity() {
    companion object {
        var videoResult: VideoResult? = null
    }

    private val videoView: VideoView by lazy { findViewById<VideoView>(R.id.video) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_preview)
        val result = videoResult ?: run {
            finish()
            return
        }
        videoView.setOnClickListener { playVideo() }
        val actualResolution = findViewById<TextView>(R.id.actualResolution)
        val isSnapshot = findViewById<TextView>(R.id.isSnapshot)
        val rotation = findViewById<TextView>(R.id.rotation)
        val audio = findViewById<TextView>(R.id.audio)
        val audioBitRate = findViewById<TextView>(R.id.audioBitRate)
        val videoCodec = findViewById<TextView>(R.id.videoCodec)
        val audioCodec = findViewById<TextView>(R.id.audioCodec)
        val videoBitRate = findViewById<TextView>(R.id.videoBitRate)
        val videoFrameRate = findViewById<TextView>(R.id.videoFrameRate)

        val ratio = AspectRatio.of(result.size)
        actualResolution.setText("Size : "+ "${result.size} ($ratio)")
        isSnapshot.setText("Snapshot : "+  result.isSnapshot.toString())
        rotation.setText("Rotation : " + result.rotation.toString())
        audio.setText("Audio : " +  result.audio.name)
        audioBitRate.setText("Audio bit rate : " +  "${result.audioBitRate} bits per sec.")
        videoCodec.setText("VideoCodec : " +  result.videoCodec.name)
        audioCodec.setText("AudioCodec : " +  result.audioCodec.name)
        videoBitRate.setText("Video bit rate" +  "${result.videoBitRate} bits per sec.")
        videoFrameRate.setText("Video frame rate" +  "${result.videoFrameRate} fps")

        val controller = MediaController(this)
        controller.setAnchorView(videoView)
        controller.setMediaPlayer(videoView)
        videoView.setMediaController(controller)
        videoView.setVideoURI(Uri.fromFile(result.file))
        videoView.setOnPreparedListener { mp ->
            val lp = videoView.layoutParams
            val videoWidth = mp.videoWidth.toFloat()
            val videoHeight = mp.videoHeight.toFloat()
            val viewWidth = videoView.width.toFloat()
            lp.height = (viewWidth * (videoHeight / videoWidth)).toInt()
            videoView.layoutParams = lp
            playVideo()
            if (result.isSnapshot) {
                // Log the real size for debugging reason.
                Log.e("VideoPreview", "The video full size is " + videoWidth + "x" + videoHeight)
            }
        }
    }

    fun playVideo() {
        if (!videoView.isPlaying) {
            videoView.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isChangingConfigurations) {
            videoResult = null
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.share, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.share) {
            Toast.makeText(this, "Sharing...", Toast.LENGTH_SHORT).show()
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "video/*"
            val uri = FileProvider.getUriForFile(this,
                this.packageName + ".provider",
                videoResult!!.file)
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}