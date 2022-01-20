package com.blogspot.svdevs.videoplayer.ui.video

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.blogspot.svdevs.videoplayer.R
import com.blogspot.svdevs.videoplayer.data.Video
import com.blogspot.svdevs.videoplayer.databinding.VideoItemBinding
import com.blogspot.svdevs.videoplayer.ui.activities.MainActivity
import com.blogspot.svdevs.videoplayer.ui.activities.PlayerActivity
import com.blogspot.svdevs.videoplayer.ui.activities.PlayerActivity.Companion.pipStatus
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class VideoAdapter(
    private val context: Context, private var list: ArrayList<Video>,
    private val isFolder: Boolean = false
) :
    RecyclerView.Adapter<VideoAdapter.VideosViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideosViewHolder {
        return VideosViewHolder(
            VideoItemBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: VideosViewHolder, position: Int) {
        holder.title.text = list[position].title
        holder.path.text = list[position].path
        //holder.duration.text = DateUtils.formatElapsedTime(list[position].duration/1000)
        Glide.with(context).asBitmap().load(list[position].icon).apply(
            RequestOptions().placeholder(
                R.drawable.ic_video
            ).centerCrop()
        ).into(holder.icon)

        holder.root.setOnClickListener {
            when {
                // handling the now playing video in videos list such that it resumes from previous stop
                    list[position].id == PlayerActivity.nowPlayingID -> {
                    sendIntent(position, "NowPlaying")
                }

                isFolder -> {
                    pipStatus = 1
                    sendIntent(position, "FoldersActivity")
                }
                MainActivity.search -> {
                    pipStatus = 2
                    sendIntent(position, "SearchVideos")
                }
                else -> {
                    pipStatus = 3
                    sendIntent(position, "AllVideos")
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private fun sendIntent(pos: Int, ref: String) {
        PlayerActivity.pos = pos
        val intent = Intent(context, PlayerActivity::class.java)
        intent.putExtra("class", ref)
        ContextCompat.startActivity(context, intent, null)
    }

    // updating the video list when searching for videos
    fun updateList(searchList: ArrayList<Video>) {
        list = ArrayList()
        list.addAll(searchList)
        notifyDataSetChanged()
    }

    class VideosViewHolder(binding: VideoItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val title = binding.videoTitle
        val path = binding.videoFolder
        val duration = binding.videoDuration
        val icon = binding.videoImage
        val root = binding.root
    }

}

