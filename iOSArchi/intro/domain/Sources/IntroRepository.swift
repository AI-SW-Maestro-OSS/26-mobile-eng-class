import Foundation
import IntroEntity

public protocol IntroRepository: Sendable {
    func fetchIntro() async throws -> IntroVO
}
