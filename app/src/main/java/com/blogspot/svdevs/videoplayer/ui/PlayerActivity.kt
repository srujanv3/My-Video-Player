package com.blogspot.svdevs.videoplayer.ui

import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.blogspot.svdevs.videoplayer.R
import com.blogspot.svdevs.videoplayer.data.Video
import com.blogspot.svdevs.videoplayer.databinding.ActivityPlayerBinding
import com.blogspot.svdevs.videoplayer.databinding.MoreFeaturesBinding
import com.blogspot.svdevs.videoplayer.ui.folder.FoldersActivity
import com.blogspot.svdevs.videoplayer.utils.showToast
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.*
import kotlin.collections.ArrayList

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private lateinit var runnable: Runnable

    companion object {
        lateinit var playerList: ArrayList<Video>
        var pos: Int = -1
        private lateinit var player: SimpleExoPlayer
        var repeat: Boolean = false
        private var isFullScreen: Boolean = false
        private var isLocked:Boolean = false
        lateinit var trackSelector: DefaultTrackSelector
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
//        window.setFlags(
//            WindowManager.LayoutParams.FLAG_FULLSCREEN,
//            WindowManager.LayoutParams.FLAG_FULLSCREEN
//        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Fullscreen mode video display
        WindowCompat.setDecorFitsSystemWindows(window,false)
        WindowInsetsControllerCompat(window,binding.root).let {
            it.hide(WindowInsetsCompat.Type.systemBars())
            it.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        initializeLayout()
        initBinding()

    }


    private fun initializeLayout() {

        when (intent.getStringExtra("class")) {
            "AllVideos" -> {
                playerList = ArrayList()
                playerList.addAll(MainActivity.videoList)
                playVideo()
            }
            "FoldersActivity" -> {
                playerList = ArrayList()
                playerList.addAll(FoldersActivity.folderVideoList)
                playVideo()
            }
        }
    }

    private fun initBinding() {

        binding.repeatBtn.setOnClickListener {
            if(repeat) {
                repeat = false
                player.repeatMode = Player.REPEAT_MODE_OFF
                binding.repeatBtn.setImageResource(R.drawable.exo_controls_repeat_off)
            }else {
                repeat = true
                player.repeatMode = Player.REPEAT_MODE_OFF
                binding.repeatBtn.setImageResource(R.drawable.exo_controls_repeat_all)
            }
        }

        binding.fullscreenBtn.setOnClickListener {
            if(isFullScreen) {
                isFullScreen = false
                fullScreenMode(enabled = false)
            }else {
                isFullScreen = true
                fullScreenMode(enabled = true)
            }
        }

        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.playBtn.setOnClickListener {
            if (player.isPlaying) {
                pausePlayer()
            } else {
                startPlayer()
            }
        }

        binding.lockBtn.setOnClickListener {
            if(!isLocked) {
                isLocked = true
                binding.playerView.hideController()
                binding.lockBtn.setImageResource(R.drawable.unlock)
                binding.playerView.useController = false
            }else {
                isLocked = false
                binding.playerView.useController = true
                binding.playerView.showController()
                binding.lockBtn.setImageResource(R.drawable.lock)



            }
        }

        binding.nextBtn.setOnClickListener { nextOrPreviousVideo() }
        binding.prevBtn.setOnClickListener { nextOrPreviousVideo(false) }

        binding.moreBtn.setOnClickListener {
            pausePlayer()
            // display the custom dialog
            val customDialog = LayoutInflater.from(this).inflate(R.layout.more_features,binding.root,false)
            val bindingMF = MoreFeaturesBinding.bind(customDialog)
            val dialog = MaterialAlertDialogBuilder(this).setView(customDialog)
                .setOnCancelListener { playVideo() }
                .setBackground(ColorDrawable(0x8003DAC5.toInt()))
                .create()

            dialog.show()

            bindingMF.audioBtn.setOnClickListener {
                dialog.dismiss()
                //playVideo()

                val audioTracks = ArrayList<String>()
                for(i in 0 until player.currentTrackGroups.length) {
                    if(player.currentTrackGroups.get(i).getFormat(0).selectionFlags ==
                            C.SELECTION_FLAG_DEFAULT) {
                        audioTracks.add(Locale(player.currentTrackGroups.get(i).getFormat(0).language.toString()).displayLanguage)
                    }
                }

                // convert array list to array of char sequences
                val tempTrack = audioTracks.toArray(arrayOfNulls<CharSequence>(audioTracks.size))

                MaterialAlertDialogBuilder(this,R.style.alertDialog)
                    .setTitle("Select track")
                    .setOnCancelListener { playVideo() }
                    .setBackground(ColorDrawable(0x8003DAC5.toInt()))
                    .setItems(tempTrack){_,pos ->
                        showToast("${audioTracks[pos]} Selected")
                        trackSelector.setParameters(trackSelector.buildUponParameters().setPreferredAudioLanguage(audioTracks[pos]))
                    }
                    .create()
                    .show()
            }
        }

    }

    private fun fullScreenMode(enabled: Boolean) {
        if(enabled) {
            binding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
            player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            binding.fullscreenBtn.setImageResource(R.drawable.fullscreen_exit)
        }else {
            binding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
            binding.fullscreenBtn.setImageResource(R.drawable.fullscreen)
        }
    }

    private fun startPlayer() {
        binding.playBtn.setImageResource(R.drawable.pause)
        player.play()
    }

    private fun pausePlayer() {
        binding.playBtn.setImageResource(R.drawable.play)
        player.pause()
    }

    private fun playVideo() {
        try {
            player.release()
        }catch (e:Exception) {  }
        trackSelector = DefaultTrackSelector(this)

        binding.videoTitle.text = playerList[pos].title
        binding.videoTitle.isSelected = true

        player = SimpleExoPlayer.Builder(this).setTrackSelector(trackSelector).build()
        binding.playerView.player = player

        val mediaItem = MediaItem.fromUri(playerList[pos].icon)
        player.setMediaItem(mediaItem)
        player.prepare()

        startPlayer()

        player.addListener(object: Player.Listener{
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if(playbackState == Player.STATE_ENDED) {
                    nextOrPreviousVideo()
                }
            }
        })

        fullScreenMode(enabled = isFullScreen)
        buttonsVisibility()
    }

    private fun buttonsVisibility() {
        runnable = Runnable {
            if(binding.playerView.isControllerVisible) {
                changeVisibility(View.VISIBLE)
            }else {
                changeVisibility(View.INVISIBLE)
            }
            // Time interval to check the looper
            Handler(Looper.getMainLooper()).postDelayed(runnable,300)
        }
        //Handler start time
        Handler(Looper.getMainLooper()).postDelayed(runnable,0)
    }
    private fun changeVisibility(visibility: Int) {
        binding.bottomController.visibility = visibility
        binding.topController.visibility = visibility
        binding.playBtn.visibility = visibility

        if(isLocked) {
            binding.lockBtn.visibility = View.VISIBLE
        }else {
            binding.lockBtn.visibility = visibility
        }
    }

    // next and previous video switch functionality
    private fun nextOrPreviousVideo(isNext: Boolean = true) {

        if (isNext) {
            setPosition()
        } else {
            setPosition(false)
        }
        playVideo()
    }

    // handling position bounds while switching next or previous videos
    private fun setPosition(isIncrement: Boolean = true) {
        // checking if repeat is on while user presses next or previous button
       if(!repeat) {
           if (isIncrement) {
               if (playerList.size - 1 == pos) {
                   pos = 0
               } else {
                   ++pos
               }
           } else {
               if (pos == 0) {
                   pos = playerList.size - 1
               } else {
                   --pos
               }
           }
       }
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}