package com.dertefter.neticlient.data.repository

import com.dertefter.neticlient.data.network.NetworkClient
import com.dertefter.neticlient.data.network.model.ResponseResult
import com.dertefter.neticlient.data.network.model.ResponseType
import javax.inject.Inject


class MoneyRepository @Inject constructor(
    private val networkClient: NetworkClient
) {




    suspend fun fetchYears(): ResponseResult{
        val years = networkClient.getMoneyYearsList()
        if (years != null){
            return ResponseResult(ResponseType.SUCCESS, data = years)
        }
        return ResponseResult(ResponseType.ERROR, "")
    }

    suspend fun fetchMoneyItems(year: String): ResponseResult {
        val moneyItems = networkClient.getMoneyItems(year)
        if (moneyItems != null){
            return ResponseResult(ResponseType.SUCCESS, data = moneyItems)
        }
        return ResponseResult(ResponseType.ERROR, "")
    }
}