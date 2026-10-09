package com.jongchan.androidarchi.common.data.analytics.tti

import com.jongchan.androidarchi.common.data.analytics.tti.dto.TtiLogRequestDTO
import com.jongchan.androidarchi.common.data.analytics.tti.dto.TtiLogResponseDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface TtiLoggingApiService {

    /**
     * Apps Script Web App 은 `.../macros/s/{deploymentId}/exec` 전체 URL 이 엔드포인트이므로
     * baseUrl 대신 [Url] 로 절대 URL 을 넘긴다. (POST → 302 → GET 리다이렉트는 OkHttp 가 따라간다)
     */
    @POST
    suspend fun send(
        @Url url: String,
        @Body body: TtiLogRequestDTO,
    ): Response<TtiLogResponseDTO>
}
