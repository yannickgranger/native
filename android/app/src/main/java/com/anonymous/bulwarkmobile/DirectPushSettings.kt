package com.anonymous.bulwarkmobile

import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager

/**
 * Whether the direct connection (BulwarkDirectPushService) is on, and the
 * texts of its notification in the app's language. Kept across reboots and
 * updates. The boot receiver is on only while the mode is, so an app that
 * never uses it is not started at every boot.
 */
object DirectPushSettings {
    /**
     * The notification's [channel], [title] and [text]; [stoppedText] tells
     * the user the connection stopped itself
     * (BulwarkDirectPushService.stoppedItself), in the channel of sync
     * problems, named [problemChannel]; [givenUpText], in the same place,
     * that an account was given up on (BulwarkDirectPushService.onGivenUp).
     */
    data class Texts(
        val channel: String,
        val title: String,
        val text: String,
        val stoppedText: String,
        val problemChannel: String,
        val givenUpText: String,
    )

    private const val PREFS = "bulwark_direct_push"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_CHANNEL = "channelName"
    private const val KEY_TITLE = "title"
    private const val KEY_TEXT = "text"
    private const val KEY_STOPPED_TEXT = "stoppedText"
    private const val KEY_PROBLEM_CHANNEL = "problemChannel"
    private const val KEY_GIVEN_UP_TEXT = "givenUpText"

    fun isEnabled(context: Context): Boolean = prefs(context).getBoolean(KEY_ENABLED, false)

    fun enable(context: Context, texts: Texts) {
        prefs(context).edit()
            .putBoolean(KEY_ENABLED, true)
            .putString(KEY_CHANNEL, texts.channel)
            .putString(KEY_TITLE, texts.title)
            .putString(KEY_TEXT, texts.text)
            .putString(KEY_STOPPED_TEXT, texts.stoppedText)
            .putString(KEY_PROBLEM_CHANNEL, texts.problemChannel)
            .putString(KEY_GIVEN_UP_TEXT, texts.givenUpText)
            .apply()
        setBootReceiver(context, enabled = true)
    }

    fun disable(context: Context) {
        prefs(context).edit().putBoolean(KEY_ENABLED, false).apply()
        setBootReceiver(context, enabled = false)
    }

    /** The texts JavaScript handed over last; [enable] writes them with the flag, so they are there whenever the mode was ever on. */
    fun texts(context: Context): Texts = texts(prefs(context))

    internal fun texts(prefs: SharedPreferences): Texts {
        fun read(key: String) = checkNotNull(prefs.getString(key, null)) { "$key is written with the enabled flag" }
        return Texts(
            read(KEY_CHANNEL),
            read(KEY_TITLE),
            read(KEY_TEXT),
            read(KEY_STOPPED_TEXT),
            read(KEY_PROBLEM_CHANNEL),
            read(KEY_GIVEN_UP_TEXT),
        )
    }

    private fun setBootReceiver(context: Context, enabled: Boolean) {
        context.packageManager.setComponentEnabledSetting(
            ComponentName(context, BulwarkDirectPushReceiver::class.java),
            if (enabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
            PackageManager.DONT_KILL_APP,
        )
    }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
