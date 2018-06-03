package com.example.idan.lightmeup

import android.content.Intent
import android.graphics.Typeface
import android.media.AudioManager.AUDIOFOCUS_NONE
import android.media.MediaPlayer.OnPreparedListener
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.beust.klaxon.Klaxon
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.GoogleApiClient
import java.io.File


class MainActivity : AppCompatActivity() {

    val RC_SIGN_IN: Int = 9000
    var phoneNum: String = "-2"


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        if (getIntent().getBooleanExtra("EXIT", false)) {
            finish()
        }

        startBackgroundVideo()

        val tx = findViewById(R.id.headline) as TextView
        val custom_font = Typeface.createFromAsset(assets, "fonts/font1.ttf")
        tx.typeface = custom_font

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()

        val mGoogleApiClient = GoogleApiClient.Builder(this)
//                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()

        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val SignInButton = findViewById<SignInButton>(R.id.signInButton)
        if (progressBar != null) {

            SignInButton?.setOnClickListener {
                val visibility = if (progressBar.visibility == View.GONE) View.VISIBLE else View.GONE
                progressBar.visibility = visibility
                val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
                startActivityForResult(signInIntent, RC_SIGN_IN)
            }
        }
        else {
            SignInButton?.setOnClickListener{
                val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
                startActivityForResult(signInIntent, RC_SIGN_IN)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val googleResult: GoogleSignInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (googleResult.isSuccess) {
                val googleAccount: GoogleSignInAccount = googleResult.signInAccount!!

                val file = File(applicationContext.filesDir, "phone.txt")
                if (!file.exists()) {
                    file.createNewFile()
                }
                val phoneText = file.readText()
                if (phoneText.length == 0){
                    file.printWriter().use { out ->
                        out.print("{\"is_run_in_background\":\"false\"}")
                    }
//                    isRunInBackground = false
                }
                else {
                    class phoneData(var phoneNumI: String)
                    val confObj = Klaxon().parse<phoneData>(phoneText)
                    if (confObj != null) {
                        phoneNum = confObj.phoneNumI
                    }
                }

                phoneNum = "972525787016"
                val intent = Intent(this, MapsActivity::class.java)
                intent.putExtra("googleAccount", googleAccount)
                intent.putExtra("phoneNum", phoneNum)
                startActivity(intent)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startBackgroundVideo() {
        val videoView = findViewById<ScaleableVideoView>(R.id.scaleableVideoView)
        val uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.light_slow)
        videoView.setVideoURI(uri)

        videoView.setAudioFocusRequest(AUDIOFOCUS_NONE)

        videoView.setOnPreparedListener(OnPreparedListener {
            mp -> mp.isLooping = true
            videoView.start()
        })
    }
}