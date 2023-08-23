package com.naurt.example.exampleapp

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.naurt.sdk.NaurtLocationManager
import com.naurt.sdk.enums.NaurtEngineType
import org.json.JSONObject
import java.util.Date


class MainActivity : AppCompatActivity() {

    private var naurtLocationManager: NaurtLocationManager? = null
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        if (!this.hasLocationPermission()) {
            this.requestLocationPermission()
        } else{
            val textView = findViewById<TextView>(R.id.locationTextView)
            textView.text = "Location access provided"
            instantiateNaurt(
                findViewById(R.id.naurtTextView),
                findViewById(R.id.validationTextView)
            )
        }
    }


    // Instantiate Naurt!
    private fun instantiateNaurt(naurtTextView: TextView, validationTextView: TextView){
        this.naurtLocationManager = NaurtLocationManager(
            BuildConfig.API_KEY,
            applicationContext,
            NaurtEngineType.Standalone,
            JSONObject(mapOf("example_app" to true))
        )

        val naurtListener = NaurtListener(
            naurtTextView,
            validationTextView
        )

        this.naurtLocationManager?.requestLocationUpdates(naurtListener)

        // A button which updates the metadata associated with Naurt!
        // Use this to link delivery addresses and other data with parking spots and building entrances
        // generated from Naurt.
        val metaButton = findViewById<Button>(R.id.metaButton);
        metaButton.setOnClickListener {
            val newMetadata = JSONObject()
            newMetadata.put("example_app", "true")
            newMetadata.put("refresh_time", System.currentTimeMillis())

            this.naurtLocationManager?.newDestination(newMetadata)

        }
    }



    // Check if the app has permission to access location
    private fun hasLocationPermission(): Boolean {
        val permissionStatus = ContextCompat.checkSelfPermission(
            this,
            ACCESS_FINE_LOCATION
        )
        return permissionStatus == PackageManager.PERMISSION_GRANTED
    }

    // Request location permission from the user
    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    // Handle the result of the permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val textView = findViewById<TextView>(R.id.locationTextView)
                textView.text = "Location access provided"

                instantiateNaurt(
                    findViewById(R.id.naurtTextView),
                    findViewById(R.id.validationTextView)
                )
            } else {
                Log.d("Naurt", "I haven't got the permissions!")
                // Permission denied, handle the situation where the user denied the permission.
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        this.naurtLocationManager?.onDestroy()
    }
}