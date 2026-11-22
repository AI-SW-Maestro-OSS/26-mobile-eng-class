import CommonDomain
import Foundation

public final class LocalizationHelperImpl: LocalizationHelper, @unchecked Sendable {
    private let languageCodeProvider: @Sendable () -> String

    public init(languageCodeProvider: @escaping @Sendable () -> String = {
        Locale.preferredLanguages.first ?? "en"
    }) {
        self.languageCodeProvider = languageCodeProvider
    }

    public func string(for key: LocalizedKey) -> String {
        let isKorean = languageCodeProvider().lowercased().hasPrefix("ko")
        return isKorean
            ? Self.koTable[key] ?? Self.enTable[key] ?? key.rawValue
            : Self.enTable[key] ?? key.rawValue
    }

    private static let enTable: [LocalizedKey: String] = [
        .appName: "iOSArchi",
        .mediaEmpty: "No media items found.",
        .favoriteEmpty: "No favorite items yet.",
        .mediaDefaultTitle: "Untitled",
        .navTabSearch: "Search",
        .navTabFavorite: "Favorite",
        .navTabEtc: "Etc",
        .navBackButton: "Back",
        .favoriteRegisterFailed: "Failed to add favorite item.",
        .favoriteRemoveFailed: "Failed to remove favorite item.",
        .searchHint: "Search images & videos",
        .searchClear: "Clear",
        .searchFailed: "Search failed. Please try again.",
        .searchPrompt: "Enter a keyword to search images and videos.",
        .listEndMarker: "You have reached the end of the list."
    ]

    private static let koTable: [LocalizedKey: String] = [
        .appName: "iOSArchi",
        .mediaEmpty: "검색 결과가 없습니다.",
        .favoriteEmpty: "즐겨찾기한 항목이 없습니다.",
        .mediaDefaultTitle: "제목 없음",
        .navTabSearch: "검색",
        .navTabFavorite: "즐겨찾기",
        .navTabEtc: "기타",
        .navBackButton: "뒤로",
        .favoriteRegisterFailed: "즐겨찾기 등록에 실패했습니다.",
        .favoriteRemoveFailed: "즐겨찾기 삭제에 실패했습니다.",
        .searchHint: "이미지 및 동영상 검색",
        .searchClear: "지우기",
        .searchFailed: "검색에 실패했습니다. 다시 시도해주세요.",
        .searchPrompt: "검색어를 입력하여 이미지와 동영상을 찾아보세요.",
        .listEndMarker: "마지막 페이지입니다."
    ]
}
