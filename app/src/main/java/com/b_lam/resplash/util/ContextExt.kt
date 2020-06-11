package com.b_lam.resplash.util

import android.app.Activity
import android.app.Notification
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Build
import android.widget.Toast
import androidx.annotation.IntRange
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.os.ConfigurationCompat
import androidx.fragment.app.Fragment
import com.b_lam.resplash.R
import com.b_lam.resplash.ResplashApplication.Companion.CHANNEL_ID
import java.util.*

inline fun Context.createNotification(
    priority: Int = NotificationCompat.PRIORITY_DEFAULT,
    body: NotificationCompat.Builder.() -> Unit
): Notification {
    val builder = NotificationCompat.Builder(this, CHANNEL_ID)
    builder.setSmallIcon(R.drawable.ic_resplash_24dp)
    builder.priority = priority
    builder.body()
    return builder.build()
}

fun Context.applyLanguage(locale: Locale?): Context {
    val configuration = resources.configuration
    val currentLocale = ConfigurationCompat.getLocales(configuration)[0]
    val newLocale = locale ?: ConfigurationCompat.getLocales(Resources.getSystem().configuration)[0]

    return if (newLocale == currentLocale) {
        this
    } else {
        Locale.setDefault(newLocale)
        configuration.setLocale(newLocale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
        createConfigurationContext(configuration)
    }
}

fun Context?.toast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT) =
    this?.let { Toast.makeText(it, text, duration).show() }

fun Context?.toast(@StringRes textId: Int, duration: Int = Toast.LENGTH_LONG) =
    this?.let { Toast.makeText(it, textId, duration).show() }

fun Context.hasPermission(vararg permissions: String): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        permissions.all { singlePermission ->
            ContextCompat.checkSelfPermission(this, singlePermission) == PackageManager.PERMISSION_GRANTED
        }
    else true
}

fun Activity.requestPermission(vararg permissions: String, @IntRange(from = 0) requestCode: Int) =
    ActivityCompat.requestPermissions(this, permissions, requestCode)

fun Fragment.requestPermission(vararg permissions: String, @IntRange(from = 0) requestCode: Int) =
    requestPermissions(permissions, requestCode)
