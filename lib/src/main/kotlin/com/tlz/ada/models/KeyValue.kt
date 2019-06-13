package com.tlz.ada.models

import androidx.annotation.Keep

/**
 * Created by tomlezen.
 * Data: 2018/2/5.
 * Time: 14:55.
 */
@Keep
data class KeyValue(val key: String, val value: String?, val type: String)