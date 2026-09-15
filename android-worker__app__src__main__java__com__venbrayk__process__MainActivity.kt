package com.venbrayk.process

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {
    private var active = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT >= 33 && ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 10)
        }
        val status = findViewById<TextView>(R.id.status)
        val toggle = findViewById<Button>(R.id.toggle)
        toggle.setOnClickListener {
            active = !active
            if (active) {
                startForegroundService(Intent(this, WorkerService::class.java))
                status.text = "Aguardando computador por USB"
                toggle.text = "INTERROMPER"
            } else {
                stopService(Intent(this, WorkerService::class.java))
                status.text = "Desconectado"
                toggle.text = "PERMITIR CONEXÃO"
            }
        }
    }
}

