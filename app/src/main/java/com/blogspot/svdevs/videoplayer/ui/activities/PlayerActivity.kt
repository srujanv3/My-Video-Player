package com.blogspot.svdevs.videoplayer.ui.activities

import android.app.AppOpsManager
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.ColorDrawable
import android.media.audiofx.LoudnessEnhancer
import android.net.Uri
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
import com.blogspot.svdevs.videoplayer.databinding.BoosterLayoutBinding
import com.blogspot.svdevs.videoplayer.databinding.MoreFeaturesBinding
import com.blogspot.svdevs.videoplayer.databinding.SpeedDialogBinding
import com.blogspot.svdevs.videoplayer.ui.folder.FoldersActivity
import com.blogspot.svdevs.videoplayer.utils.DoubleClickListener
import com.blogspot.svdevs.videoplayer.utils.showToast
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.system.exitProcess

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private lateinit var runnable: Runnable
    private var isSubtitle: Boolean = true
    private var doubleTapIconTime: Int = 0

    companion object {
        lateinit var playerList: ArrayList<Video>
        var pos: Int = -1
        private lateinit var player: ExoPlayer
        var repeat: Boolean = false
        private var isFullScreen: Boolean = false
        private var isLocked:Boolean = false
        lateinit var trackSelector: DefaultTrackSelector

        //for sleep timer
        private var timer: Timer? = null

        // for audio booster
        private lateinit var audioEnhancer: LoudnessEnhancer

        // for playback speed
        private var speed: Float = 1.0f

        // for pip mode (play different video)
        var pipStatus: Int = 0

        // for now playing video
        var nowPlayingID: String = ""
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

        // double click events
        binding.forwardFrame.setOnClickListener(DoubleClickListener(callback = object : DoubleClickListener.Callback{
            override fun doubleClicked() {
                binding.playerView.showController()
                binding.forwardBtn.visibility = View.VISIBLE
                player.seekTo(player.currentPosition + 10000)
                doubleTapIconTime = 0
            }
        }))

        binding.rewindFrame.setOnClickListener(DoubleClickListener(callback = object : DoubleClickListener.Callback{
            override fun doubleClicked() {
                binding.playerView.showController()
                binding.rewindBtn.visibility = View.VISIBLE
                player.seekTo(player.currentPosition - 10000)
                doubleTapIconTime = 0
            }
        }))

    }


    // checking if the video is playing from all videos or the folders activity
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
            "SearchVideos" -> {
                playerList = ArrayList()
                playerList.addAll(MainActivity.searchList)
                playVideo()
            }
            "NowPlaying" -> {
                startPlayer()
                speed = 1.0f
                binding.videoTitle.text = playerList[pos].title
                binding.videoTitle.isSelected = true
                binding.playerView.player = player
                fullScreenMode(enabled = isFullScreen)
                buttonsVisibility()

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

        //lock button functionality
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
                .setOnCancelListener { startPlayer() }
                .setBackground(ColorDrawable(0x8003DAC5.toInt()))
                .create()

            dialog.show()

            // handling audio button
            bindingMF.audioBtn.setOnClickListener {
                dialog.dismiss()
               startPlayer()

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
                    .setOnCancelListener { startPlayer() }
                    .setBackground(ColorDrawable(0x8003DAC5.toInt()))
                    .setItems(tempTrack){_,pos ->
                        showToast("${audioTracks[pos]} Selected")
                        trackSelector.setParameters(trackSelector.buildUponParameters().setPreferredAudioLanguage(audioTracks[pos]))
                    }
                    .create()
                    .show()
            }

            // handling subtitles button
            bindingMF.subtitlesBtn.setOnClickListener {
                if (isSubtitle) {
                    // turining off the subtitles
                    trackSelector.parameters = DefaultTrackSelector.ParametersBuilder(this).
                    setRendererDisabled(C.TRACK_TYPE_VIDEO,true).build()
                    isSubtitle = false
                    showToast("Subtitles OFF")
                }else {
                    // turining on the subtitles
                    trackSelector.parameters = DefaultTrackSelector.ParametersBuilder(this).
                    setRendererDisabled(C.TRACK_TYPE_VIDEO,false).build()
                    isSubtitle = true
                    showToast("Subtitles ON")
                }
                dialog.dismiss()
                startPlayer()
            }

            //handling booster button functionality
            bindingMF.boosterBtn.setOnClickListener {
                dialog.dismiss()
                val boosterDialog = LayoutInflater.from(this).inflate(R.layout.booster_layout,binding.root,false)
                val bindingB = BoosterLayoutBinding.bind(boosterDialog)
                val dialogBooster = MaterialAlertDialogBuilder(this).setView(boosterDialog)
                    .setOnCancelListener { startPlayer() }
                    .setPositiveButton("OK") {self, _ ->
                        audioEnhancer.setTargetGain(bindingB.verticalSeekBar.progress * 100)
                        startPlayer()
                        self.dismiss()
                    }
                    .setBackground(ColorDrawable(0x8003DAC5.toInt()))
                    .create()

                dialogBooster.show()

                // Amplifying the audio
                bindingB.verticalSeekBar.progress = audioEnhancer.targetGain.toInt()/100
                bindingB.pgText.text = "Audio Boost \n\n ${audioEnhancer.targetGain.toInt()/10} %"
                bindingB.verticalSeekBar.setOnProgressChangeListener {
                    bindingB.pgText.text = "Audio Boost \n\n ${it * 10} %"
                }

                //startPlayer()
            }

            // handling play back speed button
            bindingMF.speedBtn.setOnClickListener {
                startPlayer()
                dialog.dismiss()
                val speedDialog = LayoutInflater.from(this).inflate(R.layout.speed_dialog,binding.root,false)
                val bindingS = SpeedDialogBinding.bind(speedDialog)
                val dialogS = MaterialAlertDialogBuilder(this).setView(speedDialog)
                    .setCancelable(false)
                    .setPositiveButton("OK") {self, _ ->
                        self.dismiss()
                    }
                    .setBackground(ColorDrawable(0x8003DAC5.toInt()))
                    .create()

                dialogS.show()

                bindingS.speedText.text = "${DecimalFormat("#.##").format(speed)} X"
                bindingS.minusBtn.setOnClickListener {
                    changePlaybackSpeed(isIncrement = false)
                    bindingS.speedText.text = "${DecimalFormat("#.##").format(speed)} X"
                }
                bindingS.plusBtn.setOnClickListener {
                    changePlaybackSpeed(isIncrement = true)
                    bindingS.speedText.text = "${DecimalFormat("#.##").format(speed)} X"
                }
            }

            // handling sleep timer button
            bindingMF.sleepBtn.setOnClickListener {
                dialog.dismiss()
                if(timer != null) {
                    showToast("Timer already active")
                }else {
                    var sleepTime = 15
                    val speedDialog = LayoutInflater.from(this).inflate(R.layout.speed_dialog,binding.root,false)
                    val bindingS = SpeedDialogBinding.bind(speedDialog)
                    val dialogS = MaterialAlertDialogBuilder(this).setView(speedDialog)
                        .setCancelable(false)
                        .setPositiveButton("OK") {self, _ ->
                            timer = Timer()
                            val task = object : TimerTask() {
                                override fun run() {
                                    moveTaskToBack(true) // prevents app from restarting again
                                    exitProcess(1)
                                }
                            }
                            timer!!.schedule(task,sleepTime*60*1000.toLong())
                            self.dismiss()
                            startPlayer()
                        }
                        .setBackground(ColorDrawable(0x8003DAC5.toInt()))
                        .create()

                    dialogS.show()

                    bindingS.speedText.text = "$sleepTime min"
                    bindingS.minusBtn.setOnClickListener {
                        if(sleepTime > 15){
                            sleepTime -= 15
                        }
                        bindingS.speedText.text ="$sleepTime min"
                    }
                    bindingS.plusBtn.setOnClickListener {
                        if(sleepTime < 120) {
                            sleepTime += 15
                        }
                        bindingS.speedText.text = "$sleepTime min"
                    }
                }
            }

            // handling pip button
            bindingMF.pipBtn.setOnClickListener {
                // checking for pip permissions
                val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
                val status = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // permission granted
                    appOps.checkOpNoThrow(AppOpsManager.OPSTR_PICTURE_IN_PICTURE, android.os.Process.myUid(), packageName) ==
                            AppOpsManager.MODE_ALLOWED
                } else {
                    false
                }

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // if permissions granted...
                    if (status) {
                        this.enterPictureInPictureMode(PictureInPictureParams.Builder().build())
                        dialog.dismiss()
                        binding.playerView.hideController()
                        startPlayer()
                        pipStatus = 0
                    }else {
                        // requesting for the permissions
                        val intent = Intent("android.settings.PICTURE_IN_PICTURE_SETTINGS",
                            Uri.parse("package:$packageName"))
                        startActivity(intent)
                    }
                }else {
                    showToast("Feature not supported")
                    dialog.dismiss()
                    startPlayer()
                }
            }
        }

    }

    // Play the video in fullScreen mode
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

    // Resume playing the video
    private fun startPlayer() {
        binding.playBtn.setImageResource(R.drawable.pause)
        player.play()

        // for hiding the status bar and phone controls
        WindowCompat.setDecorFitsSystemWindows(window,false)
        WindowInsetsControllerCompat(window,binding.root).let {
            it.hide(WindowInsetsCompat.Type.systemBars())
            it.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    // Pause the video
    private fun pausePlayer() {
        binding.playBtn.setImageResource(R.drawable.play)
        player.pause()
    }

    // Create the player and start playing the video
    private fun playVideo() {
        try {
            player.release()
        }catch (e:Exception) {  }
        // setup default playback speed
        speed = 1.0f

        trackSelector = DefaultTrackSelector(this)

        binding.videoTitle.text = playerList[pos].title
        binding.videoTitle.isSelected = true

        player = ExoPlayer.Builder(this).setTrackSelector(trackSelector).build()
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
        audioEnhancer = LoudnessEnhancer(player.audioSessionId)
        audioEnhancer.enabled = true
        nowPlayingID = playerList[pos].id
    }

    // hiding the control buttons when video is playing
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

    // lock button functionality
    private fun changeVisibility(visibility: Int) {
        binding.bottomController.visibility = visibility
        binding.topController.visibility = visibility
        binding.playBtn.visibility = visibility

        if(isLocked) {
            binding.lockBtn.visibility = View.VISIBLE
        }else {
            binding.lockBtn.visibility = visibility
        }

        // displaying the doubletap buttons for bit longer time
        if(doubleTapIconTime == 2) {
            // == 2 will hide the icons after 2 iterations
            binding.rewindBtn.visibility = View.GONE
            binding.forwardBtn.visibility = View.GONE
        } else {
            doubleTapIconTime ++
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

    // playback speed functionality
    private fun changePlaybackSpeed(isIncrement: Boolean) {
        if(isIncrement) {
            if(speed <= 1.9f) {
                speed += 0.10f
            }
        }else {
            if(speed > 0.20f) {
                speed -= 0.10f
            }

        }
        player.setPlaybackSpeed(speed)
    }

    // playing new video when pip mode is already active
    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration?) {

        if (pipStatus != 0) {
            finish() // close previous pip mode
            val intent = Intent(this, PlayerActivity::class.java)
            when(pipStatus) {
                1 -> intent.putExtra("class","FoldersActivity")
                2 -> intent.putExtra("class","SearchVideos")
                3 -> intent.putExtra("class","AllVideos")
            }
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player.pause()
    }
}