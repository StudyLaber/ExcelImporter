package com.peanut.exercise.excel

import android.annotation.SuppressLint
import android.content.Context
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.LinearLayout

/**
 * @function 对外完全透明，返回普通的checkbox列表，通过getAns()来获取答案
 * @param checkboxTexts 每一个checkbox的显示文字的集合，返回的答案根据这个顺序。

 * @param random 是否开启乱序显示，包括产生随机种子，映射算法，对外透明
 * @param questionType 题目类型，用于返回
 */
@SuppressLint("ViewConstructor")
class CheckboxList(
    mContext: Context,
    private val checkboxTexts: List<CharSequence>,
    private val random: Boolean = false,
    private val questionType: QuestionType,
    private val onCheck: (() -> Unit)? = null,
) : LinearLayout(mContext) {
    private val stringAnswerLetter =
        if (questionType == QuestionType.PD) "YN" else "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private var seed: IntArray? = null
    private val checkBoxes: MutableList<CheckBox> = ArrayList()
    private var userSelection: IntArray? = null

    companion object {
        val pdStringValue = arrayOf("Y", "N")
        val xzStringValue = arrayOf(
            "A", "B", "C", "D", "E", "F", "G", "H",
            "I", "J", "K", "L", "M", "N", "O", "P",
            "Q", "R", "S", "T", "U", "V", "W", "X",
            "Y", "Z"
        )

        /**
         * this:ABCD/B
         * int array:0,1,2,3/1
         */
        fun String.toXzIntArray(): IntArray = mutableListOf<Int>().apply {
            for (c in this@toXzIntArray)
                if (c in 'A'..'Z') this.add(c - 'A')
        }.toIntArray()

        /**
         * this:Y/N
         * int array:0/1
         */
        fun String.toPdIntArray(): IntArray = mutableListOf<Int>().apply {
            if (this@toPdIntArray.isNotBlank() and (this@toPdIntArray.length == 1))
                if (this@toPdIntArray[0] == 'Y') this.add(0)
                else if (this@toPdIntArray[0] == 'N') this.add(1)
        }.toIntArray()

        /**
         * this:0,1,2,3/1
         * int array:ABCD/B
         */
        fun IntArray.toXzStringValue(): String = StringBuilder().apply {
            for (i in this@toXzStringValue)
                this.append(xzStringValue[i])
        }.toString()

        /**
         * this:0,1,2,3/1
         * int array:ABCD/B
         */
        fun IntArray.toPdStringValue(): String = StringBuilder().apply {
            for (i in this@toPdStringValue)
                this.append(pdStringValue[i])
        }.toString()

        fun List<CharSequence>.requireNotEmpty(): List<CharSequence> {
            val a = mutableListOf<CharSequence>()
            for (c in this)
                if (c.isNotEmpty() and c.isNotBlank())
                    a.add(c)
            return a
        }

    }

    /**
     * selectionMode 选择模式 （从题目类型自动推断，只有多选题是多选）
     */
    private val selectionMode: SelectionMode =
        if (questionType == QuestionType.DD) SelectionMode.MULTIPLE else SelectionMode.SINGLE

    init {
        this.orientation = VERTICAL
        for (idx in checkboxTexts.indices) {
            //映射选项文本，为了获取原答案，需要在getUserAns()映射一次checkbox的位置
            val option = checkboxTexts[seed?.get(idx) ?: idx]
            this.addView(CheckBox(mContext).apply {
                this.text = option
                this.setOnCheckedChangeListener { _, isChecked -> if (isChecked) onCheck?.invoke() }
                checkBoxes.add(this)
                this.setOnClickListener {
                    syncCheckBoxes(this)
                }
            })
        }
    }

    private fun syncCheckBoxes(button: CompoundButton) {
        if (selectionMode == SelectionMode.SINGLE)
            for (a in checkBoxes) if (a != button) a.isChecked = false
    }

    fun getSelection(real: IntArray): Pair<IntArray, IntArray> {
        var list = listOf<Int>()
        for ((idx, checkBox) in checkBoxes.withIndex()) {
            if (checkBox.isChecked) {
                list = list.plus(idx)
            }
        }
        return list.toIntArray() to real.translate()
    }

    fun select(selection: IntArray) {
        selection.translate().let {
            for (idx in checkBoxes.indices) {
                checkBoxes[idx].isChecked = idx in it
                checkBoxes[idx].isClickable = false
            }
        }
    }

    private fun IntArray.translate(): IntArray {
        for (idx in this.indices)
            this[idx] = seed?.indexOf(this[idx]) ?: this[idx]
        return this.sorted().toIntArray()
    }

    private fun IntArray.toStringAns(type: String): String {
        val sb = StringBuilder()
        this.forEach { item ->
            sb.append(type[item])
        }
        return sb.toString()
    }
}

enum class SelectionMode {
    SINGLE, MULTIPLE
}

enum class QuestionType {
    //> .table
    //> .schema table
    PD,//判断题
    DX,//单选题
    DD,//多选题
    TK,//填空题
    JD,//简答题

    /**
     * CREATE TABLE YD (
     *    QID INTEGER PRIMARY KEY AUTOINCREMENT,
     *    Topic TEXT NOT NULL,
     *    OptionList TEXT,
     *    Result TEXT,
     *    Explain TEXT,
     *    chapId tinyint
     * );
     */
    YD //阅读题
}