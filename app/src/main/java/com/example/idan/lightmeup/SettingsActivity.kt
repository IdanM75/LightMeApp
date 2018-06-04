package com.example.idan.lightmeup

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import com.beust.klaxon.Klaxon
import net.rimoto.intlphoneinput.IntlPhoneInput
import java.io.File



class SettingsActivity : AppCompatActivity() {

    lateinit var myPhone : String
    var isRunInBackground : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val tx = findViewById(R.id.headline) as TextView

        val custom_font = Typeface.createFromAsset(assets, "fonts/font1.ttf")
        tx.typeface = custom_font

        val file = File(applicationContext.filesDir, "conf.txt")
        val confText = file.readText()
        class ConfData(var is_run_in_background: Boolean)
        val confObj = Klaxon().parse<ConfData>(confText)

        val backgroundView = findViewById<Switch>(R.id.backgroundButton)

        if (confObj != null) {
            backgroundView.setChecked(confObj.is_run_in_background)
            isRunInBackground = confObj.is_run_in_background
        }

        backgroundView.setOnCheckedChangeListener { buttonView, isChecked ->
            confObj!!.is_run_in_background = isChecked
            file.printWriter().use { out ->
                out.print(Klaxon().toJsonString(confObj))
            }
            isRunInBackground = isChecked
        }


        val phoneFile = File(applicationContext.filesDir, "phone.txt")
        val phoneText = phoneFile.readText()
        class phoneData(var phoneNumI: String)
        val phoneObj = Klaxon().parse<phoneData>(phoneText)
        myPhone = phoneObj!!.phoneNumI

        val layPhone = findViewById<ConstraintLayout>(R.id.constraintLayout2)
        val finalPhoneb = findViewById<Button>(R.id.finalPhone)
        val myPhoneNumber = findViewById<Button>(R.id.myPhoneNumber)
        val phoneInputView = findViewById(R.id.my_phone_input) as IntlPhoneInput
        myPhoneNumber.text = "+" + phoneObj!!.phoneNumI

        myPhoneNumber.setOnClickListener {
            phoneInputView.number = phoneObj.phoneNumI
            layPhone.visibility = View.VISIBLE
        }

        finalPhoneb.setOnClickListener {
            if (phoneInputView.isValid) {
                val myInternationalNumber = phoneInputView.number
                val parsed = myInternationalNumber.substring(1)
                phoneObj.phoneNumI = parsed
                phoneFile.printWriter().use { out ->
                    out.print(Klaxon().toJsonString(phoneObj))
                }
                myPhone = parsed
                myPhoneNumber.text = "+" + parsed
            }
            else {
                Toast.makeText(this, "The number is not valid", Toast.LENGTH_SHORT).show()
            }
            layPhone.visibility = View.INVISIBLE
        }
    }

    override fun onBackPressed() {
        val resultIntent = Intent()
// TODO Add extras or a data URI to this intent as appropriate.
        resultIntent.putExtra("myPhone", myPhone)
        resultIntent.putExtra("isRunInBackground", isRunInBackground)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}
