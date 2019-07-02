package com.tlz.ada

import android.content.Context
import fi.iki.elonen.NanoHTTPD
import java.io.File

/**
 * Created by Tomlezen.
 * Data: 2018/9/5.
 * Time: 12:51.
 */
class AdaTempFileManagerFactory(private val ctx: Context) : NanoHTTPD.TempFileManagerFactory {

  override fun create(): NanoHTTPD.TempFileManager =
      AndTempFileManager(ctx.externalCacheDir?.absolutePath + "/AndDevelopAssistant")

  class AndTempFileManager(tmpdir: String) : NanoHTTPD.TempFileManager {

    private val tempDirFile = File(tmpdir)
    private val tempFiles = mutableListOf<NanoHTTPD.TempFile>()

    init {
      if (!tempDirFile.exists()) {
        tempDirFile.mkdirs()
      } else {
        // 清空之前未删除掉的文件
        runCatching { tempDirFile.listFiles().forEach { it.delete() } }
      }
    }

    override fun clear() {
      this.tempFiles.forEach {
        runCatching { it.delete() }
      }
      this.tempFiles.clear()
    }

    override fun createTempFile(filename_hint: String?): NanoHTTPD.TempFile =
        NanoHTTPD.DefaultTempFile(tempDirFile).also {
          this.tempFiles += it
        }
  }

}