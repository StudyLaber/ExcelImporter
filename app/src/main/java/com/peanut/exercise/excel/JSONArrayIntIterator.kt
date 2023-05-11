package com.peanut.exercise.excel

import org.json.JSONArray

class JSONArrayIntIterator(private val j: JSONArray): Iterator<Int> {
    private var m = -1
    override fun hasNext(): Boolean = m <= j.length() - 1

    override fun next(): Int = j.getInt(++m)

}