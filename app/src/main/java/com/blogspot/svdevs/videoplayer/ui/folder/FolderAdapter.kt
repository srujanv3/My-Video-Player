package com.blogspot.svdevs.videoplayer.ui.folder

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.blogspot.svdevs.videoplayer.data.Folder
import com.blogspot.svdevs.videoplayer.databinding.FolderItemBinding

class FolderAdapter(private val context: Context, private val list: ArrayList<Folder>) :
    RecyclerView.Adapter<FolderAdapter.FoldersViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoldersViewHolder {
        return FoldersViewHolder(
            FolderItemBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: FoldersViewHolder, position: Int) {

        holder.folderName.text = list[position].folderName

        // handle click events
        holder.root.setOnClickListener {
            val intent = Intent(context, FoldersActivity::class.java)
            intent.putExtra("position",position)
            ContextCompat.startActivity(context,intent,null)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class FoldersViewHolder(binding: FolderItemBinding) : RecyclerView.ViewHolder(binding.root) {

        val folderName = binding.folderName
        val root = binding.root
    }

}