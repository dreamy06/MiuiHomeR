package com.yuk.miuiHomeR

import android.content.Context
import com.yuk.miuiHomeR.utils.PrefsUtils
import com.yuk.miuiHomeR.utils.PrefsUtils.getSharedPrefs
import com.yuk.miuiHomeR.utils.ktx.getLocale
import com.yuk.miuiHomeR.utils.ktx.setLocale
import java.util.*

class Application : android.app.Application() {

    override fun attachBaseContext(base: Context?) {
        PrefsUtils.mSharedPreferences = getSharedPrefs(base, false)
        val locale: String? = PrefsUtils.mSharedPreferences.getString("prefs_key_language", "SYSTEM")
        if (locale != null && "SYSTEM" != locale && "1" != locale) Locale.setDefault(Locale.forLanguageTag(locale))
        super.attachBaseContext(base)
    }
}