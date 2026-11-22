import Foundation

/// SDUI content 의 공통 상위 타입.
public protocol SDUIContentVO: Codable, Equatable, Hashable, Sendable {}

/// 클라이언트가 파싱 가능한 SDUI viewType 화이트리스트이자 content 매핑의 단일 소스.
public enum SDUIViewType: String, CaseIterable, Sendable {
    case titleDescription = "TitleDescriptionViewType"
    case titleDescriptionEndImage = "TitleDescriptionEndImageViewType"
    case startImageTitleDescriptionActionIcon = "StartImageTitleDescriptionActionIconType"

    public var name: String { rawValue }

    public static func from(_ viewType: String?) -> SDUIViewType? {
        guard let viewType else { return nil }
        return SDUIViewType(rawValue: viewType)
    }

    public func decodeContent(from data: Data, using decoder: JSONDecoder = JSONDecoder()) throws -> any SDUIContentVO {
        switch self {
        case .titleDescription:
            return try decoder.decode(TitleDescViewTypeVO.self, from: data)
        case .titleDescriptionEndImage:
            return try decoder.decode(TitleDescEndImageViewTypeVO.self, from: data)
        case .startImageTitleDescriptionActionIcon:
            return try decoder.decode(StartImageTitleDescActionIconViewTypeVO.self, from: data)
        }
    }
}

/// 파싱 완료된 SDUI 컴포넌트 1건. data 레이어의 toVO 매퍼에서만 생성된다.
public struct SDUIViewTypeVO: Equatable, Hashable, Sendable {
    public let viewType: SDUIViewType
    public let content: any SDUIContentVO

    public init(viewType: SDUIViewType, content: any SDUIContentVO) {
        self.viewType = viewType
        self.content = content
    }

    public static func == (lhs: SDUIViewTypeVO, rhs: SDUIViewTypeVO) -> Bool {
        guard lhs.viewType == rhs.viewType else { return false }
        return AnyHashable(lhs.content) == AnyHashable(rhs.content)
    }

    public func hash(into hasher: inout Hasher) {
        hasher.combine(viewType)
        hasher.combine(AnyHashable(content))
    }
}

/// viewType == "TitleDescriptionViewType"
public struct TitleDescViewTypeVO: SDUIContentVO {
    public let titleText: String
    public let descriptionText: String

    public init(titleText: String, descriptionText: String) {
        self.titleText = titleText
        self.descriptionText = descriptionText
    }
}

/// viewType == "TitleDescriptionEndImageViewType"
public struct TitleDescEndImageViewTypeVO: SDUIContentVO {
    public let titleText: String
    public let descriptionText: String
    public let endImage: SDUIImageVO

    public init(titleText: String, descriptionText: String, endImage: SDUIImageVO) {
        self.titleText = titleText
        self.descriptionText = descriptionText
        self.endImage = endImage
    }
}

/// viewType == "StartImageTitleDescriptionActionIconType"
public struct StartImageTitleDescActionIconViewTypeVO: SDUIContentVO {
    public let titleText: String
    public let descriptionText: String
    public let image: SDUIImageVO
    public let actionIcon: SDUIActionIconVO

    public init(
        titleText: String,
        descriptionText: String,
        image: SDUIImageVO,
        actionIcon: SDUIActionIconVO
    ) {
        self.titleText = titleText
        self.descriptionText = descriptionText
        self.image = image
        self.actionIcon = actionIcon
    }
}
