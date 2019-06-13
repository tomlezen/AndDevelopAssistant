package com.tlz.ada.exceptions

/**
 * 异常.
 * Created by Tomlezen.
 * Date: 2019-06-13.
 * Time: 22:08.
 */
class AdaException(message: String, val code: Int = 0) : Exception(message)