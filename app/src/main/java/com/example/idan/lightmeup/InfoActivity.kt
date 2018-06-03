package com.example.idan.lightmeup

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import com.google.android.gms.auth.api.signin.GoogleSignInAccount



class InfoActivity : AppCompatActivity() {

    lateinit var googleAccount: GoogleSignInAccount

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        googleAccount = intent.getParcelableExtra("googleAccount")

        findViewById<Button>(R.id.contactUsButton).setOnClickListener(View.OnClickListener {
            val mailClient = Intent(Intent.ACTION_SENDTO)
            val mailto = "mailto:lightmeupapp@gmail.com"
            mailClient.data = Uri.parse(mailto)
            startActivity(mailClient)
        })
    }
}
