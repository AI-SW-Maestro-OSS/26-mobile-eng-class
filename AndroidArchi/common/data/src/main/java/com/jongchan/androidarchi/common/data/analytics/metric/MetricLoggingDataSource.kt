package com.jongchan.androidarchi.common.data.analytics.metric

import com.jongchan.androidarchi.common.data.BaseRemoteDataSource
import com.jongchan.androidarchi.common.data.analytics.metric.dto.MetricLogRequestDTO
import com.jongchan.androidarchi.common.data.analytics.metric.dto.MetricLogResponseDTO

class MetricLoggingDataSource(
    private val apiService: MetricLoggingApiService,
    private val endpointUrl: String,
) : BaseRemoteDataSource() {

    suspend fun send(request: MetricLogRequestDTO): MetricLogResponseDTO {
        return checkResponse(apiService.send(url = endpointUrl, body = request))
    }
}
