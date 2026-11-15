import Foundation

/// Android 의 `BuildConfig` (local.properties 주입)에 대응하는 설정 객체.
/// `Info.plist` 또는 환경변수(`ProcessInfo`)에서 값을 읽으며, 없을 경우 레퍼런스 기본값을 사용한다.
public enum BuildConfig {
    #if DEBUG
    public static let DEBUG: Bool = true
    #else
    public static let DEBUG: Bool = false
    #endif

    // API-CONFIG-INJECTION-POINT: API 키/베이스 URL
    public static var API_KEY: String {
        readValue("API_KEY") ?? "177352d0cea0c5e2b07da2d5a930be97"
    }

    public static var API_BASE_URL: String {
        readValue("API_BASE_URL") ?? "https://dapi.kakao.com/"
    }

    // METRIC-LOG-INJECTION-POINT: Metric Logging(Google Apps Script Web App) 엔드포인트/토큰.
    public static var METRIC_LOG_URL: String {
        readValue("METRIC_LOG_URL") ?? ""
    }

    public static var METRIC_LOG_TOKEN: String {
        readValue("METRIC_LOG_TOKEN") ?? ""
    }

    // TTI-LOG-INJECTION-POINT: TTI Logging(Google Apps Script Web App) 엔드포인트/토큰.
    public static var TTI_LOG_URL: String {
        readValue("TTI_LOG_URL") ?? ""
    }

    public static var TTI_LOG_TOKEN: String {
        readValue("TTI_LOG_TOKEN") ?? ""
    }

    private static func readValue(_ key: String) -> String? {
        if let envVal = ProcessInfo.processInfo.environment[key], !envVal.isEmpty {
            return envVal
        }
        if let plistVal = Bundle.main.object(forInfoDictionaryKey: key) as? String, !plistVal.isEmpty {
            return plistVal
        }
        return nil
    }
}
