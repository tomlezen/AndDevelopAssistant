package com.tlz.debugger

import android.support.annotation.Keep
import java.io.File
import android.util.Pair

/**
 * Created by tomlezen.
 * Data: 2018/2/24.
 * Time: 13:59.
 */
@Keep
object Initializer {

  /**
   * 自定义数据库文件.
   */
  @Keep
  @JvmStatic
  fun customDatabaseFiles(files: Map<String, Pair<File, String>>){
    DebuggerWebServer.setCustomDatabaseFiles(files)
  }

}