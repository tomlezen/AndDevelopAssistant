package com.tlz.ada

import android.annotation.SuppressLint
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.util.concurrent.Executors

/**
 * Created by Tomlezen.
 * Date: 2019-06-13.
 * Time: 23:04.
 */
object Ada {
  /** Gson实例. */
  val adaGson: Gson by lazy { GsonBuilder().create() }

  /** 线程池. */
  val adaExcutorService by lazy { Executors.newCachedThreadPool() }

  @SuppressLint("StaticFieldLeak")
  lateinit var adaWebServer: AdaWebServer
    internal set
}