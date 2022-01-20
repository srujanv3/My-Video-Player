package com.blogspot.svdevs.videoplayer.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.blogspot.svdevs.videoplayer.R
import com.blogspot.svdevs.videoplayer.databinding.ActivityAboutBinding
import com.blogspot.svdevs.videoplayer.utils.showToast

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "About"

        binding.aboutText.text = " Video Player Application \n"+
                "\n" +" Developer: Srujan V \n" +
                "\n" + " Date: 3rd January 2022"

    }
}