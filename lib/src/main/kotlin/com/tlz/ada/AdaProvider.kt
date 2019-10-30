package com.tlz.ada

import android.util.Pair
import androidx.annotation.Keep
import androidx.sqlite.db.SupportSQLiteDatabase
import java.io.File
import java.net.Inet4Address
import java.net.NetworkInterface

/**
 * 提供给外部访问.
 * Created by tomlezen.
 * Data: 2018/2/24.
 * Time: 13:59.
 */
@Keep
object AdaProvider {

  /**
   * 设置自定义数据库文件.
   * @param files Map<String, Pair<File, String>>
   */
  @Keep
  @JvmStatic
  fun setCustomDatabaseFiles(files: Map<String, Pair<File, String>>) {
    Ada.adaWebServer.setCustomDatabaseFiles(files)
  }

  /**
   * 设置内存数据库(Room).
   * @param databases Map<String, SupportSQLiteDatabase>
   */
  @Keep
  @JvmStatic
  fun setInMemoryRoomDatabases(databases: Map<String, SupportSQLiteDatabase>) {
    Ada.adaWebServer.setInMemoryRoomDatabases(databases)
  }

  /**
   * 获取当前手机ip地址.
   * @return String
   */
  @Keep
  @JvmStatic
  fun getPhoneIp(): String =
      runCatching {
        val en = NetworkInterface.getNetworkInterfaces()
        while (en.hasMoreElements()) {
          val intf = en.nextElement()
          val enumIpAddr = intf.inetAddresses
          while (enumIpAddr.hasMoreElements()) {
            val inetAddress = enumIpAddr.nextElement()
            if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
              return@runCatching inetAddress.getHostAddress().toString()
            }
          }
        }
        "没有获取到ip地址"
      }.getOrNull() ?: "没有获取到ip地址"

  /**
   * 获取服务器地址.
   * @return String
   */
  @Keep
  @JvmStatic
  fun getAdaServerAddress(): String = Ada.adaWebServer.serverAddress
}