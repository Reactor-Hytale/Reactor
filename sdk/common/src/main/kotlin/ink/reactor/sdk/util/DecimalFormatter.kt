package codes.reactor.sdk.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

object DecimalFormatter {
    /**
     * Decimal formatter that uses German locale (comma as decimal separator).
     * Pattern "#.#" ensures at most one decimal digit.
     */
    private val DECIMAL_FORMAT = DecimalFormat("#.#", DecimalFormatSymbols.getInstance(Locale.GERMANY))

    /**
     * Currency formatter for long numbers with thousand separators.
     * Uses US locale (comma as thousand separator, no decimal places).
     * Pattern "#,##0" formats numbers like 1,234,567
     */
    private val LONG_CURRENCY_FORMAT = DecimalFormat("#,##0", DecimalFormatSymbols.getInstance(Locale.US))

    /**
     * Formats a long amount with abbreviations (K, M, B, T, Q).
     *
     * <p>The method converts large numbers into human-readable format with suffixes:
     * <ul>
     *   <li>K (Thousand) - for values >= 1,000</li>
     *   <li>M (Million) - for values >= 1,000,000</li>
     *   <li>B (Billion) - for values >= 1,000,000,000</li>
     *   <li>T (Trillion) - for values >= 1,000,000,000,000</li>
     *   <li>Q (Quadrillion) - for values >= 1,000,000,000,000,000</li>
     * </ul>
     *
     * <p>Examples:
     * <pre>
     * formatCurrency(500)        → "500"
     * formatCurrency(1,500)      → "1,5K"    (uses comma as decimal separator)
     * formatCurrency(1,234,567)  → "1,2M"
     * formatCurrency(5,000,000,000) → "5B"
     * </pre>
     *
     * @param amount the amount to format (non-negative recommended)
     * @return formatted string with appropriate suffix
     */
    fun formatCurrency(amount: Long): String {
        if (amount < 1000L) {
            return amount.toString()
        }
        if (amount < 1000000L) {
            return formatNumber(amount / 1000.0) + 'K'
        }
        if (amount < 1000000000L) {
            return formatNumber(amount / 1000000.0) + 'M'
        }
        if (amount < 1000000000000L) {
            return formatNumber(amount / 1000000000.0) + 'B'
        }
        if (amount < 1000000000000000L) {
            return formatNumber(amount / 1000000000000.0) + 'T'
        }
        return formatNumber(amount / 1000000000000000.0) + 'Q'
    }

    /**
     * Formats a long amount with thousand separators.
     *
     * <p>Uses US locale formatting where commas are used as thousand separators.
     *
     * <p>Examples:
     * <pre>
     * formatCurrencyLong(1234)      → "1,234"
     * formatCurrencyLong(1234567)   → "1,234,567"
     * formatCurrencyLong(1000000)   → "1,000,000"
     * </pre>
     *
     * @param amount the amount to format
     * @return formatted string with thousand separators, or null if formatting fails
     */
    fun formatCurrencyLong(amount: Long): String? {
        return LONG_CURRENCY_FORMAT.format(amount)
    }

    /**
     * Formats a double value with at most one decimal place.
     *
     * <p>Uses German locale formatting where comma (,) is used as the decimal separator.
     * Removes trailing ".0" to provide cleaner output.
     *
     * <p>Examples:
     * <pre>
     * formatNumber(123.0)    → "123"
     * formatNumber(123.4)    → "123,4"
     * formatNumber(123.45)   → "123,4"   (rounded due to "#.#" pattern)
     * formatNumber(123.9)    → "123,9"
     * </pre>
     *
     * @param value the double value to format
     * @return formatted string without trailing ".0"
     */
    fun formatNumber(value: Double): String {
        val formatted = DECIMAL_FORMAT.format(value)

        if (formatted.endsWith(".0")) {
            return formatted.substring(0, formatted.length - 2)
        }

        return formatted
    }
}
