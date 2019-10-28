package com.awoo.uploader

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*


private const val OwO: Int = 42

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        pick_btn.setOnClickListener {
            startActivityForResult(intent, OwO)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == OwO) {
            if (resultCode == Activity.RESULT_OK) {
                data?.data?.also { uri ->
                    val uploadIntent = Intent(this, UploadActivity::class.java)
                    uploadIntent.data = uri
                    startActivity(uploadIntent)
                }
            } else {
                text.text = "sumfing went wong"
            }
        }
    }
}
