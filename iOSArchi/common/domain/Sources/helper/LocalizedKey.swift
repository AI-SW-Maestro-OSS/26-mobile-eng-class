import Foundation

public enum LocalizedKey: String, CaseIterable, Sendable {
    case appName = "APP_NAME"
    case mediaEmpty = "MEDIA_EMPTY"
    case favoriteEmpty = "FAVORITE_EMPTY"
    case mediaDefaultTitle = "MEDIA_DEFAULT_TITLE"
    case navTabSearch = "NAV_TAB_SEARCH"
    case navTabFavorite = "NAV_TAB_FAVORITE"
    case navTabEtc = "NAV_TAB_ETC"
    case navBackButton = "NAV_BACK_BUTTON"
    case favoriteRegisterFailed = "FAVORITE_REGISTER_FAILED"
    case favoriteRemoveFailed = "FAVORITE_REMOVE_FAILED"
    case searchHint = "SEARCH_HINT"
    case searchClear = "SEARCH_CLEAR"
    case searchFailed = "SEARCH_FAILED"
    case searchPrompt = "SEARCH_PROMPT"
    case listEndMarker = "LIST_END_MARKER"
}
