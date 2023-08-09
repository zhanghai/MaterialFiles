/*
 * Copyright (c) 2023 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.core.app.LocaleManagerCompat
import androidx.core.os.LocaleListCompat
import androidx.preference.ListPreference
import androidx.preference.Preference.SummaryProvider
import me.zhanghai.android.files.R
import me.zhanghai.android.files.app.application
import me.zhanghai.android.files.compat.LocaleConfigCompat
import me.zhanghai.android.files.util.toList
import java.util.Locale

class LocalePreference : ListPreference {
    lateinit var setApplicationLocalesPre33: (LocaleListCompat) -> Unit

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    )

    constructor(
        context: Context,
        attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int,
        @StyleRes defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        val context = context
        val systemDefaultEntry = context.getString(R.string.system_default)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Prefer using the system setting because it has better support for locales.
            intent = Intent(
                Settings.ACTION_APP_LOCALE_SETTINGS,
                Uri.fromParts("package", context.packageName, null)
            )
            summaryProvider = SummaryProvider<LocalePreference> {
                applicationLocale?.sentenceCasedLocalizedDisplayName ?: systemDefaultEntry
            }
        } else {
            setDefaultValue(VALUE_SYSTEM_DEFAULT)
            val supportedLocales = LocaleConfigCompat(context).supportedLocales!!.toList()
                .sortedBy { it.toLanguageTag() }
            entries = supportedLocales.mapTo(mutableListOf(systemDefaultEntry)) {
                it.sentenceCasedLocalizedDisplayName
            }.toTypedArray<CharSequence>()
            entryValues =
                supportedLocales
                    .mapTo(mutableListOf(VALUE_SYSTEM_DEFAULT)) { it.toLanguageTag() }
                    .toTypedArray<CharSequence>()
            summaryProvider = SimpleSummaryProvider.getInstance()
        }
    }

    private val Locale.sentenceCasedLocalizedDisplayName: String
        // See com.android.internal.app.LocaleHelper.toSentenceCase() for a proper case conversion
        // implementation which requires android.icu.text.CaseMap that's only available on API 29+.
        @Suppress("DEPRECATION")
        get() = getDisplayName(this).capitalize(this)

    override fun getPersistedString(defaultReturnValue: String?): String =
        applicationLocale?.toLanguageTag() ?: VALUE_SYSTEM_DEFAULT

    override fun persistString(value: String?): Boolean {
        applicationLocale = if (value != null && value != VALUE_SYSTEM_DEFAULT) {
            Locale.forLanguageTag(value)
        } else {
            null
        }
        return true
    }

    private var applicationLocale: Locale?
        get() = LocaleManagerCompat.getApplicationLocales(application).toList().firstOrNull()
        set(value) {
            check(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
            if (value == applicationLocale) {
                return
            }
            val locales = if (value != null) {
                LocaleListCompat.create(value)
            } else {
                LocaleListCompat.getEmptyLocaleList()
            }
            setApplicationLocalesPre33(locales)
        }

    override fun onClick() {
        // Don't show dialog if we have an intent.
        if (intent != null) {
            return
        }

        super.onClick()
    }

    // Exposed for SettingsPreferenceFragment.onResume().
    public override fun notifyChanged() {
        super.notifyChanged()
    }

    companion object {
        private const val VALUE_SYSTEM_DEFAULT = ""
    }
}
