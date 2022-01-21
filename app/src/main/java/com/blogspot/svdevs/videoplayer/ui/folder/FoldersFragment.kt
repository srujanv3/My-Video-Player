package com.blogspot.svdevs.videoplayer.ui.folder

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.blogspot.svdevs.videoplayer.R
import com.blogspot.svdevs.videoplayer.databinding.FragmentFoldersBinding
import com.blogspot.svdevs.videoplayer.ui.activities.MainActivity.Companion.folderList

class FoldersFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_folders, container, false)
        val binding = FragmentFoldersBinding.bind(view)

        // temporary list (Ignore)
        val tempList = ArrayList<String>()
        tempList.add("Downloads")
        tempList.add("Pictures")
        tempList.add("Android")
        tempList.add("DCIM")
        tempList.add("Video Downloader")

        binding.apply {
            foldersRecyclerView.apply {
                setHasFixedSize(true)
                setItemViewCacheSize(10)
                layoutManager = LinearLayoutManager(requireContext())
                adapter = FolderAdapter(requireContext(), folderList)
            }
        }

        return view
    }
}