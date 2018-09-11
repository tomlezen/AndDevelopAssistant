package com.tlz.ada

import android.content.Context
import fi.iki.elonen.NanoHTTPD
import java.io.File

/**
 * Created by Tomlezen.
 * Data: 2018/9/5.
 * Time: 12:51.
 */
class AndTempFileManagerFactory(private val ctx: Context) : NanoHTTPD.TempFileManagerFactory {

	override fun create(): NanoHTTPD.TempFileManager =
			AndTempFileManager(ctx.externalCacheDir.absolutePath + "/AndDevelopAssistant")

	class AndTempFileManager(tmpdir: String) : NanoHTTPD.TempFileManager {

		private val tempDirFile = File(tmpdir)
		private val tempFiles = mutableListOf<NanoHTTPD.TempFile>()

		init {
			if (!tempDirFile.exists()) {
				tempDirFile.mkdirs()
			}
		}

		override fun clear() {
			this.tempFiles.forEach {
				try {
					it.delete()
				} catch (ignored: Exception) {
					ignored.printStackTrace()
				}
			}
			this.tempFiles.clear()
		}

		override fun createTempFile(filename_hint: String?): NanoHTTPD.TempFile {
			val tempFile = NanoHTTPD.DefaultTempFile(tempDirFile)
			this.tempFiles.add(tempFile)
			return tempFile
		}
	}

}