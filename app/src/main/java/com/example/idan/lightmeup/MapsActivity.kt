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
import android.view.ViewGroup
import android.widget.Toast
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import com.beust.klaxon.Parser
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
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.Circle
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import khttp.get


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    override fun onMarkerClick(p0: Marker?) = false

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var backPressed: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
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
//        val telaviv = LatLng(32.0853, 34.7818)
//        googleMap.addMarker(MarkerOptions().position(telaviv)
//                .title("Marker in Telavivo")
//                .icon(BitmapDescriptorFactory.fromResource(R.drawable.lighter_on_map)))
//
//        map.addCircle(CircleOptions()
//                .center(telaviv)
//                .radius(1000.0)
//                .fillColor(Color.BLUE)
//                .strokeColor(R.color.colorPrimary)
//        )


        val client = OkHttpClient()
        val mHandler: Handler = Handler(Looper.getMainLooper())
        val mMapView: ViewGroup = findViewById(R.id.mapLayout);

        val address = "10.0.0.2"
        val port = "5000"
        val route = "/get_lighters_latlng"
        val url = "http://" + address + ":" + port + route

        val request = Request.Builder()
                .url(url)
                .build()

        var lightersLatLng = listOf<LatLng>()

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
                                println("ido")
                                googleMap.addMarker(MarkerOptions().position(latlng)
                                        .title("h")
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.lighter_on_map)))
                            }
                            mMapView.invalidate()
                        }
                    });
                }
            }
        })
        client.dispatcher().executorService().shutdown()

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
                val telaviv = LatLng(32.0853, 34.7818)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(telaviv, 12f))
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
}