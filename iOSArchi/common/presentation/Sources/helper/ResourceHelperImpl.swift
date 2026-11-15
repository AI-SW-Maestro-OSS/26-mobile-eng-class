import CommonDomain
import Foundation

public final class ResourceHelperImpl: ResourceHelper, @unchecked Sendable {
    private let languageCodeProvider: @Sendable () -> String

    public init(languageCodeProvider: @escaping @Sendable () -> String = {
        Locale.preferredLanguages.first ?? "en"
    }) {
        self.languageCodeProvider = languageCodeProvider
    }

    public func getString(_ resource: StringResource) -> String {
        let isKorean = languageCodeProvider().lowercased().hasPrefix("ko")
        return isKorean
            ? Self.koTable[resource] ?? Self.enTable[resource] ?? resource.rawValue
            : Self.enTable[resource] ?? resource.rawValue
    }

    private static let enTable: [StringResource: String] = [
        .APP_NAME: "iOSArchi",
        .MEDIA_EMPTY: "No media items found.",
        .FAVORITE_EMPTY: "No favorite items yet.",
        .MEDIA_DEFAULT_TITLE: "Untitled",
        .NAV_TAB_SEARCH: "Search",
        .NAV_TAB_FAVORITE: "Favorite",
        .NAV_TAB_ETC: "Etc",
        .NAV_BACK_BUTTON: "Back",
        .FAVORITE_REGISTER_FAILED: "Failed to add favorite item.",
        .FAVORITE_REMOVE_FAILED: "Failed to remove favorite item.",
        .SEARCH_HINT: "Search images & videos",
        .SEARCH_CLEAR: "Clear",
        .SEARCH_FAILED: "Search failed. Please try again.",
        .SEARCH_PROMPT: "Enter a keyword to search images and videos.",
        .LIST_END_MARKER: "You have reached the end of the list."
    ]

    private static let koTable: [StringResource: String] = [
        .APP_NAME: "iOSArchi",
        .MEDIA_EMPTY: "검색 결과가 없습니다.",
        .FAVORITE_EMPTY: "즐겨찾기한 항목이 없습니다.",
        .MEDIA_DEFAULT_TITLE: "제목 없음",
        .NAV_TAB_SEARCH: "검색",
        .NAV_TAB_FAVORITE: "즐겨찾기",
        .NAV_TAB_ETC: "기타",
        .NAV_BACK_BUTTON: "뒤로",
        .FAVORITE_REGISTER_FAILED: "즐겨찾기 등록에 실패했습니다.",
        .FAVORITE_REMOVE_FAILED: "즐겨찾기 삭제에 실패했습니다.",
        .SEARCH_HINT: "이미지 및 동영상 검색",
        .SEARCH_CLEAR: "지우기",
        .SEARCH_FAILED: "검색에 실패했습니다. 다시 시도해주세요.",
        .SEARCH_PROMPT: "검색어를 입력하여 이미지와 동영상을 찾아보세요.",
        .LIST_END_MARKER: "마지막 페이지입니다."
    ]
}
