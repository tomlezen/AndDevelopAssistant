package com.tlz.ada

import android.annotation.SuppressLint
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.util.concurrent.Executors
import java.util.concurrent.Future

/**
 * Created by Tomlezen.
 * Date: 2019-06-13.
 * Time: 23:04.
 */
object Ada {
  /** Gson实例. */
  val adaGson: Gson by lazy { GsonBuilder().create() }

  /** 线程池. */
  private val adaExcutorService by lazy { Executors.newCachedThreadPool() }

  /**
   * 提交执行任务.
   * @param task () -> Unit
   * @return Future<*>
   */
  fun submitTask(task: () -> Unit): Future<*> = adaExcutorService.submit(task)

  @SuppressLint("StaticFieldLeak")
  lateinit var adaWebServer: AdaWebServer
    internal set
}