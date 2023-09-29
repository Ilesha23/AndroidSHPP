package com.iyakovlev.contacts.utils

import android.util.Log

const val LOG_TAG = "LOG_TAG"

fun log(
    message: String,
    isDebug: Boolean = true
) {
    if (isDebug) Log.e(LOG_TAG, message)
}