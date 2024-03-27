package me.zhanghai.kotlin.filesystem.internal

internal class AsciiCharSet private constructor(private val mask0: Long, private val mask64: Long) {
    fun matches(char: Char): Boolean =
        when {
            char < 64.toChar() -> (1L shl char.code) and mask0 != 0L
            char < 128.toChar() -> (1L shl (char.code - 64)) and mask64 != 0L
            else -> false
        }

    infix fun and(other: AsciiCharSet): AsciiCharSet =
        AsciiCharSet(mask0 and other.mask0, mask64 and other.mask64)

    fun inv(): AsciiCharSet = AsciiCharSet(mask0.inv(), mask64.inv())

    infix fun or(other: AsciiCharSet): AsciiCharSet =
        AsciiCharSet(mask0 or other.mask0, mask64 or other.mask64)

    companion object {
        fun of(char: Char): AsciiCharSet {
            var mask64 = 0L
            var mask128 = 0L
            when {
                char < 64.toChar() -> mask64 = mask64 or (1L shl char.code)
                char < 128.toChar() -> mask128 = mask128 or (1L shl (char.code - 64))
                else -> throw IllegalArgumentException("Non-ASCII char '$char'")
            }
            return AsciiCharSet(mask64, mask128)
        }

        fun of(chars: String): AsciiCharSet {
            var mask64 = 0L
            var mask128 = 0L
            for (char in chars) {
                when {
                    char < 64.toChar() -> mask64 = mask64 or (1L shl char.code)
                    char < 128.toChar() -> mask128 = mask128 or (1L shl (char.code - 64))
                    else -> throw IllegalArgumentException("Non-ASCII char '$char'")
                }
            }
            return AsciiCharSet(mask64, mask128)
        }

        fun ofRange(startChar: Char, endCharInclusive: Char): AsciiCharSet {
            require(endCharInclusive < 128.toChar()) {
                "Non-ASCII endCharInclusive ('$endCharInclusive')"
            }
            require(startChar <= endCharInclusive) {
                "startChar ('$startChar') > endCharInclusive ('$endCharInclusive')"
            }
            var mask64 = 0L
            var mask128 = 0L
            for (char in startChar..endCharInclusive) {
                if (char < 64.toChar()) {
                    mask64 = mask64 or (1L shl char.code)
                } else {
                    mask128 = mask128 or (1L shl (char.code - 64))
                }
            }
            return AsciiCharSet(mask64, mask128)
        }
    }
}
