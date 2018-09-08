package com.tlz.debugger.models

import android.support.annotation.Keep

/**
 * Created by Tomlezen.
 * Date: 2018/9/8.
 * Time: 下午8:25.
 * @param type String 日志类型.
 * @param level Int 日志级别.
 * @param content String 处理后的日志内容.
 * @param originalContent String 原始日志内容.
 */
@Keep
data class Log(val type: String, val level: Int, var content: String, var originalContent: String)