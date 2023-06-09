package com.peanut.exercise.excel

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

open class PeanutActivity : AppCompatActivity() {
    protected var onActivityResultListener = mutableListOf<Pair<Int,(Int, Intent?)->Unit>>()
    private var onRequestPermissionsResultListener = mutableListOf<Pair<Int,(Array<out String>, IntArray)->Unit>>()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        for (a in onActivityResultListener){
            if (a.first == requestCode) {
                a.second.invoke(resultCode,data)
                onActivityResultListener.remove(a)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for (a in onRequestPermissionsResultListener){
            if (a.first == requestCode) {
                a.second.invoke(permissions,grantResults)
                onRequestPermissionsResultListener.remove(a)
            }
        }
    }

    fun fileChooser(mimeType:String = "application/*",func:(resultCode:Int, data:Intent?)->Unit){
        val requestCode: Int = (Math.random()*Int.MAX_VALUE).toInt() and 0x3fff
        onActivityResultListener.add(requestCode to func)
        requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)){ _, grantResults ->
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = mimeType
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                startActivityForResult(intent, requestCode)
            }else Toast.makeText(this,"授权被拒绝,想屁吃?",Toast.LENGTH_SHORT).show()
        }
    }

    fun requestPermissions(permissions:Array<String>,func:(permissions: Array<out String>, grantResults: IntArray)->Unit){
        val requestCode: Int = (Math.random()*Int.MAX_VALUE).toInt() and 0x7fff
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                func(permissions, IntArray(permissions.size){PackageManager.PERMISSION_GRANTED})
            }else {
                onActivityResultListener.add(requestCode to { _:Int, _:Intent?->
                    if (Environment.isExternalStorageManager()) {
                        func(permissions, IntArray(permissions.size){PackageManager.PERMISSION_GRANTED})
                    }
                })
                startActivityForResult(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply { data = Uri.parse("package:${BuildConfig.APPLICATION_ID}") }, requestCode)
            }
        }else if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            onRequestPermissionsResultListener.add(requestCode to func)
            ActivityCompat.requestPermissions(this, permissions, requestCode)
        }else{
            func(permissions, IntArray(permissions.size){PackageManager.PERMISSION_GRANTED})
        }
    }

    fun callActivity(intent: Intent,func:(resultCode:Int, data:Intent?)->Unit){
        val requestCode: Int = (Math.random()*Int.MAX_VALUE).toInt() and 0xbfff
        onActivityResultListener.add(requestCode to func)
        startActivityForResult(intent, requestCode)
    }
}