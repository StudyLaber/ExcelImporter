package com.peanut.exercise.excel

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.util.IOUtils
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileInputStream
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter

object ExcelUnit {
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
}