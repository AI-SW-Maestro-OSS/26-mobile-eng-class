import Foundation

/// 앱 빌드 및 환경 설정 객체.
/// `Info.plist` 또는 환경변수(`ProcessInfo`)에서 값을 읽으며, 없을 경우 레퍼런스 기본값을 사용한다.
public enum AppConfig {
    #if DEBUG
    public static let isDebug: Bool = true
    #else
    public static let isDebug: Bool = false
    #endif

    // API-CONFIG-INJECTION-POINT: API 키/베이스 URL (환경변수 또는 Info.plist 로 주입)
    public static var apiKey: String {
        readValue("API_KEY") ?? ""
    }

    public static var apiBaseURL: String {
        readValue("API_BASE_URL") ?? "https://dapi.kakao.com/"
    }

    // METRIC-LOG-INJECTION-POINT: Metric Logging(Google Apps Script Web App) 엔드포인트/토큰.
    public static var metricLogURL: String {
        readValue("METRIC_LOG_URL") ?? ""
    }

    public static var metricLogToken: String {
        readValue("METRIC_LOG_TOKEN") ?? ""
    }

    // TTI-LOG-INJECTION-POINT: TTI Logging(Google Apps Script Web App) 엔드포인트/토큰.
    public static var ttiLogURL: String {
        readValue("TTI_LOG_URL") ?? ""
    }

    public static var ttiLogToken: String {
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
