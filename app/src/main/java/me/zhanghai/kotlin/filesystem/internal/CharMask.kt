package me.zhanghai.kotlin.filesystem.internal

internal class CharMask private constructor(private val mask64: Long, private val mask128: Long) {
    fun matches(char: Char): Boolean =
        when {
            char < 64.toChar() -> (1L shl char.code) and mask64 != 0L
            char < 128.toChar() -> (1L shl (char.code - 64)) and mask128 != 0L
            else -> false
        }

    infix fun and(other: CharMask): CharMask =
        CharMask(mask64 and other.mask64, mask128 and other.mask128)

    fun inv(): CharMask = CharMask(mask64.inv(), mask128.inv())

    infix fun or(other: CharMask): CharMask =
        CharMask(mask64 or other.mask64, mask128 or other.mask128)

    companion object {
        fun of(char: Char): CharMask {
            var mask64 = 0L
            var mask128 = 0L
            when {
                char < 64.toChar() -> mask64 = mask64 or (1L shl char.code)
                char < 128.toChar() -> mask128 = mask128 or (1L shl (char.code - 64))
                else -> throw IllegalArgumentException("Non-ASCII char '$char'")
            }
            return CharMask(mask64, mask128)
        }

        fun of(chars: String): CharMask {
            var mask64 = 0L
            var mask128 = 0L
            for (char in chars) {
                when {
                    char < 64.toChar() -> mask64 = mask64 or (1L shl char.code)
                    char < 128.toChar() -> mask128 = mask128 or (1L shl (char.code - 64))
                    else -> throw IllegalArgumentException("Non-ASCII char '$char'")
                }
            }
            return CharMask(mask64, mask128)
        }

        fun ofRange(startChar: Char, endCharInclusive: Char): CharMask {
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
            return CharMask(mask64, mask128)
        }
    }
}
