package com.peanut.exercise.excel

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import com.peanut.exercise.excel.CheckboxList.Companion.toPdIntArray
import com.peanut.exercise.excel.CheckboxList.Companion.toXzIntArray
import org.json.JSONArray
import org.json.JSONObject

class PreviewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)
        findViewById<LinearLayout>(R.id.preview_layout).apply {
            removeAllViews()
            val r = JSONArrayIterator<JSONObject>(JSONArray(intent.getStringExtra("JSON")))
            for (q in r){
                addView(TextView(this@PreviewActivity).apply { text = q.getString("Topic") })
                addView(CheckboxList(
                    this@PreviewActivity,
                    MutableList(q.getJSONArray("Options").length()){ q.getJSONArray("Options").getString(it) },
                    random = false,
                    questionType = QuestionType.valueOf(q.getString("Type"))
                ).apply {
                    this.select(if (QuestionType.valueOf(q.getString("Type")) == QuestionType.PD) q.getString("Answer").toPdIntArray() else
                        q.getString("Answer").toXzIntArray())
                })
                if (q.getString("Explain").isNotEmpty())
                    addView(TextView(this@PreviewActivity).apply { text = q.getString("Explain") })
                addView(TextView(this@PreviewActivity).apply { text = "" })
            }
        }
    }
}