package com.example.service

import java.io.File
import java.util.*

class StorageService {

    // Cloudinary Config (Optional - replace with actual values if you want to use Cloudinary)
    private val cloudinaryCloudName: String? = null // e.g., "your_cloud_name"
    private val cloudinaryUploadPreset: String? = null // e.g., "your_preset"

    /**
     * Saves the image and returns the URL/path to be stored in the database.
     */
    suspend fun saveProfileImage(bytes: ByteArray, userId: String, originalName: String): String {
        if (!cloudinaryCloudName.isNullOrEmpty() && !cloudinaryUploadPreset.isNullOrEmpty()) {
            return uploadToCloudinary(bytes, userId)
        } else {
            return saveLocally(bytes, userId, originalName)
        }
    }

    private fun saveLocally(bytes: ByteArray, userId: String, originalName: String): String {
        val extension = originalName.substringAfterLast(".", "jpg")
        val fileName = "profile_${userId}_${System.currentTimeMillis()}.$extension"
        
        val uploadDir = File("uploads/profile_pics")
        if (!uploadDir.exists()) uploadDir.mkdirs()
        
        val file = File(uploadDir, fileName)
        file.writeBytes(bytes)
        
        // Return the relative path for static serving
        return "/uploads/profile_pics/$fileName"
    }

    private suspend fun uploadToCloudinary(bytes: ByteArray, userId: String): String {
        // This would use Ktor client to upload to Cloudinary's REST API
        // For now, we'll focus on making Local Storage robust first.
        // If the user provides Cloudinary credentials, we can implement this easily.
        return "" 
    }
}
