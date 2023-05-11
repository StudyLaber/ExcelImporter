package com.peanut.exercise.excel

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.peanut.exercise.excel.databinding.ConfigViewBinding
import org.json.JSONObject

class ConfigView: FrameLayout {
    var sheetID: Int = -1
    var sheetName: String = ""
        set(value) {
            field = value
            binding.sheetName.text = value
        }
    private var binding: ConfigViewBinding

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle){
        binding = ConfigViewBinding.inflate(LayoutInflater.from(context), this, true)
        binding.button.setOnClickListener { preview() }
    }

    fun getConfig(onSuccess:(JSONObject)->Unit){
        val result = JSONObject()
        //sheet
        result.put("sheet", sheetID)
        result.put("sheetName", sheetName)
        //起始行
        try {
            result.put("start", binding.quesStartNum.text.toString().toInt())
            binding.quesStartNumLayout.error = null
        }catch (e:Exception){
            binding.quesStartNumLayout.error = e.localizedMessage
            return
        }
        //题目列
        try {
            val a = binding.quesTitleLoc.text.toString()
            ConfigView.assert(a.length == 1){ "length must be 1" }
            if (a[0] !in 'A'..'Z')
                throw Exception("必须是A-Z中的一个。")
            result.put("topic", a[0] - 'A')
            binding.quesTitleLocLayout.error = null
        }catch (e:Exception){
            binding.quesTitleLocLayout.error = e.localizedMessage
            return
        }
        //选项列
        try {
            val a = binding.quesOptionLoc.text.toString().uppercase()
            //A,B,C-D
            result.put("list", string2List(a))
            binding.quesOptionLocLayout.error = null
        }catch (e:Exception){
            binding.quesOptionLocLayout.error = e.localizedMessage
            return
        }
        //选项分隔符
        try {
            var a = ""
            if (binding.checkBox.isChecked){
                a = binding.quesOptionSplit.text.toString()
            }
            result.put("list_separator", a)
            binding.quesOptionSplitLayout.error = null
        }catch (e:Exception){
            binding.quesOptionSplitLayout.error = e.localizedMessage
            return
        }
        //答案列
        try {
            val a = binding.quesAnsLoc.text.toString()[0].uppercaseChar()
            if (a !in 'A'..'Z')
                throw Exception("必须是A-Z中的一个。")
            result.put("ans", a - 'A')
            binding.quesAnsLocLayout.error = null
        }catch (e:Exception){
            binding.quesAnsLocLayout.error = e.localizedMessage
            return
        }
        //答案替换列
        try {
            val a = JSONObject(binding.quesAnsReplace.text.toString())
            result.put("answerTranslate", a)
            binding.quesAnsReplaceLayout.error = null
        }catch (e:Exception){
            binding.quesAnsReplaceLayout.error = "Json格式不合法，请百度或者仔细看默认值的格式"
            return
        }
        //解析列
        try {
            val a = binding.quesExplainLoc.text.toString()[0].uppercaseChar()
            if (a !in 'A'..'Z')
                throw Exception("必须是A-Z中的一个。")
            result.put("explain", a - 'A')
            binding.quesExplainLocLayout.error = null
        }catch (e:Exception){
            binding.quesExplainLocLayout.error = e.localizedMessage
            return
        }
        onSuccess(result)
    }

    private fun preview(){
        this.getConfig { cfg ->
            println(cfg)
        }
    }

    private fun string2List(string: String): List<Int> = mutableListOf<Int>().apply {
        for (s in string.split(",")){
            val p = s.split("-")
            when (p.size) {
                2 -> {
                    for(c in p[0][0]..p[1][0]) this.add(c - 'A')
                }
                1 -> this.add(p[0][0] - 'A')
                else -> throw Exception("范围不合法${s}。")
            }
        }
    }

    companion object {
        inline fun assert(value: Boolean, lazyMessage: () -> String) {
            if (!value) {
                val message = lazyMessage()
                throw Exception(message)
            }
        }
    }

}