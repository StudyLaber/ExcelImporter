package com.peanut.exercise.excel

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.util.Base64
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.*

class Question(context: Context) {
    private var database: DataBase? = null
    private var id: Int = -1

    init {
        database = DataBase(context, File(context.getExternalFilesDir("Database"), "parse.db").also { it.delete() }.path, null, 1)
        "a.sql".execAssetsSql(database!!.sqLiteDatabase, context)
    }

    private fun String.execAssetsSql(db: SQLiteDatabase?, context: Context){
        try {
            val sqls = BufferedReader(InputStreamReader(context.resources.assets.open("schema/$this"))).readLines()
            for (sql in sqls){
                if (sql.startsWith("--")) continue
                try {
                    db?.execSQL(sql)
                }catch (e: SQLiteException){
                    e.printStackTrace()
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun save(question: JSONObject):Boolean{
        database?.let { temp->
            val topic = question.getString("Topic").encode64()
            val optionList = question.getJSONArray("Options").toString().encode64()
            val answer = question.getString("Answer")
            val explain = question.getString("Explain")
            val type = question.getString("Type")
            val chapter = question.getString("Chapter")
            when (type.lowercase(Locale.CHINA)) {
                "pd" ->
                    temp.execSQL("insert into PD values ('${++id}','$topic','$optionList','$answer','$explain','$chapter')")
                "dx" ->
                    temp.execSQL("insert into DX values ('${++id}','$topic','$optionList','$answer','$explain','$chapter')")
                "dd" ->
                    temp.execSQL("insert into DD values ('${++id}','$topic','$optionList','$answer','$explain','$chapter')")
                "tk" ->
                    temp.execSQL("insert into TK values ('${++id}','$topic','$optionList','$explain','$chapter')")
                "jd" ->
                    temp.execSQL("insert into JD values ('${++id}','$topic','$explain','$chapter')")
                "yd" ->{
                    temp.execSQL("insert into YD values ('${++id}','$topic','$optionList','$answer','$explain','$chapter')")
                }
                else -> {
                    return false
                }
            }
            return true
        }
        return false
    }

    fun getNumber() = id + 1

    private fun String.encode64(): String = Base64.encodeToString(this.toByteArray(), 0)
}