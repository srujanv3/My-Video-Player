package com.blogspot.svdevs.videoplayer.ui.activities

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.blogspot.svdevs.videoplayer.R
import com.blogspot.svdevs.videoplayer.data.Folder
import com.blogspot.svdevs.videoplayer.data.Video
import com.blogspot.svdevs.videoplayer.databinding.ActivityMainBinding
import com.blogspot.svdevs.videoplayer.ui.folder.FoldersFragment
import com.blogspot.svdevs.videoplayer.ui.video.VideosFragment
import com.blogspot.svdevs.videoplayer.utils.Constants.REQUEST_CODE
import com.blogspot.svdevs.videoplayer.utils.showToast
import java.io.File
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle

    companion object {
        lateinit var videoList:ArrayList<Video>
        lateinit var folderList: ArrayList<Folder>
        lateinit var searchList: ArrayList<Video>
        var search:Boolean = false // check if is searching
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //setup Nav Drawer
        toggle = ActionBarDrawerToggle(this, binding.root, R.string.open, R.string.close)
        binding.root.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

       if(requestPermissions()) {
           folderList = ArrayList()
           videoList = getAllVideos()
           setupFragment(VideosFragment())
       }

        binding.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.allVideos -> setupFragment(VideosFragment())
                R.id.allFolders -> setupFragment(FoldersFragment())
            }
            return@setOnItemSelectedListener true
        }

        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.themes -> showToast("Feedback")
                R.id.share -> showToast("Share")
                R.id.about -> startActivity(Intent(this,AboutActivity::class.java))
            }

            return@setNavigationItemSelectedListener true
        }

    }

    private fun setupFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frameLayout, fragment)
        transaction.disallowAddToBackStack()
        transaction.commit()
    }

    // Request for permissions
    private fun requestPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE), REQUEST_CODE)
            return false
        }
        return true
    }

    // Checks if permissions are granted or not
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showToast("Permission Granted")
            folderList = ArrayList()
            videoList = getAllVideos()
            setupFragment(VideosFragment())
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE), REQUEST_CODE)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("Recycle")
    private fun getAllVideos(): ArrayList<Video> {
        val list = ArrayList<Video>()
        val tempFolderList = ArrayList<String>()

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

        val cursor = this.contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,projection,
            null,null, MediaStore.Video.Media.DATE_ADDED+" DESC")

        if(cursor!=null){
            if(cursor.moveToNext()) {
                do {
                    val titleC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE))
                    val idC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID))
                    val folderC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME))
                    val folderIdC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID))
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

                        //getting folders
                        if(!tempFolderList.contains(folderC)){
                            tempFolderList.add(folderC)
                            folderList.add(Folder(id = folderIdC,folderName = folderC))
                        }

                    }catch (e:Exception){}
                }while (cursor.moveToNext())
                cursor.close()
            }
        }

        return list
    }
}