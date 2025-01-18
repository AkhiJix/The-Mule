package com.akhijix.themule.utils

import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import kotlin.time.Duration

fun Fragment.showSnackbar(
    message: String,
    duration: Int = Snackbar.LENGTH_LONG,
    view: View = requireView()
) {
    Snackbar.make(view,message,duration).show()
}

val <T> T.exhaustive : T
    get() = this