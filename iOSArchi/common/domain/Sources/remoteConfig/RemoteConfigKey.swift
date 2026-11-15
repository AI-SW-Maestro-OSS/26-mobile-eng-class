import CommonEntity
import Foundation

public protocol AnyRemoteConfigKey: Sendable {
    var key: String { get }
    var fetchWhenUpdated: Bool { get }
}

/// Remote Config 키 정의. 키 하나가 (이름, 타입, 기본값) 을 함께 가지므로 호출부에서 캐스팅/기본값 처리가 사라진다.
public final class RemoteConfigKey<T: Codable & Sendable>: AnyRemoteConfigKey, @unchecked Sendable {
    public let key: String
    public let defaultValue: T
    public let fetchWhenUpdated: Bool

    public init(
        key: String,
        defaultValue: T,
        fetchWhenUpdated: Bool = true
    ) {
        self.key = key
        self.defaultValue = defaultValue
        self.fetchWhenUpdated = fetchWhenUpdated
    }
}

public enum RemoteConfigKeys {
    // ---- Boolean ----
    public static let IsShowFeatureA = RemoteConfigKey<Bool>(
        key: "isShowFeatureA",
        defaultValue: false
    )

    // ---- String ----
    public static let MarketingPromotionUrl = RemoteConfigKey<String>(
        key: "marketingPromotionUrl",
        defaultValue: "https://www.aiswmaestro.org/asmMobile/marketingUrl"
    )

    // ---- Int ----
    public static let NetworkRetryCount = RemoteConfigKey<Int>(
        key: "networkRetryCount",
        defaultValue: 5
    )

    // ---- List ----
    public static let AvailableTabs = RemoteConfigKey<[String]>(
        key: "availableTabs",
        defaultValue: ["firstTab", "secondTab", "thirdTab"]
    )

    public static let WebSocketReconnectExponentiallyDelayList = RemoteConfigKey<[Int64]>(
        key: "webSocketReconnectExponentiallyDelayList",
        defaultValue: [1000, 2000, 4000, 8000]
    )

    // ---- JSON object (VO) ----
    public static let BottomSheetTooltip = RemoteConfigKey<BottomSheetVO>(
        key: "earnPoolToolTipBottomSheet",
        defaultValue: BottomSheetVO(
            title: "Earn Pool",
            description: "A daily reward pool of 2,000 USDT, exclusively for VIPs who have traded over 10M in the last 30 days, distributed based on balance up to 50,000 USDT."
        )
    )

    // ---- JSON List (VO) ----
    public static let BuiltInTestAccounts = RemoteConfigKey<[TestAccountVO]>(
        key: "builtInTestAccounts",
        defaultValue: []
    )

    public static var entries: [any AnyRemoteConfigKey] {
        [
            IsShowFeatureA,
            AvailableTabs,
            MarketingPromotionUrl,
            BottomSheetTooltip,
        ]
    }
}

public extension RemoteConfigKey where T == Bool {
    static var IsShowFeatureA: RemoteConfigKey<Bool> { RemoteConfigKeys.IsShowFeatureA }
}

public extension RemoteConfigKey where T == String {
    static var MarketingPromotionUrl: RemoteConfigKey<String> { RemoteConfigKeys.MarketingPromotionUrl }
}

public extension RemoteConfigKey where T == Int {
    static var NetworkRetryCount: RemoteConfigKey<Int> { RemoteConfigKeys.NetworkRetryCount }
}

public extension RemoteConfigKey where T == [String] {
    static var AvailableTabs: RemoteConfigKey<[String]> { RemoteConfigKeys.AvailableTabs }
}

public extension RemoteConfigKey where T == [Int64] {
    static var WebSocketReconnectExponentiallyDelayList: RemoteConfigKey<[Int64]> {
        RemoteConfigKeys.WebSocketReconnectExponentiallyDelayList
    }
}

public extension RemoteConfigKey where T == BottomSheetVO {
    static var BottomSheetTooltip: RemoteConfigKey<BottomSheetVO> { RemoteConfigKeys.BottomSheetTooltip }
}

public extension RemoteConfigKey where T == [TestAccountVO] {
    static var BuiltInTestAccounts: RemoteConfigKey<[TestAccountVO]> { RemoteConfigKeys.BuiltInTestAccounts }
}
