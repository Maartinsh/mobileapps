package com.experiment.scope

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.text.TextUtils
import android.provider.Settings.Secure
import android.provider.Settings.Secure.LOCATION_MODE_OFF
import android.provider.Settings.SettingNotFoundException
import android.provider.Settings.Secure.LOCATION_MODE
import android.os.Build
import android.provider.Settings


fun createFragment(
  supportFragmentManager: FragmentManager,
  fragment: Fragment,
  tag: String
) {
  supportFragmentManager
    .beginTransaction()
    .replace(R.id.fragmentContainer,
      fragment,
      tag)
    .addToBackStack(tag)
    .commit()
}

fun isLocationEnabled(
  context: Context
): Boolean {
  val locationMode: Int
  val locationProviders: String

  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
    try {
      locationMode = Settings.Secure.getInt(
        context.contentResolver,
        Settings.Secure.LOCATION_MODE
      )
    } catch (e: SettingNotFoundException) {
      e.printStackTrace()
      return false
    }

    return locationMode != Settings.Secure.LOCATION_MODE_OFF
  } else {
    locationProviders =
            Settings.Secure.getString(context.contentResolver, Settings.Secure.LOCATION_PROVIDERS_ALLOWED)
    return !TextUtils.isEmpty(locationProviders)
  }
}

