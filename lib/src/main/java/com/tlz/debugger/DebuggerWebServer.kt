package com.tlz.debugger

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.tlz.debugger.model.AppInfo
import com.tlz.debugger.model.DataResponse
import com.tlz.debugger.model.Db
import com.tlz.debugger.model.KeyValue
import fi.iki.elonen.NanoHTTPD
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream


/**
 * Created by tomlezen.
 * Data: 2018/1/27.
 * Time: 15:00.
 */
class DebuggerWebServer private constructor(private val ctx: Context, port: Int) : NanoHTTPD(port) {

  private val tag = DebuggerWebServer::class.java.canonicalName

  private val dataProvider: DataProvider by lazy { IDataProvider(ctx) }
  private val gson: Gson by lazy { GsonBuilder().create() }

  /** web服务器是否运行. */
  var isRunning = false

  /**
   * 启动服务器.
   */
  fun startServer() {
    if (!isRunning) {
      isRunning = true
      start(10000)
    }
  }

  private fun response(type: String, html: String, cacheTime: String): NanoHTTPD.Response {
    val response = NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, type, html)
    response.addHeader("Cache-Control", "public")
    response.addHeader("Cache-Control", "max-age=" + cacheTime)
    return response
  }

  private fun responseData(data: Any): NanoHTTPD.Response {
    return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, gson.toJson(data))
  }

  private fun responseHtml(html: String): NanoHTTPD.Response {
    return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, NanoHTTPD.MIME_HTML, html)
  }

  private fun responseError(status: Int = 401, errorMsg: String): NanoHTTPD.Response {
    return responseData(com.tlz.debugger.model.Response(status, null, errorMsg))
  }

  override fun serve(session: IHTTPSession?): Response {
    try {
      session?.let {
        val uri = it.uri
        when {
          uri.contains("/api/init") -> {
            val dbs = mutableListOf<Db>()
            dataProvider.getDatabaseList().forEach {
              executeSafely {
                val tabWrapper = dataProvider.getAllTable(it)
                dbs.add(Db(it, tabWrapper.verison, tabWrapper.tables))
              }
            }
            val pkgInfo = ctx.packageManager.getPackageInfo(ctx.packageName, 0)
            return responseData(AppInfo(ctx.getString(ctx.applicationInfo.labelRes), ctx.packageName, pkgInfo.versionName, pkgInfo.versionCode, dbs).toResponse())
          }
          uri.contains("/api/db") -> {
            return if (it.parms.isEmpty() || !it.parms.containsKey("sql") || !it.parms.containsKey("dName") || !it.parms.containsKey("tName")) {
              responseError(errorMsg = "缺少查询参数")
            } else {
              handleDbDataRequest(it.parms)
            }
          }
          uri.contains("/api/del") -> {
            return if (it.parms.isEmpty() || !it.parms.containsKey("dName") || !it.parms.containsKey("tName") || !it.parms.containsKey("where")) {
              responseError(errorMsg = "缺少查询参数")
            } else {
              handleDeleteRequest(it.parms)
            }
          }
          uri.contains("/api/update") -> {
            return if (it.parms.isEmpty() || !it.parms.containsKey("dName") || !it.parms.containsKey("tName") || !it.parms.containsKey("where") || !it.parms.containsKey("data")) {
              responseError(errorMsg = "缺少查询参数")
            } else {
              handleUpdateRequest(it.parms)
            }
          }
        //获取应用logo
          uri == "/image/appIcon" -> {
            try {
              val os = ByteArrayOutputStream()
              BitmapFactory.decodeResource(ctx.resources, ctx.resources.getIdentifier("ic_launcher", "mipmap", ctx.packageName)).compress(Bitmap.CompressFormat.PNG, 100, os)
              val ins = ByteArrayInputStream(os.toByteArray())
              return newChunkedResponse(Response.Status.OK, "image/png", ins)
            } catch (e: Exception) {
              e.printStackTrace()
            }
          }
          else -> {

          }
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
      return responseError(501, "error: ${e.message}")
    }
    return super.serve(session)
  }

  /**
   * 处理数据库数据查询请求.
   */
  private fun handleDbDataRequest(params: Map<String, String>): NanoHTTPD.Response {
    try {
      //数据库名
      val dName = params["dName"] ?: ""
      //表名
      val tName = params["tName"] ?: ""
      //客户端执行的查询语句不能够有排序代码
      val searchSql = with(params["sql"] ?: "select * from $tName") {
        if (this.contains("order by ")) {
          this.substring(0, this.indexOf("order by"))
        } else {
          this
        }
      }
      val draw = params["draw"]?.toInt() ?: 0
      val start = params["start"]?.toInt() ?: 0
      val length = params["length"]?.toInt() ?: -1
      var recordsTotal = 0
      var recordsFiltered = 0
      if (dName == "SharePreferences") {
        val data = dataProvider.executeQuery(dName, tName, "")
        return responseData(DataResponse(draw, data.size, data.size, data))
      } else {
        dataProvider.getTableInfo(dName, tName)?.let {
          //获取where条件
          val where = if (searchSql.contains("where")) searchSql.substring(searchSql.indexOf("where") + 5, searchSql.length) else ""
          //获取查询到的数据最大数量.
          recordsTotal = dataProvider.getTableDataCount(dName, tName, where)
          //获取排序方式
          val orderDir = params["order[0][dir]"] ?: "asc"
          //排序列
          val orderColumn = it.fieldInfos[params["order[0][column]"]?.toInt() ?: 0].name
          //用户输入的过滤字符
          val filterValue = params["search[value]"]
          val filterList = mutableListOf<String>()
          if (!filterValue.isNullOrBlank()) {
            it.fieldInfos.forEach {
              filterList.add("${it.name} like '%$filterValue%'")
            }
          }
          //拼接过滤条件
          var filterWhere = " "
          if (filterList.size == 1) {
            filterWhere = filterList[0]
          } else if (filterList.isNotEmpty()) {
            filterList.forEach { filterWhere += "$it or " }
            filterWhere = filterWhere.substring(0, filterWhere.length - 3)
          }
          //获取过滤后的数据最大数量
          recordsFiltered = if (filterWhere.isNotBlank()) {
            dataProvider.getTableDataCount(dName, tName, "$where $filterWhere")
          } else {
            recordsTotal
          }
          var sql = "select * from $tName"
          val tWhere = where + filterWhere
          if (tWhere.isNotBlank()) {
            sql += " where $tWhere"
          }
          sql += " order by $orderColumn $orderDir"
          if (length >= 0) {
            sql += " limit $start, $length"
          }
          val data = dataProvider.executeQuery(dName, tName, sql)
          return responseData(DataResponse(draw, recordsTotal, recordsFiltered, data))
        }
      }
      return responseData(DataResponse(draw, recordsTotal, recordsFiltered, mutableListOf(), "没有找到${dName}数据库下的${tName}表"))
    } catch (e: Exception) {
      e.printStackTrace()
      return responseData(DataResponse(0, 0, 0, mutableListOf(), "Error: ${e.message}"))
    }
  }

  /**
   * 处理数据更新操作请求.
   */
  private fun handleUpdateRequest(params: Map<String, String>): NanoHTTPD.Response {
    val dName = params["dName"] ?: ""
    val tName = params["tName"] ?: ""
    val where = params["where"] ?: ""
    val data = params["data"] ?: ""
    return if (where.isNotBlank() && data.isNotBlank()) {
      (gson.fromJson<Array<KeyValue>>(data, Array<KeyValue>::class.java))?.let {
        val contentValues = ContentValues()
        it.forEach {
          when (it.type) {
            ConstUtils.TYPE_INTEGER -> contentValues.put(it.key, it.value?.toIntOrNull())
            ConstUtils.TYPE_REAL -> contentValues.put(it.key, it.value?.toDoubleOrNull())
            else -> contentValues.put(it.key, it.value)
          }
        }
        if (dataProvider.updateRow(dName, tName, contentValues, where)) {
          responseData(com.tlz.debugger.model.Response(data = "success"))
        } else {
          responseError(errorMsg = "没有找到匹配的数据")
        }
      } ?: responseError(errorMsg = "更新内容数据解析失败")
    } else {
      responseError(errorMsg = "缺少更新条件或更新内容")
    }
  }

  /**
   * 处理数据删除请求.
   */
  private fun handleDeleteRequest(params: Map<String, String>): NanoHTTPD.Response {
    val dName = params["dName"] ?: ""
    val tName = params["tName"] ?: ""
    val where = params["where"] ?: ""
    return if (where.isNotBlank()) {
      try {
        if (dataProvider.deleteRow(dName, tName, where)) {
          responseData(com.tlz.debugger.model.Response(data = "success"))
        } else {
          responseError(errorMsg = "没有匹配的数据")
        }
      } catch (e: Exception) {
        e.printStackTrace()
        responseError(errorMsg = "缺少删除条件")
      }
    } else {
      responseError(errorMsg = "缺少删除条件")
    }
  }

  /**
   * 处理添加数据请求.
   */
//  private fun handleAddRequest(params: Map<String, String>): NanoHTTPD.Response {
//
//  }


  companion object {
    private const val DEF_PORT = 10000

    @SuppressLint("StaticFieldLeak")
    internal var instance: DebuggerWebServer? = null

    fun start(ctx: Context) {
      instance = instance ?: DebuggerWebServer(ctx, readPort(ctx))
      instance?.startServer()
    }

    /**
     * 读取设置的端口号.
     */
    private fun readPort(ctx: Context): Int {
      return DEF_PORT
    }
  }

}