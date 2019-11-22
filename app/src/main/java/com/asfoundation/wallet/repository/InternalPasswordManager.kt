package com.asfoundation.wallet.repository

import android.content.Context
import android.preference.PreferenceManager
import android.util.Base64

object InternalPasswordManager {

  @JvmStatic
  fun setPassword(address: String, password: String, context: Context?) {
    context?.let {
      val pref = PreferenceManager.getDefaultSharedPreferences(it)
      pref.edit()
          .putString("$address-pwd", Base64.encodeToString(password.toByteArray(), 0))
          .apply()
    }
  }

  @JvmStatic
  fun getPassword(address: String, context: Context?): String? {
    return context?.let {
      val pref = PreferenceManager.getDefaultSharedPreferences(it)
      val passwordEncoded = pref.getString("$address-pwd", "")
      val passwordBytes = Base64.decode(passwordEncoded, 0)
      String(passwordBytes)
    }
  }

}