package com.rohit.toolkit.updater

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.rohit.toolkit.BuildConfig
import com.rohit.toolkit.MainActivity
import okhttp3.OkHttpClient
import okhttp3.Request

class UpdateCheckWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        try {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://api.github.com/repos/ROHITSHARMA838888/Rohits-ToolKit/releases/latest")
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "RohitsToolKit-App")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val json = response.body?.string() ?: return Result.success()
                    val release = Gson().fromJson(json, GitHubRelease::class.java)

                    val remoteVersionTag = release.tagName.removePrefix("v").trim()
                    val currentVersion = BuildConfig.VERSION_NAME.removePrefix("v").trim()

                    if (remoteVersionTag != currentVersion) {
                        showUpdateNotification(release.tagName)
                    }
                }
            }
            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }

    private fun showUpdateNotification(latestTag: String) {
        val channelId = "updates_channel"
        val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "App Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for Rohit's ToolKit GitHub releases"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "settings")
        }

        val pendingIntent = PendingIntent.getActivity(
            appContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(appContext, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("Rohit's ToolKit Update Available")
            .setContentText("Version $latestTag is ready to download from GitHub Releases.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }
}
