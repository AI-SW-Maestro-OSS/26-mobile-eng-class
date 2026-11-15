import Foundation

/// TTI 측정 단위가 되는 화면. 어떤 페이지가 존재하고 각 페이지가 어떤 `TimelineCategory`
/// 들을 측정하는지는 `TTI` 모듈이 알 필요가 없으므로, 본 프로토콜을 의존하는 각 기능
/// domain 모듈에서 정의한다.
public protocol TTIPage: Sendable {
    var pageName: String { get }
    var timelines: [TimelineCategory] { get }
}
