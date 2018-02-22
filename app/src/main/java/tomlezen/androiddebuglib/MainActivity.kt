package tomlezen.androiddebuglib

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import tomlezen.androiddebuglib.database.CarDBHelper
import tomlezen.androiddebuglib.database.ContactDBHelper
import tomlezen.androiddebuglib.database.ExtTestDBHelper
import tomlezen.androiddebuglib.database.PersonDBHelper
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*


class MainActivity : AppCompatActivity() {

  @SuppressLint("SetTextI18n")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    Thread{
      val stringSet = HashSet<String>()
      stringSet.add("SetOne")
      stringSet.add("SetTwo")
      stringSet.add("SetThree")

      val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)

      val prefsOne = getSharedPreferences("countPrefOne", Context.MODE_PRIVATE)
      val prefsTwo = getSharedPreferences("countPrefTwo", Context.MODE_PRIVATE)

      sharedPreferences.edit().putString("testOne", "one").commit()
      sharedPreferences.edit().putInt("testTwo", 2).commit()
      sharedPreferences.edit().putLong("testThree", 100000L).commit()
      sharedPreferences.edit().putFloat("testFour", 3.01f).commit()
      sharedPreferences.edit().putBoolean("testFive", true).commit()
      sharedPreferences.edit().putStringSet("testSix", stringSet).commit()

      prefsOne.edit().putString("testOneNew", "one").commit()

      prefsTwo.edit().putString("testTwoNew", "two").commit()

      val contactDBHelper = ContactDBHelper(applicationContext)
      if (contactDBHelper.count() == 0) {
        for (i in 0..99) {
          val name = "name_" + i
          val phone = "phone_" + i
          val email = "email_" + i
          val street = "street_" + i
          val place = "place_" + i
          contactDBHelper.insertContact(name, phone, email, street, null)
        }
      }

      val carDBHelper = CarDBHelper(applicationContext)
      if (carDBHelper.count() == 0) {
        for (i in 0..49) {
          val name = "name_" + i
          val color = "RED"
          val mileage = i + 10.45f
          carDBHelper.insertCar(name, color, mileage)
        }
      }

      val extTestDBHelper = ExtTestDBHelper(applicationContext)
      if (extTestDBHelper.count() == 0) {
        (0..19)
            .map { "value_" + it }
            .forEach { extTestDBHelper.insertTest(it) }
      }

      // Create Person encrypted database
      val personDBHelper = PersonDBHelper(applicationContext)
      if (personDBHelper.count() == 0) {
        for (i in 0..99) {
          val firstName = PersonDBHelper.PERSON_COLUMN_FIRST_NAME + "_" + i
          val lastName = PersonDBHelper.PERSON_COLUMN_LAST_NAME + "_" + i
          val address = PersonDBHelper.PERSON_COLUMN_ADDRESS + "_" + i
          personDBHelper.insertPerson(firstName, lastName, address)
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
