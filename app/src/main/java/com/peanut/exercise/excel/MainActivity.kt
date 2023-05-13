package com.peanut.exercise.excel

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.allViews
import androidx.core.view.children
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.apache.poi.ss.usermodel.Workbook
import org.json.JSONArray
import org.json.JSONObject
import kotlin.concurrent.thread

class MainActivity : PeanutActivity() {
    private var selectedUri: Uri? = null
    private var selectedWorkBook: Workbook? = null

    /**
     * 只有功能，没有界面设计、代码优美可言，主打能用就行。
     * 以后也不会放任何精力在二者上。
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val fileUri: Uri? = intent.data
        if (fileUri != null){
            parseSelectedFile(findViewById<TextInputLayout>(R.id.textFieldLayout), fileUri)
        }
        findViewById<TextInputLayout>(R.id.textFieldLayout).setEndIconOnClickListener {
            fileChooser { _: Int, data: Intent? ->
                parseSelectedFile(it, data?.data)
            }
        }
        findViewById<TextView>(R.id.github).apply {
            text = Html.fromHtml(getString(R.string.github_repo))
            movementMethod = LinkMovementMethod.getInstance()
        }
    }

    private fun parseSelectedFile(it: View, uri: Uri?){
        val (name, size) = FileCompat.getFileNameAndSize(this, uri)
        val destination = this@MainActivity.getExternalFilesDir("excel_temp").toString() + "/$name"
        if (size <= 0)
            Snackbar.make(it, "文件大小为0，读取错误。", Snackbar.LENGTH_SHORT).show()
        else if (!name.endsWith(".xls", true) && !name.endsWith(".xlsx", true))
            Snackbar.make(it, "文件类型不支持，请选择有效的Excel文件。", Snackbar.LENGTH_SHORT).show()
        else {
            findViewById<TextInputEditText>(R.id.textField).setText(name)
            selectedUri = uri
            //创建sheet选项组
            thread {
                val chipGroup = findViewById<ChipGroup>(R.id.chipGroup)
                runOnUiThread {
                    chipGroup.removeAllViews()
                    chipGroup.addView(TextView(this).apply { this.text = "请稍等，正在解析Excel文件。" })
                }
                FileCompat.copyFile(selectedUri!!, destination, this@MainActivity)
                selectedWorkBook = ExcelParser.getReadWorkBookType(destination){ err ->
                    Snackbar.make(it, err, Snackbar.LENGTH_SHORT).show()
                    runOnUiThread {
                        chipGroup.removeAllViews()
                    }
                }
                selectedWorkBook?.let { workbook: Workbook ->
                    runOnUiThread{
                        chipGroup.removeAllViews()
                        for ((i, s) in workbook.sheetIterator().withIndex()){
                            val chip = layoutInflater.inflate(R.layout.filter_chip, chipGroup, false) as Chip
                            chip.text = s.sheetName
                            chip.setOnCheckedChangeListener { _, isChecked ->
                                val root = findViewById<LinearLayout>(R.id.config_view_layout)
                                if (isChecked){
                                    root.addView(ConfigView(this).apply {
                                        sheetID = i
                                        sheetName = s.sheetName
                                        onPreview = { testCfg ->
                                            var testID = 0
                                            val r = JSONArray()
                                            selectedWorkBook?.let { workbook ->
                                                try {
                                                    ExcelParser.parse(cfg = testCfg, workbook = workbook) { question ->
                                                        r.put(question)
                                                        testID++ < 3
                                                    }
                                                } catch (e: Exception) {
                                                    if (testID != 0) {
                                                        //发送几题到preview activity
                                                        startActivity(Intent(this@MainActivity, PreviewActivity::class.java).apply {
                                                            putExtra("JSON", r.toString())
                                                        })
                                                    } else Toast.makeText(this@MainActivity, "出错了，可能是配置或题目有问题。${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        }
                                    })
                                }else{
                                    for (a in root.allViews){
                                        if (a is ConfigView && a.sheetID == i) {
                                            root.removeView(a)
                                            break
                                        }
                                    }
                                }
                            }
                            chipGroup.addView(chip)
                        }
                    }
                }
            }
        }
        findViewById<Button>(R.id.parsing).setOnClickListener {
            thread {
                val q = Question(this)
                for (v in findViewById<LinearLayout>(R.id.config_view_layout).children){
                    if (v is ConfigView){
                        var c: JSONObject? = null
                        runOnUiThread { v.getConfig { c = it } }
                        selectedWorkBook?.let {workbook->
                            ExcelParser.parse(cfg = c!!, workbook = workbook){question->
                                q.save(question)
                            }
                        }
                    }
                }
                Snackbar.make(it, "共解析${q.getNumber()}题", Snackbar.LENGTH_SHORT).setAction("OK"){}.show()
            }
        }
    }

    init {
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLInputFactory",
            "com.fasterxml.aalto.stax.InputFactoryImpl"
        )
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLOutputFactory",
            "com.fasterxml.aalto.stax.OutputFactoryImpl"
        )
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLEventFactory",
            "com.fasterxml.aalto.stax.EventFactoryImpl"
        )
    }
}