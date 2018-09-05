package com.tlz.debugger.models

/**
 * Created by Tomlezen.
 * Date: 2018/9/5.
 * Time: 下午9:35.
 */
class FileInfo(
		val name: String,
		val isDir: Boolean = false,
		val path: String,
		val size: Long,
		val isRead: Boolean,
		val isWrite: Boolean,
		val isHidden: Boolean,
		val modifyTime: Long,
		val children: List<FileInfo> = listOf()
)