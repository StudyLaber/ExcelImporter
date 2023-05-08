package com.peanut.exercise.excel

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

class ConfigView: FrameLayout {
    var sheetID: Int = -1
    var sheetName: String = ""

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle){

    }

}