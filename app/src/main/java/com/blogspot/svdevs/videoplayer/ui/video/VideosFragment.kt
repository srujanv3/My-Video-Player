package com.blogspot.svdevs.videoplayer.ui.video

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.blogspot.svdevs.videoplayer.ui.MainActivity.Companion.videoList
import com.blogspot.svdevs.videoplayer.R
import com.blogspot.svdevs.videoplayer.databinding.FragmentVideosBinding
import com.blogspot.svdevs.videoplayer.ui.MainActivity

class VideosFragment : Fragment() {

    private lateinit var adapter: VideoAdapter

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
        val binding = FragmentVideosBinding.bind(view)


        binding.apply {
            videoRecyclerView.apply {
                setHasFixedSize(true)
                setItemViewCacheSize(10)
                layoutManager = LinearLayoutManager(requireContext())
            }
        }
        // initializing and assigning adapter
        adapter = VideoAdapter(requireContext(),videoList)
        binding.videoRecyclerView.adapter = adapter

        return view
    }


    // for search view
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu,menu)
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
}