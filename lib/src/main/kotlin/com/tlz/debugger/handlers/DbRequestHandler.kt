package com.tlz.debugger.handlers

import com.tlz.debugger.*
import com.tlz.debugger.ConstUtils.DATA
import com.tlz.debugger.ConstUtils.DB_NAME
import com.tlz.debugger.ConstUtils.PAGE_INDEX
import com.tlz.debugger.ConstUtils.PAGE_SIZE
import com.tlz.debugger.ConstUtils.SQL
import com.tlz.debugger.ConstUtils.TABLE_NAME
import com.tlz.debugger.ConstUtils.WHERE
import com.tlz.debugger.models.DataResponse
import com.tlz.debugger.models.KeyValue
import fi.iki.elonen.NanoHTTPD

/**
 * 数据库请求处理.
 *
 * Created by Tomlezen.
 * Data: 2018/9/5.
 * Time: 14:50.
 */
class DbRequestHandler(private val dataProvider: DataProvider) : RequestHandler {

	override fun onRequest(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response? {
		val uri = session.uri
		return when (uri) {
			"/api/db/select" -> if (session.parms[DB_NAME] == "SharePreferences") {
				session.verifyParams(::handleSqSelectRequest, SQL, DB_NAME, TABLE_NAME)
			} else {
				session.verifyParams(::handleDbSelectRequest, SQL, DB_NAME, TABLE_NAME, PAGE_INDEX, PAGE_SIZE)
			}
			"/api/db/del" -> session.verifyParams(::handleDbDel, DB_NAME, TABLE_NAME, WHERE)
			"/api/db/add" -> session.verifyParams(::handleDbAdd, DB_NAME, TABLE_NAME, DATA)
			"/api/db/update" -> session.verifyParams(::handleDbUpdate, DB_NAME, TABLE_NAME, WHERE, DATA)
			"/api/db/execute" -> session.verifyParams(::handleDbExecute, DB_NAME, SQL)
			"/api/db/download" -> session.verifyParams(::handleDbDownload, DB_NAME)
			else -> null
		}
	}

	/**
	 * 处理SharePreferences查询请求.
	 * @param session NanoHTTPD.IHTTPSession
	 * @return NanoHTTPD.Response
	 */
	private fun handleSqSelectRequest(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response =
			handleRequestSafely {
				val dName = session.parms[DB_NAME] ?: ""
				val tName = session.parms[TABLE_NAME] ?: ""
				val tabInfo = dataProvider.getTableInfo(dName, tName)
				val orderColumn = tabInfo?.fieldInfos?.find { session.parms.containsKey(it.name) }?.name
						?: ""
				val orderDir = session.parms[orderColumn] ?: ""
				val data = dataProvider.executeQuery(dName, tName, orderDir)
				return@handleRequestSafely responseData(DataResponse(data.size, data.size, data))
			}

	/**
	 * 处理数据库查询请求.
	 * @param session NanoHTTPD.IHTTPSession
	 * @return NanoHTTPD.Response
	 */
	private fun handleDbSelectRequest(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response =
			handleRequestSafely {
				val params = session.parms
				val dName = params[DB_NAME] ?: ""
				val tName = params[TABLE_NAME] ?: ""
				val originalSql = params[SQL] ?: "select * from $tName"
				val pageSize = params[PAGE_SIZE]?.toInt() ?: 10
				val pageIndex = params[PAGE_INDEX]?.toInt() ?: 1
				var length = pageSize
				var start = pageSize * (pageIndex - 1)
				val tabInfo = dataProvider.getTableInfo(dName, tName)
				val orderColumn = tabInfo?.fieldInfos?.find { params.containsKey(it.name) }?.name ?: ""
				val orderDir = params[orderColumn] ?: ""
				var recordsTotal = 0
				var recordsFiltered = 0
				dataProvider.getTableInfo(dName, tName)?.let {
					var limitStart = 0
					var limitLength = -1
					var tSearchSql = originalSql
					//分割limit限制
					if (tSearchSql.contains("limit")) {
						executeSafely {
							val index = tSearchSql.indexOf("limit")
							val limitStr = tSearchSql.substring(index + 5, tSearchSql.length)
							val limitParams = limitStr.split(",")
							if (limitParams.size == 1) {
								limitLength = limitParams.first().trim().toInt()
								//修改查询条数
								length = rMin(length, limitLength)
							} else if (limitParams.size == 2) {
								limitLength = limitParams.last().trim().toInt()
								limitStart = limitParams.first().trim().toInt()
								start += limitStart
								length = rMin(length, limitLength)
							}
							tSearchSql = tSearchSql.substring(0, index)
						}
					}
					//客户端执行的查询语句不能够有排序代码
					val searchSql = if (tSearchSql.contains("order by ")) tSearchSql.substring(0, tSearchSql.indexOf("order by")) else tSearchSql
					//获取where条件
					val where = if (searchSql.contains("where")) searchSql.substring(searchSql.indexOf("where") + 5, searchSql.length) else ""
					//获取查询到的数据最大数量.
					recordsTotal = dataProvider.getTableDataCount(dName, tName, where)
					if (recordsTotal - limitStart >= 0) {
						recordsTotal -= limitStart
					}
					recordsTotal = rMin(recordsTotal, limitLength)
					//用户输入的过滤字符
					val filterValue = params["search"]
					val filterList = mutableListOf<String>()
					if (!filterValue.isNullOrBlank()) {
						it.fieldInfos.forEach { fieldInfo ->
							filterList.add("${fieldInfo.name} like '%$filterValue%'")
						}
					}
					//拼接过滤条件
					var filterWhere = " "
					if (filterList.size == 1) {
						filterWhere = filterList[0]
					} else if (filterList.isNotEmpty()) {
						filterList.forEach { filter -> filterWhere += "$filter or " }
						filterWhere = filterWhere.substring(0, filterWhere.length - 3)
					}
					//获取过滤后的数据最大数量
					recordsFiltered = if (filterWhere.isNotBlank()) {
						var count = dataProvider.getTableDataCount(dName, tName, if (where.isBlank()) filterWhere else "$where and ($filterWhere)")
						if (count - limitStart >= 0) {
							count -= limitStart
						}
						rMin(count, limitLength)
					} else {
						recordsTotal
					}
					var sql = "select ${originalSql.substring(originalSql.indexOf("select") + 6, originalSql.indexOf("from"))} from $tName"
					//拼接where条件
					val tWhere = if (where.isNotBlank() && filterWhere.isNotBlank()) ("$where and ($filterWhere)") else if (where.isNotBlank()) where else filterWhere
					if (tWhere.isNotBlank()) {
						sql += " where $tWhere"
					}
					if (orderColumn.isNotBlank() && orderDir.isNotBlank()) {
						sql += " order by $orderColumn $orderDir"
					}
					if (length >= 0 || length == -1) {
						sql += " limit $start, $length"
					}
					val data = dataProvider.executeQuery(dName, tName, sql)
					responseData(DataResponse(if (recordsFiltered != 0) recordsFiltered else recordsTotal, recordsFiltered, data))
				}
						?: responseData(DataResponse(recordsTotal, recordsFiltered, mutableListOf(), "没有找到${dName}数据库下的${tName}表"))
			}

	/**
	 * 处理删除请求.
	 * @param session NanoHTTPD.IHTTPSession
	 * @return NanoHTTPD.Response
	 */
	private fun handleDbDel(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response =
			handleRequestSafely {
				val dName = session.parms["dName"] ?: ""
				val tName = session.parms["tName"] ?: ""
				val where = session.parms["where"] ?: ""
				if (where.isNotBlank()) {
					if (dataProvider.deleteRow(dName, tName, where)) {
						responseData(com.tlz.debugger.models.Response(data = "success"))
					} else {
						responseError(errorMsg = "没有匹配的数据")
					}
				} else {
					responseError(errorMsg = "缺少删除条件")
				}
			}

	/**
	 * 处理添加请求.
	 * @param session NanoHTTPD.IHTTPSession
	 * @return NanoHTTPD.Response
	 */
	private fun handleDbAdd(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response =
			handleRequestSafely {
				val dName = session.parms["dName"] ?: ""
				val tName = session.parms["tName"] ?: ""
				val data = session.parms["data"] ?: ""
				if (data.isNotBlank()) {
					(gson.fromJson<Array<KeyValue>>(data, Array<KeyValue>::class.java))?.let {
						if (dataProvider.addRow(dName, tName, it)) {
							responseData(com.tlz.debugger.models.Response(data = "success"))
						} else {
							responseError(errorMsg = "添加数据失败数据")
						}
					} ?: responseError(errorMsg = "数据解析失败")
				} else {
					responseError(errorMsg = "缺少添加数据内容")
				}
			}

	/**
	 * 处理更新请求
	 * @param session NanoHTTPD.IHTTPSession
	 * @return NanoHTTPD.Response
	 */
	private fun handleDbUpdate(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response =
			handleRequestSafely {
				val dName = session.parms["dName"] ?: ""
				val tName = session.parms["tName"] ?: ""
				val where = session.parms["where"] ?: ""
				val data = session.parms["data"] ?: ""
				if (where.isNotBlank() && data.isNotBlank()) {
					(gson.fromJson<Array<KeyValue>>(data, Array<KeyValue>::class.java))?.let {
						if (dataProvider.updateRow(dName, tName, it, where)) {
							responseData(com.tlz.debugger.models.Response(data = "success"))
						} else {
							responseError(errorMsg = "没有找到匹配的数据")
						}
					} ?: responseError(errorMsg = "更新内容数据解析失败")
				} else {
					responseError(errorMsg = "缺少更新条件或更新内容")
				}
			}

	/**
	 * 处理sql语句查询请求.
	 * @param session NanoHTTPD.IHTTPSession
	 * @return NanoHTTPD.Response
	 */
	private fun handleDbExecute(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response =
			handleRequestSafely {
				val dName = session.parms.getValue("dName")
				val sql = session.parms.getValue("sql")
				if (sql.isNotBlank()) {
					if (dataProvider.executeSql(dName, sql)) {
						responseData(com.tlz.debugger.models.Response(data = "success"))
					} else {
						responseError(errorMsg = "sql语句执行失败，请检查后再重试")
					}
				} else {
					responseError(errorMsg = "缺少执行sql语句参数")
				}
			}

	/**
	 * 处理数据库文件下载请求.
	 * @param session NanoHTTPD.IHTTPSession
	 * @return NanoHTTPD.Response
	 */
	private fun handleDbDownload(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response =
			handleRequestSafely {
				val dName = session.parms["dName"] ?: ""
				val file = dataProvider.getDatabaseFile(dName)
				if (file == null) {
					responseError(errorMsg = "不存在该数据库")
				} else {
					NanoHTTPD.newChunkedResponse(NanoHTTPD.Response.Status.OK, "*/*", file.inputStream()).apply {
						addHeader("Content-Disposition", "attachment; filename=$dName")
					}
				}
			}
}