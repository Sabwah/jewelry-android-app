package com.evans.jewelryapp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

object CloudinaryUploader {
    private const val CLOUD_NAME = "dnb8awck9" // ✅ Replace with your actual Cloudinary cloud name
    private const val UPLOAD_PRESET = "android_unsigned_upload" // ✅ Replace with your actual unsigned preset
    private const val UPLOAD_URL = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload"

    suspend fun uploadImage(imageFile: File): String? = withContext(Dispatchers.IO) {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", imageFile.name, imageFile.asRequestBody())
            .addFormDataPart("upload_preset", UPLOAD_PRESET)
            .build()

        val request = Request.Builder()
            .url(UPLOAD_URL)
            .post(requestBody)
            .build()

        val response = OkHttpClient().newCall(request).execute()
        if (response.isSuccessful) {
            val bodyString = response.body?.string()
            // Extract secure_url from JSON response
            val regex = """"secure_url":"(.*?)"""".toRegex()
            val match = regex.find(bodyString ?: "")
            match?.groupValues?.get(1)?.replace("\\/", "/")
        } else {
            null
        }
    }
}
