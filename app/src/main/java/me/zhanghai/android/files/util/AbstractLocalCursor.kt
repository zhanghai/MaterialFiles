package me.zhanghai.android.files.util

import android.content.ContentResolver
import android.database.CharArrayBuffer
import android.database.ContentObserver
import android.database.Cursor
import android.database.CursorIndexOutOfBoundsException
import android.database.DataSetObserver
import android.net.Uri
import android.os.Bundle

abstract class AbstractLocalCursor : Cursor {
    private var _position: Int = -1

    private var _isClosed: Boolean = false

    private var _extras = Bundle.EMPTY

    override fun getPosition(): Int = _position

    override fun move(offset: Int): Boolean = moveToPosition(_position + offset)

    override fun moveToPosition(position: Int): Boolean {
        val count = count
        return when {
            position >= count -> {
                _position = count
                false
            }
            position < 0 -> {
                _position = -1
                false
            }
            else -> {
                _position = position
                true
            }
        }
    }

    override fun moveToFirst(): Boolean = moveToPosition(0)

    override fun moveToLast(): Boolean = moveToPosition(count - 1)

    override fun moveToNext(): Boolean = moveToPosition(_position + 1)

    override fun moveToPrevious(): Boolean = moveToPosition(_position - 1)

    override fun isFirst(): Boolean {
        val count = count
        return count != 0 && _position == 0
    }

    override fun isLast(): Boolean {
        val count = count
        return count != 0 && _position == count - 1
    }

    override fun isBeforeFirst(): Boolean {
        val count = count
        return count == 0 || _position == -1
    }

    override fun isAfterLast(): Boolean {
        val count = count
        return count == 0 || _position == count
    }

    override fun getColumnIndex(columnName: String): Int {
        for ((index, columnNameForIndex) in columnNames.withIndex()) {
            if (columnNameForIndex.equals(columnName, true)) {
                return index
            }
        }
        return -1
    }

    override fun getColumnIndexOrThrow(columnName: String): Int =
        getColumnIndex(columnName).also {
            require(it != -1) {
                "Column '$columnName' does not exist, available columns: ${
                    columnNames.contentToString()}"
            }
        }

    override fun getColumnName(columnIndex: Int): String = columnNames[columnIndex]

    override fun getColumnCount(): Int = columnNames.size

    private fun getObjectChecked(columnIndex: Int): Any? {
        val columnCount = columnCount
        if (columnIndex !in 0 until columnCount) {
            throw CursorIndexOutOfBoundsException(
                "Requested column: $columnIndex, # of columns: $columnCount"
            )
        }
        val count = count
        if (_position !in 0 until count) {
            throw CursorIndexOutOfBoundsException(_position, count)
        }
        return getObject(columnIndex)
    }

    protected abstract fun getObject(columnIndex: Int): Any?

    override fun getBlob(columnIndex: Int): ByteArray? = getObjectChecked(columnIndex) as ByteArray?

    override fun getString(columnIndex: Int): String? = getObjectChecked(columnIndex)?.toString()

    override fun copyStringToBuffer(columnIndex: Int, buffer: CharArrayBuffer) {
        val value = getString(columnIndex)
        if (value != null) {
            val data = buffer.data
            if (data == null || data.size < value.length) {
                buffer.data = value.toCharArray()
            } else {
                value.toCharArray(data, 0, 0, value.length)
            }
            buffer.sizeCopied = value.length
        } else {
            buffer.sizeCopied = 0
        }
    }

    override fun getShort(columnIndex: Int): Short =
        when (val value = getObjectChecked(columnIndex)) {
            null -> 0
            is Number -> value.toShort()
            else -> value.toString().toShort()
        }

    override fun getInt(columnIndex: Int): Int =
        when (val value = getObjectChecked(columnIndex)) {
            null -> 0
            is Number -> value.toInt()
            else -> value.toString().toInt()
        }

    override fun getLong(columnIndex: Int): Long =
        when (val value = getObjectChecked(columnIndex)) {
            null -> 0
            is Number -> value.toLong()
            else -> value.toString().toLong()
        }

    override fun getFloat(columnIndex: Int): Float =
        when (val value = getObjectChecked(columnIndex)) {
            null -> 0f
            is Number -> value.toFloat()
            else -> value.toString().toFloat()
        }

    override fun getDouble(columnIndex: Int): Double =
        when (val value = getObjectChecked(columnIndex)) {
            null -> 0.0
            is Number -> value.toDouble()
            else -> value.toString().toDouble()
        }

    override fun getType(columnIndex: Int): Int =
        when (getObjectChecked(columnIndex)) {
            null -> Cursor.FIELD_TYPE_NULL
            is Byte, is Short, is Int, is Long -> Cursor.FIELD_TYPE_INTEGER
            is Float, is Double -> Cursor.FIELD_TYPE_FLOAT
            is ByteArray -> Cursor.FIELD_TYPE_BLOB
            else -> Cursor.FIELD_TYPE_STRING
        }

    override fun isNull(columnIndex: Int): Boolean = getObjectChecked(columnIndex) == null

    override fun deactivate() {}

    override fun requery(): Boolean = true

    override fun close() {
        _isClosed = true
    }

    override fun isClosed(): Boolean = _isClosed

    override fun registerContentObserver(observer: ContentObserver) {}

    override fun unregisterContentObserver(observer: ContentObserver) {}

    override fun registerDataSetObserver(observer: DataSetObserver) {}

    override fun unregisterDataSetObserver(observer: DataSetObserver) {}

    override fun setNotificationUri(resolver: ContentResolver, uri: Uri) {
        throw UnsupportedOperationException()
    }

    override fun getNotificationUri(): Uri? = null

    override fun getWantsAllOnMoveCalls(): Boolean = false

    override fun setExtras(extras: Bundle?) {
        _extras = extras ?: Bundle.EMPTY
    }

    override fun getExtras(): Bundle = _extras

    override fun respond(extras: Bundle): Bundle = Bundle.EMPTY
}
