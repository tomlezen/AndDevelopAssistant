package tomlezen.androiddebuglib

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Pair
import android.widget.TextView
import android.widget.Toast
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tlz.andbase.persmission.RxPermissions
import tomlezen.androiddebuglib.database.CustomDB
import tomlezen.androiddebuglib.database.TestOneDb
import tomlezen.androiddebuglib.database.TestThreeDb
import tomlezen.androiddebuglib.database.TestTwoDb
import tomlezen.androiddebuglib.room.RoomDBTestHelper
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity() {

  @SuppressLint("SetTextI18n")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    val roomDbTestHelper = RoomDBTestHelper(this)
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

      roomDbTestHelper.init()
    }.start()

    if (BuildConfig.DEBUG) {
      try {
        //初始化自定义数据库文件
        val initializer = Class.forName("com.tlz.ada.AdaProvider")
        val method = initializer.getMethod("setCustomDatabaseFiles", Map::class.java)
        val customDatabaseFiles = HashMap<String, Pair<File, String>>()
        customDatabaseFiles["Custom.db"] = Pair(File("${filesDir.absolutePath}/custom_dir/Custom.db"), "")
        method.invoke(null, customDatabaseFiles)

        //初始化内存数据库
        val setInMemoryDbMethod = initializer.getMethod("setInMemoryRoomDatabases", Map::class.java)
        val inMemoryDbs = HashMap<String, SupportSQLiteDatabase>()
        inMemoryDbs[roomDbTestHelper.name] = roomDbTestHelper.inMemoryAppDatabase
        setInMemoryDbMethod.invoke(null, inMemoryDbs)

        //获取服务端地址
        val serverAddressMethod = initializer.getMethod("getAdaServerAddress")
        findViewById<TextView>(R.id.tv_ip).text = "服务器地址：${serverAddressMethod.invoke(null)}"
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }

    RxPermissions.with(this)
        .request(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE))
        .filter { !it }
        .subscribe{
          Toast.makeText(this, "您拒绝了文件读写权限，会导致文件相关功能不可用", Toast.LENGTH_LONG).show()
        }
  }

}
