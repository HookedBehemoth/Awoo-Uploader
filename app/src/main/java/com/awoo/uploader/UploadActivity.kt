package com.awoo.uploader

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.upload_layout.*
import java.io.InputStream
import kotlin.concurrent.thread


private const val STORAGE_PERMISSION_CODE: Int = 1337

class UploadActivity : AppCompatActivity() {
    private var uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.upload_layout)
        requestStoragePermission()

        upload_btn.setOnClickListener {
            thread {
                val multipart = MultipartUtility("https://lewd.pics/p/index.php", "UTF-8")
                var inputStream : InputStream? = null
                var fileType : String? = null
                var fileName : String? = null
                multipart.addFormField("curl", "1")
                uri?.let{uri ->
                    inputStream = contentResolver.openInputStream(uri)
                    fileType = contentResolver.getType(uri)
                    contentResolver.query(uri, null, null, null, null)
                }?.use {cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    cursor.moveToFirst()
                    fileName = cursor.getString(nameIndex);
                }
                multipart.addFilePart("fileToUpload", inputStream, fileType, fileName)
                runOnUiThread { text.text = "sending..." }
                val response = multipart.finish()

                println("Server replied with:")

                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                response.forEach {
                    println(it)
                    runOnUiThread {
                        text.text = it
                        Toast.makeText(this, "copied link to clipboard", Toast.LENGTH_LONG).show()
                    }
                    val clip = ClipData.newPlainText("awoo link", it)
                    clipboard.setPrimaryClip(clip)
                }
            }
        }
    }

    private fun loadIntent() {
        uri = if (intent.action == Intent.ACTION_SEND) {
            intent.getParcelableExtra(Intent.EXTRA_STREAM)!!
        } else {
            intent.data!!
        }
        println("picked image at: $uri")
        image.setImageURI(uri)
    }

    private fun requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            loadIntent()
            return
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {}
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted! You're good to go", Toast.LENGTH_LONG).show()
                loadIntent()
            } else {
                Toast.makeText(this, "No permission granted! :/", Toast.LENGTH_LONG).show()
            }
        }
    }
}