package com.blogspot.svdevs.videoplayer.ui.folder

import android.annotation.SuppressLint
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.blogspot.svdevs.videoplayer.data.Video
import com.blogspot.svdevs.videoplayer.databinding.ActivityFoldersBinding
import com.blogspot.svdevs.videoplayer.ui.MainActivity
import com.blogspot.svdevs.videoplayer.ui.MainActivity.Companion.folderList
import com.blogspot.svdevs.videoplayer.ui.video.VideoAdapter
import java.io.File
import java.lang.Exception

class FoldersActivity : AppCompatActivity() {

    companion object {
        lateinit var folderVideoList : ArrayList<Video>
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityFoldersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pos = intent.getIntExtra("position",0)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = folderList[pos].folderName
        folderVideoList = getAllVideos(MainActivity.folderList[pos].id)

//        val tempList = ArrayList<Video>()
//        tempList.add(videoList[0])
//        tempList.add(videoList[1])
//        tempList.add(videoList[2])
//        tempList.add(videoList[3])

        binding.apply {
            videoRecyclerViewFolders.apply {
                setHasFixedSize(true)
                setItemViewCacheSize(10)
                layoutManager = LinearLayoutManager(this@FoldersActivity)
                adapter = VideoAdapter(this@FoldersActivity, folderVideoList,isFolder = true)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }

    @SuppressLint("Recycle")
    private fun getAllVideos(id: String): ArrayList<Video> {
        val list = ArrayList<Video>()
        val selection = MediaStore.Video.Media.BUCKET_ID + " like? "

        val projection = arrayOf(
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.BUCKET_ID
        )

        val cursor = this.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,projection,
            selection, arrayOf(id), MediaStore.Video.Media.DATE_ADDED+" DESC")

        if(cursor!=null){
            if(cursor.moveToNext()) {
                do {
                    val titleC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE))
                    val idC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID))
                    val folderC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME))
                    val sizeC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE))
                    val pathC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
                    //val durationC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)).toLong()

                    try {
                        val file = File(pathC)
                        val videoImage = Uri.fromFile(file)
                        val video = Video(idC,titleC,pathC,sizeC,videoImage)
                        if(file.exists()){
                            list.add(video)
                        }

                    }catch (e: Exception){}
                }while (cursor.moveToNext())
                cursor.close()
            }
        }

        return list
    }
}