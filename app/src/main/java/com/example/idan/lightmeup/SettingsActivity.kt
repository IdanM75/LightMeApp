package com.example.idan.lightmeup

import android.graphics.Typeface
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.widget.Switch
import android.widget.TextView
import com.beust.klaxon.Klaxon
import java.io.File

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val tx = findViewById(R.id.headline) as TextView

        val custom_font = Typeface.createFromAsset(assets, "fonts/font1.ttf")
        tx.typeface = custom_font

        println("dddddddddd")
        println(applicationContext.filesDir)
        val file = File(applicationContext.filesDir, "conf.txt")
        val confText = file.readText()
        class ConfData(var is_run_in_background: Boolean)
        val confObj = Klaxon().parse<ConfData>(confText)

        val backgroundView = findViewById<Switch>(R.id.backgroundButton)

        if (confObj != null) {
            backgroundView.setChecked(confObj.is_run_in_background)
        }

        backgroundView.setOnCheckedChangeListener { buttonView, isChecked ->
            confObj!!.is_run_in_background = isChecked
            file.printWriter().use { out ->
                out.print(Klaxon().toJsonString(confObj))
            }
        }

        val settingsLayout = findViewById<ConstraintLayout>(R.id.settingsLayout)



    }
}
