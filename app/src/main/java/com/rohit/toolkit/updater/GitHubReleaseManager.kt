package com.rohit.toolkit.updater

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.rohit.toolkit.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

data class GitHubAsset(
    @SerializedName("name") val name: String,
    @SerializedName("browser_download_url") val downloadUrl: String,
    @SerializedName("size") val size: Long,
    @SerializedName("content_type") val contentType: String
)

data class GitHubRelease(
    @SerializedName("tag_name") val tagName: String,
    @SerializedName("name") val name: String?,
    @SerializedName("body") val releaseNotes: String?,
    @SerializedName("published_at") val publishedAt: String?,
    @SerializedName("assets") val assets: List<GitHubAsset>
)

sealed class UpdateStatus {
    object Idle : UpdateStatus()
    object Checking : UpdateStatus()
    data class UpToDate(val currentVersion: String) : UpdateStatus()
    data class UpdateAvailable(
        val latestVersion: String,
        val currentVersion: String,
        val releaseNotes: String,
        val apkSize: Long,
        val downloadUrl: String
    ) : UpdateStatus()
    data class Downloading(val progressPercent: Int, val downloadedBytes: Long, val totalBytes: Long) : UpdateStatus()
    data class DownloadCompleted(val apkFile: File) : UpdateStatus()
    data class Error(val message: String) : UpdateStatus()
}

object GitHubReleaseManager {

    private const val GITHUB_OWNER = "ROHITSHARMA838888"
    private const val GITHUB_REPO = "Rohits-ToolKit"
    const val RELEASES_WEB_URL = "https://github.com/$GITHUB_OWNER/$GITHUB_REPO/releases"
    private const val LATEST_API_URL = "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases/latest"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val _updateState = MutableStateFlow<UpdateStatus>(UpdateStatus.Idle)
    val updateState = _updateState.asStateFlow()

    suspend fun checkForUpdates(context: Context) {
        _updateState.value = UpdateStatus.Checking
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(LATEST_API_URL)
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "RohitsToolKit-App")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        if (response.code == 404) {
                            _updateState.value = UpdateStatus.UpToDate(BuildConfig.VERSION_NAME)
                        } else {
                            _updateState.value = UpdateStatus.Error("GitHub API returned error: HTTP ${response.code}")
                        }
                        return@withContext
                    }

                    val json = response.body?.string() ?: ""
                    val release = Gson().fromJson(json, GitHubRelease::class.java)

                    val remoteVersionTag = release.tagName.removePrefix("v").trim()
                    val currentVersion = BuildConfig.VERSION_NAME.removePrefix("v").trim()

                    // Find APK asset
                    val apkAsset = release.assets.find { it.name.endsWith(".apk", ignoreCase = true) }

                    if (isNewerVersion(remoteVersionTag, currentVersion) && apkAsset != null) {
                        _updateState.value = UpdateStatus.UpdateAvailable(
                            latestVersion = release.tagName,
                            currentVersion = BuildConfig.VERSION_NAME,
                            releaseNotes = release.releaseNotes ?: "No release notes provided.",
                            apkSize = apkAsset.size,
                            downloadUrl = apkAsset.downloadUrl
                        )
                    } else {
                        _updateState.value = UpdateStatus.UpToDate(BuildConfig.VERSION_NAME)
                    }
                }
            } catch (e: Exception) {
                _updateState.value = UpdateStatus.Error(e.localizedMessage ?: "Failed to check for updates")
            }
        }
    }

    suspend fun downloadApk(context: Context, downloadUrl: String, onCompleted: (File) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(downloadUrl).build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        _updateState.value = UpdateStatus.Error("Failed to download APK: HTTP ${response.code}")
                        return@withContext
                    }

                    val body = response.body ?: throw Exception("Empty response body")
                    val totalBytes = body.contentLength()

                    val updatesDir = File(context.cacheDir, "updates").apply { if (!exists()) mkdirs() }
                    val apkFile = File(updatesDir, "rohits_toolkit_update.apk")

                    body.byteStream().use { input ->
                        FileOutputStream(apkFile).use { output ->
                            val buffer = ByteArray(8 * 1024)
                            var bytesRead: Int
                            var downloadedBytes = 0L

                            while (input.read(buffer).also { bytesRead = it } != -1) {
                                output.write(buffer, 0, bytesRead)
                                downloadedBytes += bytesRead

                                val progress = if (totalBytes > 0) {
                                    ((downloadedBytes * 100) / totalBytes).toInt()
                                } else 0

                                _updateState.value = UpdateStatus.Downloading(progress, downloadedBytes, totalBytes)
                            }
                        }
                    }

                    _updateState.value = UpdateStatus.DownloadCompleted(apkFile)
                    withContext(Dispatchers.Main) {
                        onCompleted(apkFile)
                    }
                }
            } catch (e: Exception) {
                _updateState.value = UpdateStatus.Error(e.localizedMessage ?: "Download failed")
            }
        }
    }

    fun installApk(context: Context, apkFile: File) {
        try {
            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(installIntent)
        } catch (e: Exception) {
            _updateState.value = UpdateStatus.Error("Failed to start installation: ${e.localizedMessage}")
        }
    }

    private fun isNewerVersion(remote: String, current: String): Boolean {
        val remoteParts = remote.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }

        val length = maxOf(remoteParts.size, currentParts.size)
        for (i in 0 until length) {
            val r = remoteParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }
        return false
    }
}
