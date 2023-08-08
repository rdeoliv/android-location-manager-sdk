package com.naurt.example.exampleapp

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import com.naurt.sdk.enums.NaurtMovement
import com.naurt.sdk.enums.NaurtValidationStatus
import com.naurt.sdk.location.NaurtLocation
import com.naurt.sdk.location.NaurtLocationListener


class NaurtListener(val naurtTextView: TextView, val validationTextView: TextView) :
    NaurtLocationListener {
    override fun onLocationChanged(location: NaurtLocation) {

        Log.d("Naurt", "My location has changed! $location")

        if (location.isMocked){
            Log.d("Naurt", "My location has been spoofed!")
        }
        if (location.isMockedPrevented){
            Log.d("Naurt", "My location has been spoofed but Naurt has provided the real location!")
        }


        val roundedLat = String.format("%.6f", location.latitude)
        val rounededLon = String.format("%.6f", location.longitude)

        val roundedSpeed = String.format("%.2f", location.speedMs)
        val roundedAltitude = String.format("%.0f", location.altitude)
        val roundedHeading = String.format("%.1f", location.bearingDeg)
        val roundedDistanceTravelled = String.format("%.2f", location.cumulativeDistance)

        // UI needs to be changed from main thread.

        Handler(Looper.getMainLooper()).post {
            naurtTextView.text =
                "Lat: ${roundedLat}		Lon: ${rounededLon}		Alt: ${roundedAltitude}\n\nSpeed: ${roundedSpeed}		Heading: ${roundedHeading}\n\n Motion type: ${location.motionFlag}		Environment type: ${location.environmentFlag}\n\nBackground Status: ${location.backgroundStatus}\n\nLocation origin: ${location.locationOrigin}\n\nDistance Travelled: $roundedDistanceTravelled\n\nisMocked: ${location.isMocked}    isMockedPrevented: ${location.isMockedPrevented}"
        }
    }

    override fun onProviderDisabled(provider: String) {
        Log.d("Naurt", "Location provider has been disabled! $provider")
    }

    override fun onProviderEnabled(provider: String) {
        Log.d("Naurt", "Location provider has been enabled! $provider")
    }

    override fun onValidationStatusChanged(validationStatus: NaurtValidationStatus) {
        Log.d("Naurt", "Naurt validation status has changed! $validationStatus")

        // UI needs to be changed from main thread.

        Handler(Looper.getMainLooper()).post {
            if (validationStatus.isValid()) {
                validationTextView.text = "Naurt key has been successfully validated."
            } else {
                validationTextView.text = "Naurt key is not validated. Status: $validationStatus"
            }
        }
    }
}