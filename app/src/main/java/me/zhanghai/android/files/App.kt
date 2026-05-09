/*
 * Auto-added Application subclass to inject a visible back button into activities.
 */

package me.zhanghai.android.files

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageButton
import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.util.dpToDimensionPixelSize
import me.zhanghai.android.files.util.valueCompat
import androidx.lifecycle.LifecycleOwner

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                try {
                    val decor = activity.window.decorView as ViewGroup
                    // Avoid injecting twice
                    if (decor.findViewWithTag<View>("__epaper_back_button") != null) return

                    val size = activity.dpToDimensionPixelSize(40)
                    val margin = activity.dpToDimensionPixelSize(8)

                    val backButton = AppCompatImageButton(activity).apply {
                        setImageResource(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
                        contentDescription = "Back"
                        isFocusable = true
                        isClickable = true
                        tag = "__epaper_back_button"
                        // Make background transparent to fit e-paper
                        background = null
                    }

                    val lp = FrameLayout.LayoutParams(size, size, Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM).apply {
                        bottomMargin = margin
                    }

                    // Add to decor view (top-most). Wrap in FrameLayout if needed.
                    if (decor is FrameLayout) {
                        decor.addView(backButton, lp)
                    } else {
                        val container = FrameLayout(activity)
                        container.layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        // move children into container
                        while (decor.childCount > 0) {
                            val child = decor.getChildAt(0)
                            decor.removeViewAt(0)
                            container.addView(child)
                        }
                        decor.addView(container)
                        container.addView(backButton, lp)
                    }
 
                    if (activity is LifecycleOwner) {
                        Settings.EPAPER_BACK_BUTTON.observe(activity) { enabled ->
                            backButton.visibility = if (enabled) View.VISIBLE else View.GONE
                        }
                    } else {
                        backButton.visibility = if (Settings.EPAPER_BACK_BUTTON.valueCompat) View.VISIBLE else View.GONE
                    }

                    backButton.setOnClickListener {
                        try {
                            if (activity is androidx.activity.ComponentActivity) {
                                activity.onBackPressedDispatcher.onBackPressed()
                            } else {
                                @Suppress("DEPRECATION")
                                activity.onBackPressed()
                            }
                        } catch (e: Exception) {
                            activity.finish()
                        }
                    }
                } catch (ignored: Exception) {
                }
            }

            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {
                try {
                    val decor = activity.window.decorView as ViewGroup
                    val v = decor.findViewWithTag<View>("__epaper_back_button")
                    if (v != null) {
                        (v.parent as? ViewGroup)?.removeView(v)
                    }
                } catch (ignored: Exception) {}
            }
        })
    }
}
