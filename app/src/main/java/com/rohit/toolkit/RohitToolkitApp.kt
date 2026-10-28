package com.rohit.toolkit

import android.app.Application
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import java.io.File

class RohitToolkitApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize PDFBox resource loader for Android
        PDFBoxResourceLoader.init(applicationContext)

        // Ensure working directories exist
        val toolsDir = File(filesDir, "processed_tools")
        if (!toolsDir.exists()) {
            toolsDir.mkdirs()
        }
    }
}
