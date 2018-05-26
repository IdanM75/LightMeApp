package com.example.idan.lightmeup

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import com.arsy.maps_library.MapRadar
import com.beust.klaxon.Klaxon
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import okhttp3.*
import java.io.IOException


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    override fun onMarkerClick(p0: Marker?) = false

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var backPressed: Long = 0
    lateinit var mAdView : AdView
    val ipAdress: String = "192.168.1.40"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val accountDisplayName: String = intent.getStringExtra("accountDisplayName")
        findViewById<TextView>(R.id.mapTextView).setText("Let's get a light $accountDisplayName")

        val clickListener = View.OnClickListener { view ->
            when (view.id) {
                R.id.buttonMenu -> {
                    showPopup(view)
                }
            }
        }
        findViewById<ImageButton>(R.id.buttonMenu).setOnClickListener(clickListener)

        MobileAds.initialize(this, "ca-app-pub-3096868502930398~4354694161")
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private lateinit var lastLocation: Location

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.getUiSettings().setZoomControlsEnabled(true)
        map.setOnMarkerClickListener(this)

        setUpMap()

        val client = OkHttpClient()
        val mHandler = Handler(Looper.getMainLooper())
        val mMapView: ViewGroup = findViewById(R.id.mapLayout);

        val port = "5000"
        val accountId: String = intent.getStringExtra("accountId")
        val route = "/get_lighters_latlng?userId=" + accountId
        val url = "http://" + ipAdress + ":" + port + route

        val request = Request.Builder()
                .url(url)
                .build()

        var lightersLatLng = listOf<LatLng>()

        Handler().postDelayed({
            // This method will be executed once the timer is over
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful()) {
                        val json1 = response.body()!!.string()
                        println(json1)
                        class DataSon(val lat: Double, val lng: Double)
                        class Data(val lighters_latlng: Array<DataSon>)
                        val json2 = Klaxon().parse<Data>(json1)
                        for (item2 in json2!!.lighters_latlng) {
                            val lat: Double = item2.lat
                            val lng: Double = item2.lng
                            val latlng = LatLng(lat, lng)
                            println(latlng.toString())
                            lightersLatLng += latlng
                        }

                        mHandler.post(Runnable() {
                            run() {
                                for (latlng in lightersLatLng) {
                                    googleMap.addMarker(MarkerOptions().position(latlng)
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.lighter_on_map)))
                                }
                                mMapView.invalidate()
                            }
                        })
                    }
                }
            })
            client.dispatcher().executorService().shutdown()
        }, 5000)
    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        map.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f))

                val client = OkHttpClient()

                val port = "5000"
                val route = "/my_location"
                val url = "http://" + ipAdress + ":" + port + route
                val currentLat: Double = currentLatLng.latitude
                val currentLng: Double = currentLatLng.longitude
                val accountId: String = intent.getStringExtra("accountId")
                val json = """
                    {"lat":${currentLat},"lng":${currentLng},"accountId":"${accountId}"}
                    """.trimIndent()
                val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json)
                val request = Request.Builder()
                        .url(url)
                        .post(body)
                        .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                    }

                    override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful()) {
                        }
                    }
                })
                client.dispatcher().executorService().shutdown()

//                val circleOptions = CircleOptions()
//                        .center(currentLatLng)
//                        .radius(600.0)
//                        .strokeColor(Color.BLACK)
//                        .strokeWidth(2.0F)
//                map.addCircle(circleOptions)

                val mapRadar = MapRadar(map, currentLatLng, this)
                mapRadar.withDistance(700)
                mapRadar.withOuterCircleStrokeColor(0xfccd29)
                mapRadar.withRadarColors(0x00fccd29, Color.parseColor("#F45500"))
                mapRadar.startRadarAnimation()      //in onMapReadyCallBack
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onBackPressed() {
        if (backPressed + 2000 > System.currentTimeMillis()) {
            val intent: Intent = Intent(applicationContext, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.putExtra("EXIT", true)
            startActivity(intent)
        }
        else {
            Toast.makeText(baseContext, "Press once again to exit", Toast.LENGTH_SHORT).show()
        }
        backPressed = System.currentTimeMillis()
    }

    private fun showPopup(view: View) {
        var popup: PopupMenu? = null;
        popup = PopupMenu(this, view)
        popup.inflate(R.menu.menu_map)
        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->
            when (item!!.itemId) {
                R.id.header1 -> {
                    Toast.makeText(this@MapsActivity, item.title, Toast.LENGTH_SHORT).show();
                }
                R.id.header2 -> {
                    Toast.makeText(this@MapsActivity, item.title, Toast.LENGTH_SHORT).show();
                }
                R.id.header3 -> {
                    Toast.makeText(this@MapsActivity, item.title, Toast.LENGTH_SHORT).show();
                }
            }
            true
        })
        popup.show()
    }
}