import CommonData
import Foundation

public enum SearchPageMockData {
    public static let rawJson = """
    {
      "components": [
        {
          "viewType": "TitleDescriptionEndImageViewType",
          "content": {
            "titleText": "Clean Architecture + MVI + SDUI",
            "descriptionText": "Server-Driven UI banner rendered from SearchPageDataSource",
            "endImage": {
              "imgUrl": "https://picsum.photos/seed/sdui-banner/160/160",
              "size": { "width": 52, "height": 52 },
              "bgColor": "bgSecondary"
            }
          }
        },
        {
          "viewType": "StartImageTitleDescriptionActionIconType",
          "content": {
            "titleText": "Try searching 'Architecture'",
            "descriptionText": "Tap to run quick deep-link search",
            "image": {
              "imgUrl": "https://picsum.photos/seed/sdui-icon/120/120",
              "size": { "width": 40, "height": 40 },
              "bgColor": "bgSecondary"
            },
            "actionIcon": {
              "imgUrl": "",
              "size": { "width": 24, "height": 24 },
              "action": {
                "navigateLink": "app://example.architecture/search?query=Architecture",
                "actionType": "DEEPLINK"
              }
            }
          }
        }
      ]
    }
    """

    public static func makeResponse(json: ArchiJson = ArchiJson()) -> SearchPageResponseDTO {
        (try? json.decodeFromString(SearchPageResponseDTO.self, rawJson))
            ?? SearchPageResponseDTO(components: [])
    }
}
