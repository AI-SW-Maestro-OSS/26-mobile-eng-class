package com.jongchan.androidarchi.common.entity.ai

/**
 * AI 작업을 실제로 수행한 하위 엔진 종류.
 */
enum class AIEngineType {
    ON_DEVICE_MEDIAPIPE,
    ON_DEVICE_AICORE,
    CLOUD_FIREBASE_AI,
    LOCAL_FALLBACK,
}

/**
 * AI 텍스트 생성/추론 시 온디바이스 및 클라우드 엔진 선택 정책.
 */
enum class AIExecutionPolicy {
    /** 온디바이스 엔진만 사용 */
    ON_DEVICE_ONLY,

    /** 온디바이스 AICore(Gemini Nano) 우선 실행 후 미지원/실패 시 Firebase AI Logic 폴백 */
    PREFER_ON_DEVICE_FALLBACK_CLOUD,

    /** 클라우드 Firebase AI Logic 직접 실행 */
    CLOUD_ONLY,
}

/**
 * [com.jongchan.androidarchi.common.domain.helper.AIBasedFeatureHelper] 에 전달되는
 * 목적별 AI 서비스 요청 명세(Bridge Request).
 *
 * 특정 화면/비즈니스 도메인 이름을 노출하지 않고, 수행할 AI 연산의 형태와 파라미터만 정의한다.
 */
sealed interface AIServiceRequest<out R : AIServiceOutput> {

    /** 단일 텍스트 임베딩 벡터 추출 요청 (MediaPipe TextEmbedder) */
    data class EmbedText(
        val text: String = "",
    ) : AIServiceRequest<AIServiceOutput.TextEmbedding>

    /** 다건 텍스트(id -> text) 일괄 임베딩 벡터 추출 요청 (MediaPipe TextEmbedder) */
    data class BatchEmbedText(
        val texts: Map<String, String> = emptyMap(),
    ) : AIServiceRequest<AIServiceOutput.BatchTextEmbedding>

    /** 두 임베딩 벡터 간의 코사인 유사도 계산 요청 */
    class CalculateVectorSimilarity(
        val firstVector: FloatArray = FloatArray(0),
        val secondVector: FloatArray = FloatArray(0),
    ) : AIServiceRequest<AIServiceOutput.VectorSimilarity>

    /** 프롬프트 기반 생성형 AI 추론 요청 (AICore Gemini Nano -> Firebase AI Logic 폴백) */
    data class GenerateText(
        val prompt: String = "",
        val systemInstruction: String = "",
        val temperature: Float = 0.4f,
        val maxOutputTokens: Int = 256,
        val policy: AIExecutionPolicy = AIExecutionPolicy.PREFER_ON_DEVICE_FALLBACK_CLOUD,
    ) : AIServiceRequest<AIServiceOutput.GeneratedText>
}

/**
 * [com.jongchan.androidarchi.common.domain.helper.AIBasedFeatureHelper] 가 반환하는
 * AI 서비스 결과 명세(Bridge Output).
 */
sealed interface AIServiceOutput {

    class TextEmbedding(
        val vector: FloatArray = FloatArray(0),
        val engineType: AIEngineType = AIEngineType.ON_DEVICE_MEDIAPIPE,
    ) : AIServiceOutput

    data class BatchTextEmbedding(
        val vectors: Map<String, FloatArray> = emptyMap(),
        val engineType: AIEngineType = AIEngineType.ON_DEVICE_MEDIAPIPE,
    ) : AIServiceOutput

    data class VectorSimilarity(
        val similarity: Double = 0.0,
    ) : AIServiceOutput

    data class GeneratedText(
        val text: String = "",
        val engineType: AIEngineType = AIEngineType.ON_DEVICE_AICORE,
    ) : AIServiceOutput {
        companion object {
            val empty: GeneratedText = GeneratedText()
        }
    }
}
