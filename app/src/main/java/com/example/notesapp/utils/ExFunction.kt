package com.example.notesapp.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.ConnectivityManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.example.notesapp.R
import com.google.android.gms.ads.*
import com.permissionx.guolindev.PermissionX

fun <T> Context.openActivity(it: Class<T>) {
    startActivity(Intent(this, it))
}

fun Context.showToast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()

}

fun FragmentActivity.checkPermission(
    onGranted: (() -> Unit)? = null,
    onDenied: (() -> Unit)? = null
) {
    PermissionX.init(this)
        .permissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ).onExplainRequestReason { scope, deniedList ->
            scope.showRequestReasonDialog(
                deniedList,
                "Core fundamental are based on these permissions",
                "OK",
                "Cancel"
            )
        }
        .request { allGranted, _, deniedList ->
            if (allGranted) {
                onGranted?.invoke()
            } else {
                onDenied?.invoke()
            }
        }
}

fun FragmentActivity.checkCamera(
    onGranted: (() -> Unit)? = null,
    onDenied: (() -> Unit)? = null
) {
    PermissionX.init(this)
        .permissions(
            Manifest.permission.CAMERA
        ).onExplainRequestReason { scope, deniedList ->
            scope.showRequestReasonDialog(
                deniedList,
                "Core fundamental are based on these permissions",
                "OK",
                "Cancel"
            )
        }
        .request { allGranted, _, deniedList ->
            if (allGranted) {
                onGranted?.invoke()
            } else {
                onDenied?.invoke()
            }
        }
}

@SuppressLint("MissingPermission")
fun Context.isNetworkConnected(): Boolean {
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
    return cm?.activeNetworkInfo != null && cm.activeNetworkInfo?.isConnected!!
}

fun Context.showBanner(bannerLayout: FrameLayout) {

    val adaptiveAds = AdaptiveAds(this)
    val adView = AdView(this)
    adView.adUnitId = getString(R.string.admob_banner_id)
    bannerLayout.addView(adView)

    val testDevices = ArrayList<String>()
    testDevices.add(AdRequest.DEVICE_ID_EMULATOR)

    val requestConfiguration = RequestConfiguration.Builder()
        .setTestDeviceIds(testDevices)
        .build()

    MobileAds.setRequestConfiguration(requestConfiguration)

    adView.adSize = adaptiveAds.adSize
    adView.loadAd(AdRequest.Builder().build())

    adView.adListener = object : AdListener() {

        override fun onAdFailedToLoad(adError: LoadAdError) {
            bannerLayout.removeAllViews()
        }
    }

}

 fun Context.isLocationEnabled(): Boolean {
    val locationManager: LocationManager =
        getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
        LocationManager.NETWORK_PROVIDER
    )
}
