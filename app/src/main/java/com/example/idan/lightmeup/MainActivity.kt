package com.example.idan.lightmeup

import android.content.Intent
import android.media.MediaPlayer.OnPreparedListener
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ProgressBar
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.GoogleApiClient


class MainActivity : AppCompatActivity() {

    val RC_SIGN_IN: Int = 9000


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (getIntent().getBooleanExtra("EXIT", false)) {
            finish();
        }

        startBackgroundVideo()

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
                val account: GoogleSignInAccount = googleResult.signInAccount!!
                val intent = Intent(this, MapsActivity::class.java)
//                intent.putExtra("accountId", account.id)
                intent.putExtra("accountDisplayName", account.displayName)
                startActivity(intent)
            }
        }
    }

    private fun startBackgroundVideo() {
        val videoView = findViewById<ScaleableVideoView>(R.id.scaleableVideoView)
        val uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.light_slow)
        videoView.setVideoURI(uri)
        videoView.start()
        videoView.setOnPreparedListener(OnPreparedListener { mp -> mp.isLooping = true })
    }
}