package com.kaspersky.test_server

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private val executor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val btnExecuteAdb = findViewById<Button>(R.id.btn_execute_adb)
        val editTextAdb = findViewById<EditText>(R.id.et_adb_command)
        btnExecuteAdb.setOnClickListener { onBtnClick(editTextAdb) }
    }

    override fun onResume() {
        super.onResume()
        AdbConnection.start()
    }

    override fun onPause() {
        AdbConnection.stop()
        super.onPause()
    }

    private fun onBtnClick(editTextAdb: EditText) {
        val command = editTextAdb.text.toString()
        if (command.isNotEmpty()) {
            executor.execute {
                val result = kotlin.runCatching { AdbConnection.execute(command) }
                runOnUiThread {
                    Toast.makeText(this, result.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
