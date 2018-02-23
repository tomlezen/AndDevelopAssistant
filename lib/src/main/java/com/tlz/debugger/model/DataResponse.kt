package com.tlz.debugger.model

/**
 * Created by tomlezen.
 * Data: 2018/2/1.
 * Time: 13:20.
 * @param draw 当前绘制次数.
 * @param recordsTotal 数据库中总得记录条数.
 * @param recordsFiltered 过滤后得数据条数.
 * @param data 查询到得数据.
 * @param error 错误信息.
 */
class DataResponse(val draw: Int, val recordsTotal: Int, val recordsFiltered: Int, val data: List<Any>, val error: String = "")