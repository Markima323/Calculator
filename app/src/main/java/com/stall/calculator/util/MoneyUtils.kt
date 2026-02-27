package com.stall.calculator.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

object MoneyUtils {
    private val formatter = DecimalFormat("0.00")

    fun formatCents(cents: Long, showSymbol: Boolean): String {
        val value = BigDecimal(cents).divide(BigDecimal(100), 2, RoundingMode.HALF_UP)
        val number = formatter.format(value)
        return if (showSymbol) "¥$number" else number
    }

    fun parsePriceToCents(raw: String): Long? {
        val normalized = raw.trim().replace("¥", "")
        if (normalized.isEmpty()) return null
        return try {
            val decimal = BigDecimal(normalized)
            if (decimal < BigDecimal.ZERO) return null
            decimal
                .multiply(BigDecimal(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact()
        } catch (_: Exception) {
            null
        }
    }
}
