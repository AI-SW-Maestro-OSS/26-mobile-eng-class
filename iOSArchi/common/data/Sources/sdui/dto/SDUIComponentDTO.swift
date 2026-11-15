import CommonDomain
import Foundation

/// SDUI 컴포넌트 1건의 원본(wire) 표현.
public struct SDUIComponentDTO: Codable, Equatable, Sendable {
    public let viewType: String?
    public let content: JsonElement?

    public init(viewType: String? = nil, content: JsonElement? = nil) {
        self.viewType = viewType
        self.content = content
    }

    /// 미지원 viewType(클라이언트에 매핑 클래스 없음) 또는 content 형식 불일치 → nil.
    public func toVOorNull(_ json: ArchiJson) -> SDUIViewTypeVO? {
        guard let type = SDUIViewType.from(viewType),
              let content else {
            return nil
        }
        guard let data = try? json.encodeToData(content),
              let contentVO = try? type.decodeContent(from: data, using: json.decoder) else {
            return nil
        }
        return SDUIViewTypeVO(viewType: type, content: contentVO)
    }
}

public extension Optional where Wrapped == [SDUIComponentDTO] {
    /// 미지원/깨진 항목은 제외하고 파싱 가능한 컴포넌트만 순서 유지하여 반환한다.
    func toSDUIComponents(_ json: ArchiJson) -> [SDUIViewTypeVO] {
        (self ?? []).compactMap { $0.toVOorNull(json) }
    }
}
