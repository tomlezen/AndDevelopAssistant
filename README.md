# Android开发辅助依赖库
主要功能：数据库（支持自定义数据库）、SharePreferences数据查询修改添加；应用实时日志以及历史日志查看；手机所有应用信息查看、apk下载
以及安装应用到手机上；手机文件管理：创建文件、文件夹，删除文件、批量上传文件到手机

数据库相关处理思路参考[Android-Debug-Database](https://github.com/amitshekhariitbhu/Android-Debug-Database)

## 引入依赖

```
Gradle3.0以上： debugImplementation 'com.tlz.tools:androiddebuglib:0.0.4'
Gradle3.0以下： debugCompile 'com.tlz.tools:androiddebuglib:0.0.4'
```

启动app，在浏览器中输入手机端ip地址+10000端口号进行访问（10000是默认端口号），如果不知道手机ip地址，可以在logcat窗口中查看名为DebuggerWebServer的日志，其中会输入完整的访问地址。

## 如何设置数据库密码

需要在Manifest中加入meta-data数据：

```
<meta-data android:name="DB_PAS_数据库名(大写)" android:value="密码" />
```

## 如何设置web访问端口

需要在Manifest中加入meta-data数据：

```
<meta-data android:name="DEBUG_PORT" android:value="10001" />
```

## 如何设置自定义数据库

需要以下模板代码：

```
//初始化自定义数据库文件
      if (BuildConfig.DEBUG) {
        try {
          val initializer = Class.forName("Initializer")
          val method = initializer.getMethod("customDatabaseFiles", Map::class.java)
          val customDatabaseFiles = HashMap<String, Pair<File, String>>()
          customDatabaseFiles.put("Custom.db", Pair(File("${filesDir.absolutePath}/custom_dir/Custom.db"), ""))
          method.invoke(null, customDatabaseFiles)
          // 获取服务器地址
          val serverAddressMethod = initializer.getMethod("getServerAddress")
          serverAddress = serverAddressMethod.invoke(null)
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
 //注：
 //HashMap<String, Pair<File, String>>：第一个String类型是数据库的名字；File是数据据文件，第二个String是数据据密码，特别注意Pair是android.util.Pair类型不是kotlin的Pair类型
```