package com.example.idan.lightmeup

import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import java.net.URL


class ProfileActivity : AppCompatActivity() {

    lateinit var googleAccount: GoogleSignInAccount
    var lightGive: Int = -1
    var lightGet: Int = -1
    var lightGift: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        googleAccount = intent.getParcelableExtra("googleAccount")

        val profileImage = findViewById<ImageView>(R.id.profileImage)
        val dName = findViewById<TextView>(R.id.dName)
        val email = findViewById<TextView>(R.id.email)

        if (googleAccount.photoUrl != null) {
            val url = URL(googleAccount.photoUrl.toString())
            val bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream())
            profileImage.setImageBitmap(bmp)
        }

        dName.text = googleAccount.displayName
        email.text = googleAccount.email

        val getLightView = findViewById<TextView>(R.id.getLightView)
        val giveLightView = findViewById<TextView>(R.id.giveLightView)
        val giftLightView = findViewById<TextView>(R.id.giftLightView)

        getLightView.text = "Lights You Get: " + lightGive.toString()
        giveLightView.text = "Lights You Give: " + lightGet.toString()
        giftLightView.text = "Gifted Lights: " + lightGift.toString()
    }
}
