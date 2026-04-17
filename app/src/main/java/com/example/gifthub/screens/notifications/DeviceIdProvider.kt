package com.example.gifthub.screens.notifications

import android.content.Context
import java.util.UUID

object DeviceIdProvider {
    private const val PREFS_NAME = "gifthub_device_prefs"
    private const val KEY_DEVICE_ID = "device_id"

    @Volatile
    private var cachedId: String? = null

    fun init(context: Context) {
        if (cachedId != null) return
        synchronized(this) {
            if (cachedId != null) return
            val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val existing = prefs.getString(KEY_DEVICE_ID, null)
            if (existing != null) {
                cachedId = existing
                return
            }
            val newId = UUID.randomUUID().toString()
            prefs.edit().putString(KEY_DEVICE_ID, newId).apply()
            cachedId = newId
        }
    }

    fun getDeviceId(context: Context): String {
        cachedId?.let { return it }
        init(context)
        return cachedId.orEmpty()
    }

    fun getDeviceIdOrEmpty(): String = cachedId.orEmpty()
}