package com.tlz.ada

import android.annotation.SuppressLint
import android.content.Context
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

  @SuppressLint("StaticFieldLeak")
  lateinit var adaWebServer: AdaWebServer
    internal set

  /** 线程池. */
  private val adaExecutorService by lazy { Executors.newCachedThreadPool() }

  /**
   * 提交执行任务.
   * @param task () -> Unit
   * @return Future<*>
   */
  fun submitTask(task: () -> Unit): Future<*> = adaExecutorService.submit(task)

  fun init(ctx: Context) {
    adaWebServer = AdaWebServer(ctx, ctx.adaServerPort())
  }
}