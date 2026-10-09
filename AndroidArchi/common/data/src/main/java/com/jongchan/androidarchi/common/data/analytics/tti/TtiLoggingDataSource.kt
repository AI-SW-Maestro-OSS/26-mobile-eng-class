package com.jongchan.androidarchi.common.data.analytics.tti

import com.jongchan.androidarchi.common.data.BaseRemoteDataSource
import com.jongchan.androidarchi.common.data.analytics.tti.dto.TtiLogRequestDTO
import com.jongchan.androidarchi.common.data.analytics.tti.dto.TtiLogResponseDTO

class TtiLoggingDataSource(
    private val apiService: TtiLoggingApiService,
    private val endpointUrl: String,
) : BaseRemoteDataSource() {

    suspend fun send(request: TtiLogRequestDTO): TtiLogResponseDTO {
        return checkResponse(apiService.send(url = endpointUrl, body = request))
    }
}
