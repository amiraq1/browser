package com.ammar.browser.utils

import android.app.Activity
import androidx.core.app.ActivityOptionsCompat
import com.ammar.browser.R

/**
 * Standard slide-in/slide-out [ActivityOptionsCompat] for opening a child
 * Activity from any screen in Nabd.
 *
 * Use:
 *   `startActivity(intent, nabdSlideOptions().toBundle())`
 *   `someLauncher.launch(intent, nabdSlideOptions())`
 *
 * Pure visual polish — does not affect Activity behavior, lifecycle, or
 * back-stack semantics. Falls back gracefully on older devices because
 * [ActivityOptionsCompat] handles the API check internally.
 */
fun Activity.nabdSlideOptions(): ActivityOptionsCompat =
    ActivityOptionsCompat.makeCustomAnimation(
        this,
        R.anim.nabd_slide_in_right,
        R.anim.nabd_slide_out_left
    )
