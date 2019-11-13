# Android开发辅助依赖库
- 数据库增删查改，excel表导出，数据库文件导出
- SharePreferences增删查改
- 应用实时日志查看和历史日志查看，也支持历史日志下载与删除
- 手机所有应用的信息查看，如：Activity、Service、权限等信息，也可以直接下载手机上的应用到电脑上
- 通过web直接安装应用到手机上
- 手机文件管理，包括新增文件、文件夹，删除文件，批量上传文件都手机指定文件夹中
- 应用截图

数据库相关处理思路参考[Android-Debug-Database](https://github.com/amitshekhariitbhu/Android-Debug-Database)
（已知目前数据访问bug：当app应用的数据库对象不关闭时（Room），工具将查询不到数据）

web端使用Angular6开发，因为个人设备和能力有限，没有在太多手机上测试，不能保证100%兼容每台手机

## 如何使用

```
只需要在gradle文件中添加一下代码：
Gradle3.0以上： debugImplementation 'com.tlz.tools:AndDevelopAssistant:0.1.2'
Gradle3.0以下： debugCompile 'com.tlz.tools:AndDevelopAssistant:0.1.2'
非androidx版本:
Gradle3.0以上： debugImplementation 'com.tlz.tools:AndDevelopAssistant:0.1.2-no-x'
Gradle3.0以下： debugCompile 'com.tlz.tools:AndDevelopAssistant:0.1.2-no-x'
```

启动app，在浏览器中输入手机端ip地址+10000端口号进行访问（10000是默认端口号），如果不知道手机ip地址，可以在logcat窗口中查看名为AdaWebServer的日志，其中会输入完整的访问地址。日志记录功能默认开启

## 如何修改web访问端口

需要在build.gradle文件下加入以下代码：

```
debug {
    resValue("string", "ADA_DEBUG_PORT", "你的端口号")
}
```

当手机上有多个应用依赖该库时，就需要自定义端口，避免端口冲突

## 如何设置数据库密码

需要在Manifest中加入meta-data数据：

```
<meta-data android:name="DB_PAS_数据库名(大写)" android:value="密码" />
```

## 如何设置自定义数据库

可以使用以下模板代码：

```
//初始化自定义数据库文件
      if (BuildConfig.DEBUG) {
        try {
          val initializer = Class.forName("com.tlz.ada.AdaProvider")
          val method = initializer.getMethod("setCustomDatabaseFiles", Map::class.java)
          val customDatabaseFiles = HashMap<String, Pair<File, String>>()
          customDatabaseFiles.put("Custom.db", Pair(File("${filesDir.absolutePath}/custom_dir/Custom.db"), ""))
          method.invoke(null, customDatabaseFiles)
          // 获取服务器地址
          val serverAddressMethod = initializer.getMethod("getAdaServerAddress")
          serverAddress = serverAddressMethod.invoke(null)
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
 //注：
 //HashMap<String, Pair<File, String>>：第一个String类型是数据库的名字；File是数据据文件，第二个String是数据据密码，特别注意Pair是android.util.Pair类型不是kotlin的Pair类型
```

## 问题
1. 如果遇到AndroidManifest的FileProider问题，请在你申明的provider加上`tools:replace="android:authorities"`

## 界面截图

<img src="https://github.com/tomlezen/AndDevelopAssistant/blob/master/screenshots/Log.png"></img>
<img src="https://github.com/tomlezen/AndDevelopAssistant/blob/master/screenshots/File.png"></img>
<img src="https://github.com/tomlezen/AndDevelopAssistant/blob/master/screenshots/application.png"></img>
<img src="https://github.com/tomlezen/AndDevelopAssistant/blob/master/screenshots/SharePrefrences.png"></img>
<img src="https://github.com/tomlezen/AndDevelopAssistant/blob/master/screenshots/Database.png"></img>
