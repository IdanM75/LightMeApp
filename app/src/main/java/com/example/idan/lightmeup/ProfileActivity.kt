package com.example.idan.lightmeup

import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import java.net.URL


class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val googleAccount: GoogleSignInAccount = intent.getParcelableExtra("googleAccount")

        val custom_font = Typeface.createFromAsset(assets, "fonts/font1.ttf")

        val profileImage = findViewById<ImageView>(R.id.profileImage)
        val name = findViewById<TextView>(R.id.name)
        val email = findViewById<TextView>(R.id.email)

        name.typeface = custom_font

        if (googleAccount.photoUrl != null) {
            val url = URL(googleAccount.photoUrl.toString())
            val bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream())
            profileImage.setImageBitmap(bmp)
        }


        name.setText(googleAccount.displayName)
        email.setText(googleAccount.email)

    }

}
