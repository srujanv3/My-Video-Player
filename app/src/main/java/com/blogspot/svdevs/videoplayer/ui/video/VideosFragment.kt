package com.blogspot.svdevs.videoplayer.ui.video

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.blogspot.svdevs.videoplayer.ui.MainActivity.Companion.videoList
import com.blogspot.svdevs.videoplayer.R
import com.blogspot.svdevs.videoplayer.databinding.FragmentVideosBinding

class VideosFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_videos, container, false)
        val binding = FragmentVideosBinding.bind(view)

        binding.apply {
            videoRecyclerView.apply {
                setHasFixedSize(true)
                setItemViewCacheSize(10)
                layoutManager = LinearLayoutManager(requireContext())
                adapter = VideoAdapter(requireContext(),videoList)
            }
        }

        return view
    }
}