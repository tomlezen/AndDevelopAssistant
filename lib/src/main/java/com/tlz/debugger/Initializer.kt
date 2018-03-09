package com.tlz.debugger

import android.support.annotation.Keep
import android.util.Pair
import java.io.File
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException

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

  /**
   * 获取当前手机ip地址.
   */
  @Keep
  @JvmStatic
  fun getPhoneIp(): String {
    try {
      val en = NetworkInterface.getNetworkInterfaces()
      while (en.hasMoreElements()) {
        val intf = en.nextElement()
        val enumIpAddr = intf.inetAddresses
        while (enumIpAddr.hasMoreElements()) {
          val inetAddress = enumIpAddr.nextElement()
          if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
            return inetAddress.getHostAddress().toString()
          }
        }
      }
    } catch (ex: SocketException) {
      ex.printStackTrace()
    }

    return "没有获取到ip地址"
  }

  @Keep
  @JvmStatic
  fun getServerAddress(): String = DebuggerWebServer.serverAddress

}