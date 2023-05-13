package com.peanut.exercise.excel

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.*
import org.apache.poi.util.IOUtils
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.json.JSONArray
import org.json.JSONObject
import java.io.FileInputStream
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*
import kotlin.system.exitProcess


object ExcelParser {
    fun parse(cfg: JSONObject, workbook: Workbook, onParse:(JSONObject)->Boolean){
        val answerTranslation = mutableListOf<Pair<String, String>>()
        cfg.getJSONObject("answerTranslate").forEach { key: String, value: String ->
            answerTranslation.add(key to value)
        }
        val chapter = if (cfg.has("chapter")) cfg.getString("chapter") else ""
        workbook.getSheetAt(cfg.getInt("sheet")).forEach(start = cfg.getInt("start")-1) { row: Row ->
            val topic = row.getCell(cfg.getInt("topic")) ?: return@forEach
            if (topic.cellValue().isBlank())
                return@forEach
            val explain = if (cfg.getInt("explain") > 0) row.getCell(cfg.getInt("explain"))?.cellValue()?:"" else ""
            val (options_, _) = row.getCells(cfg.get("list") as List<Int>, workbook, separator = cfg.getString("list_separator"))
            val ans = if (cfg.getInt("ans") > 0)
                row.getCell(cfg.getInt("ans")).cellValue().translate(answerTranslation).characterOnly()
            else ""
            val options = JSONArray(options_)
            val realAns = if (options.length() > 0) ans else ans.replace("A", "Y").replace("B", "N")
            ConfigView.assert(onParse(JSONObject().also { question ->
                question.put("Topic", topic.cellValue())
                question.put("Answer", realAns)
                question.put("Chapter", chapter)
                question.put("Explain", explain)
                question.put("Options", options)
                question.put("Type", getType(realAns, options))
            })){"无法保存解析结果。"}
        }
    }

    private fun String.characterOnly(): String {
        val sb = StringBuilder()
        this.uppercase(Locale.getDefault()).forEach {
            if (it in 'A'..'Z')
                sb.append(it)
        }
        return sb.toString()
    }

    private fun getType(answer: String, options: JSONArray): String {
        return if (answer.length == 1)
            if (answer in arrayOf("Y", "N"))
                "PD"
            else "DX"
        else if (answer.length > 1) "DD"
        else if (options.length() > 0) "TK" else "JD"
    }

    private fun Sheet.forEach(start: Int = 0, callback: (row: Row) -> Unit) {
        this.forEachIndexed { index, row ->
            if (index >= start)
                callback.invoke(row)
        }
    }

    private fun Row.getCells(jsonArray: List<Int>, workbook: Workbook, separator: String):
            Pair<MutableList<String>, MutableList<Short>> {
        val optionList = mutableListOf<String>()
        val optionListColor = mutableListOf<Short>()
        for (i in jsonArray) {
            val color = this.getCell(i)?.cellStyle?.fontIndex?.let { workbook.getFontAt(it).color }
            val value = this.getCell(i)?.cellValue() ?: ""
            if (value.isNotEmpty())
                if (separator.isNotEmpty()) {
                    for (v in value.split(separator)) {
                        if (v.isNotBlank()) {
                            optionList.add(v.trim())
                            optionListColor.add(color ?: 0)
                        }
                    }
                }else{
                    optionList.add(value)
                    optionListColor.add(color?:0)
                }
        }
        return optionList to optionListColor
    }

    private fun Cell.cellValue(): String {
        return when (this.cellTypeEnum) {
            CellType.NUMERIC -> this.numericCellValue.toString()
            CellType.BOOLEAN -> this.booleanCellValue.toString()
            CellType.ERROR -> this.errorCellValue.toString()
            CellType.STRING, CellType.FORMULA, CellType.BLANK ->
                try {
                    this.stringCellValue
                } catch (e: Exception) {
                    e.localizedMessage
                }
            else -> "单元格格式:" + this.cellTypeEnum.name + "不支持!请使用文本型单元格格式"
        }
    }

    fun getReadWorkBookType(filePath: String, func: (err: String) -> Unit): Workbook? {
        var fileInputStream: FileInputStream? = null
        try {
            fileInputStream = FileInputStream(filePath)
            if (filePath.endsWith(".xlsx", ignoreCase = true)) {
                return XSSFWorkbook(fileInputStream)
            } else if (filePath.endsWith(".xls", ignoreCase = true)) {
                return HSSFWorkbook(fileInputStream)
            }
        } catch (e: IOException) {
            val a = StringWriter()
            e.printStackTrace(PrintWriter(a))
            func("读取文件失败，可能不是excel文件！")
        } finally {
            IOUtils.closeQuietly(fileInputStream)
        }
        return null
    }

    fun JSONObject.forEach(func: (key: String, value: String) -> Unit) {
        for (key in this.keys())
            func.invoke(key.toString(), this.getString(key.toString()))
    }

    fun String.translate(s: MutableList<Pair<String, String>>): String {
        var result = this
        for (p in s) {
            result = result.replace(p.first, p.second)
        }
        return result
    }
}