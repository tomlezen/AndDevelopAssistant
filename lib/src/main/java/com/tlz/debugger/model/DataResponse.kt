package com.tlz.debugger.model

/**
 * Created by tomlezen.
 * Data: 2018/2/1.
 * Time: 13:20.
 */
class DataResponse(val draw: Int, val recordsTotal: Int, val recordsFiltered: Int, val data: List<Any>, val error: String = "")