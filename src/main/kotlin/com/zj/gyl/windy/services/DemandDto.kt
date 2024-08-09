package com.zj.gyl.windy.services

class DemandDto(var demandName: String, var demandId: String, var createTime: Long, var level: Int) {

    override fun toString(): String {
        return demandName
    }
}