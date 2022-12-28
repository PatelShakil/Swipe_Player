package com.shakilpatel.swipeplayer.adapters

import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer.OnCompletionListener
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.Tracker
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector
import com.shakilpatel.swipeplayer.R
import com.shakilpatel.swipeplayer.databinding.ActivityVideoPlayerBinding
import com.shakilpatel.swipeplayer.databinding.SampleReelsItemBinding
import com.shakilpatel.swipeplayer.models.VideoModel
import java.io.IOException


class ReelsAdapter:RecyclerView.Adapter<ReelsAdapter.ReelsViewHolder> {
    var context : Context
    var videoList : ArrayList<VideoModel>

    constructor(context: Context, videoList: ArrayList<VideoModel>) : super() {
        this.context = context
        this.videoList = videoList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReelsViewHolder {
//        return ReelsViewHolder(LayoutInflater.from(context).inflate(R.layout.sample_reels_item,null,true))
        val view: View = LayoutInflater.from(context).inflate(R.layout.sample_reels_item, parent, false)
        view.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        return ReelsViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReelsViewHolder, position: Int) {
        val model = videoList[position]
        holder.binding.reelsVideoVv.keepScreenOn = true
        holder.setData(model)
    }

    override fun getItemCount(): Int {
        return videoList.size
    }
    class ReelsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: SampleReelsItemBinding
        init {
            binding = SampleReelsItemBinding.bind(itemView)
        }
        fun setData(model : VideoModel){
            var check = true

            binding.reelsVideoTitle.text = model.videoTitle
            binding.reelsVideoVv.setVideoURI(model.videoUri)
            binding.reelsVideoVv.requestFocus()
            binding.reelsVideoVv.setOnPreparedListener { binding.reelsVideoVv.start() }
            binding.reelsVideoVv.setOnCompletionListener {
                binding.reelsVideoVv.start()
            }
            binding.reelsItem.setOnClickListener {
                if (check){
                    binding.reelsVideoVv.pause()
                    check = false
                }else{
                    binding.reelsVideoVv.start()
                    check = true
                }
            }
        }

    }
}