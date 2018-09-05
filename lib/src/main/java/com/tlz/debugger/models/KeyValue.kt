package com.tlz.debugger.models

import android.support.annotation.Keep

/**
 * Created by tomlezen.
 * Data: 2018/2/5.
 * Time: 14:55.
 */
@Keep
data class KeyValue(val key: String, val value: String?, val type: String)