package com.blogspot.svdevs.videoplayer.ui.video

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.blogspot.svdevs.videoplayer.ui.activities.MainActivity.Companion.videoList
import com.blogspot.svdevs.videoplayer.R
import com.blogspot.svdevs.videoplayer.databinding.FragmentVideosBinding
import com.blogspot.svdevs.videoplayer.ui.activities.MainActivity
import com.blogspot.svdevs.videoplayer.ui.activities.PlayerActivity

class VideosFragment : Fragment() {

    private lateinit var adapter: VideoAdapter
    private lateinit var binding: FragmentVideosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_videos, container, false)
        binding = FragmentVideosBinding.bind(view)

        // now playing button
        binding.nowPlaying.setOnClickListener {
            // send intent to player activity
            val intent = Intent(requireContext(), PlayerActivity::class.java)
            intent.putExtra("class", "NowPlaying")
            startActivity(intent)

        }


        binding.apply {
            videoRecyclerView.apply {
                setHasFixedSize(true)
                setItemViewCacheSize(10)
                layoutManager = LinearLayoutManager(requireContext())
            }
        }
        // initializing and assigning adapter
        adapter = VideoAdapter(requireContext(), videoList)
        binding.videoRecyclerView.adapter = adapter

        return view
    }


    // for search view
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
        val searchView = menu.findItem(R.id.search)?.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    MainActivity.searchList = ArrayList()
                    for (video in MainActivity.videoList) {
                        if (video.title.lowercase().contains(newText.lowercase())) {
                            MainActivity.searchList.add(video)
                        }
                    }
                    MainActivity.search = true
                    adapter.updateList(searchList = MainActivity.searchList) // adding the search list instead of the video list
                }
                return true
            }
        })

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onResume() {
        super.onResume()
        if (PlayerActivity.pos != -1) {
            // this means some video is being played
            binding.nowPlaying.visibility = View.VISIBLE
        }
    }
}