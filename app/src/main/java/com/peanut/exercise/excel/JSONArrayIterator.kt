package com.peanut.exercise.excel

import org.json.JSONArray

class JSONArrayIterator<T>(private val j: JSONArray): Iterator<T> {
    private var m = 0
    override fun hasNext(): Boolean = m <= j.length() - 1

    override fun next(): T {
        return (j.get(m++) as T)
    }

}