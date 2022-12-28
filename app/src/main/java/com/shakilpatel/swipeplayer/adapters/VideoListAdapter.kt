package com.shakilpatel.swipeplayer.adapters

import android.content.Context
import android.content.Intent
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.core.net.toFile
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.shakilpatel.swipeplayer.R
import com.shakilpatel.swipeplayer.activities.VideoPlayerActivity
import com.shakilpatel.swipeplayer.databinding.SampleVideoListItemBinding
import com.shakilpatel.swipeplayer.models.VideoModel
import java.io.File


class VideoListAdapter : RecyclerView.Adapter<VideoListAdapter.VideoListViewHolder> {
    var context : Context
    var videoList : ArrayList<VideoModel>

    constructor(context: Context, videoList: ArrayList<VideoModel>) : super() {
        this.context = context
        this.videoList = videoList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoListViewHolder {
        return VideoListViewHolder(LayoutInflater.from(context).inflate(R.layout.sample_video_list_item,null,true))
    }

    override fun onBindViewHolder(holder: VideoListViewHolder, position: Int) {
        holder.setData(videoList[position],videoList,position)
    }

    override fun getItemCount(): Int {
        return videoList.size
    }
    class VideoListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: SampleVideoListItemBinding
        init {
            binding = SampleVideoListItemBinding.bind(itemView)
        }

        fun setData(model : VideoModel,videoList : ArrayList<VideoModel>,pos : Int){
            var list : ArrayList<String> = ArrayList()
            for(i in videoList){
                list.add(i.videoUri.toString())
            }
            binding.listVideoTitle.text = model.videoTitle
            binding.listVideoDuration.text = model.videoDuration
            Glide.with(binding.listVideo.context)
                .load(Uri.fromFile(File(model.videoUri.toString())))
                .placeholder(R.drawable.video_placeholder)
                .into(binding.videoThumbnail)
            binding.listVideo.setOnClickListener {
                var intent = Intent(binding.listVideo.context,VideoPlayerActivity::class.java)
                intent.putExtra("videoList",list)
                intent.putExtra("pos",pos)
                binding.listVideo.context.startActivity(intent)
            }
            var popupWindow = PopupWindow(
                LayoutInflater.from(binding.listVideo.context).inflate(R.layout.view_video_info_popup, null, false),
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
            )
            binding.listVideo.setOnLongClickListener (object :OnLongClickListener{
                override fun onLongClick(p0: View?): Boolean {
                    popupWindow.showAtLocation(binding.listVideo,Gravity.CENTER,0,0)
                    Glide.with(popupWindow.contentView.findViewById<ImageView>(R.id.video_thumbnail))
                        .load(Uri.fromFile(File(model.videoUri.toString())))
                        .placeholder(R.drawable.video_placeholder)
                        .into(popupWindow.contentView.findViewById<ImageView>(R.id.video_thumbnail))
                    popupWindow.contentView.findViewById<ImageView>(R.id.video_thumbnail).setOnClickListener {
                        var intent = Intent(popupWindow.contentView.findViewById<ImageView>(R.id.video_thumbnail).context,VideoPlayerActivity::class.java)
                        intent.putExtra("uri",model.videoUri)
                        popupWindow.contentView.findViewById<ImageView>(R.id.video_thumbnail).context.startActivity(intent)
                    }
                    popupWindow.contentView.findViewById<TextView>(R.id.video_info_tv).text = "Name : " + model.videoTitle + "\n" +
                            "Location : " + model.videoUri + "\n" +
                            "Duration : " + model.videoDuration + "\n" +
                            "Date Added : " + model.videoCreationTime + "\n" +
                            "Date Modified : " + model.videoModificationTime + "\n" +
                            "Size : " + model.size
                    return true
                }
            })
        }
    }
}