package com.stall.calculator.util

import com.stall.calculator.data.model.ExportBundle
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object ExportCodec {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    fun encode(bundle: ExportBundle): String = json.encodeToString(bundle)

    fun decode(raw: String): ExportBundle = json.decodeFromString(raw)
}
