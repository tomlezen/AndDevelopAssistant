package com.tlz.ada

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri

/**
 * Created by tomlezen.
 * Data: 2018/1/27.
 * Time: 14:48.
 */
class AndDevelopAssistantInitProvider : ContentProvider() {

  override fun insert(uri: Uri?, values: ContentValues?): Uri? = null

  override fun query(uri: Uri?, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? = null

  override fun onCreate(): Boolean {
    AndDevelopAssistantWebServer.start(context ?: return true)
    return true
  }

  override fun update(uri: Uri?, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0

  override fun delete(uri: Uri?, selection: String?, selectionArgs: Array<out String>?): Int = 0

  override fun getType(uri: Uri?): String = ""


}