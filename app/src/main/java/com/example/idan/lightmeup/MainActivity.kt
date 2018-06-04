package com.example.idan.lightmeup

import android.content.Intent
import android.graphics.Typeface
import android.media.AudioManager.AUDIOFOCUS_NONE
import android.media.MediaPlayer.OnPreparedListener
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.beust.klaxon.Klaxon
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.GoogleApiClient
import net.rimoto.intlphoneinput.IntlPhoneInput
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
        val signInButtonb = findViewById<SignInButton>(R.id.signInButtonb)
        val layPhone = findViewById<ConstraintLayout>(R.id.constraintLayout2)
        val finalPhoneb = findViewById<Button>(R.id.finalPhone)

        signInButtonb.isEnabled = false

        if (progressBar != null) {

            signInButtonb?.setOnClickListener {
                val visibility = if (progressBar.visibility == View.GONE) View.VISIBLE else View.GONE
                progressBar.visibility = visibility
                val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
                startActivityForResult(signInIntent, RC_SIGN_IN)
            }
        }
        else {
            signInButtonb?.setOnClickListener{
                val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
                startActivityForResult(signInIntent, RC_SIGN_IN)
            }
        }

        val file = File(applicationContext.filesDir, "phone.txt")
        if (!file.exists()) {
            file.createNewFile()
        }
        val phoneText = file.readText()
        if (phoneText.length == 0){
            layPhone.visibility = View.VISIBLE
            val phoneInputView = findViewById(R.id.my_phone_input) as IntlPhoneInput
            finalPhoneb.setOnClickListener {
                if (phoneInputView.isValid) {
                    val myInternationalNumber = phoneInputView.number
                    val parsed = myInternationalNumber.substring(1)
                    file.printWriter().use { out ->
                        out.print("{\"phoneNumI\":\"${parsed}\"}")
                    }
                    phoneNum = parsed
                    layPhone.visibility = View.INVISIBLE
                    signInButtonb.isEnabled = true
                }
                else {
                    Toast.makeText(this, "The number is not valid", Toast.LENGTH_SHORT).show()
                }
            }
        }
        else {
            class phoneData(var phoneNumI: String)
            val phoneObj = Klaxon().parse<phoneData>(phoneText)
            if (phoneObj != null) {
                phoneNum = phoneObj.phoneNumI
            }
            signInButtonb.isEnabled = true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val googleResult: GoogleSignInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (googleResult.isSuccess) {
                val googleAccount: GoogleSignInAccount = googleResult.signInAccount!!

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