package com.jongchan.androidarchi.common.domain.helper

import com.jongchan.androidarchi.common.entity.ai.AIServiceOutput
import com.jongchan.androidarchi.common.entity.ai.AIServiceRequest

/**
 * 온디바이스 AI(MediaPipe, AICore Gemini Nano) 및 클라우드 폴백(Firebase AI Logic) 서비스를
 * 단일 브릿지로 연결하는 범용 AI 헬퍼 인터페이스.
 *
 * 특정 비즈니스 도메인이나 화면의 목적(검색, 즐겨찾기, 취향 평가 등)을 함수나 필드명으로 드러내지 않으며,
 * 전달받은 [AIServiceRequest] 인자의 형태와 실행 정책에 맞춰 적합한 AI 서비스를 라우팅·실행한다.
 */
interface AIBasedFeatureHelper {
    suspend fun <R : AIServiceOutput> execute(request: AIServiceRequest<R>): Result<R>
}
