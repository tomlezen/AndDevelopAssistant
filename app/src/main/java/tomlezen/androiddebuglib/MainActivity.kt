package tomlezen.androiddebuglib

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import tomlezen.androiddebuglib.database.CustomDB
import tomlezen.androiddebuglib.database.TestOneDb
import tomlezen.androiddebuglib.database.TestThreeDb
import tomlezen.androiddebuglib.database.TestTwoDb
import java.io.File
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*
import android.util.Pair

class MainActivity : AppCompatActivity() {

  @SuppressLint("SetTextI18n")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    Thread{
      val prefsOne = getSharedPreferences("TestOne", Context.MODE_PRIVATE)
      val prefsTwo = getSharedPreferences("TestTwo", Context.MODE_PRIVATE)

      prefsOne.edit().apply{
        putString("testString", "string")
        putInt("testInt", 1)
        putLong("testLong", System.currentTimeMillis())
        putFloat("testFloat", Random().nextFloat())
        putBoolean("testBoolean", false)
        putStringSet("testStringSet", setOf("value1", "value2", "value2"))
      }.apply()

      prefsTwo.edit().apply{
        putString("test1", "one").commit()
        putString("test2", "two").commit()
      }.apply()

      CustomDB.create(this)
      TestOneDb.create(this)
      TestTwoDb.create(this)
      TestThreeDb.create(this)

      //初始化自定义数据库文件
      if (BuildConfig.DEBUG) {
        try {
          val initializer = Class.forName("com.tlz.debugger.Initializer")
          val method = initializer.getMethod("customDatabaseFiles", Map::class.java)
          val customDatabaseFiles = HashMap<String, Pair<File, String>>()
          customDatabaseFiles.put("Custom.db", Pair(File("${filesDir.absolutePath}/custom_dir/Custom.db"), ""))
          method.invoke(null, customDatabaseFiles)
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }.start()

    findViewById<TextView>(R.id.tv_ip).text = "您的ip地址是：${getIp()}"
  }

  private fun getIp(): String {
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

}
