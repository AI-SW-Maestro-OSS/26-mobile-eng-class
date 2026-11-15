import CommonDomain
import Foundation
import TTI

public final class SearchPageUseCase: BaseUseCase, @unchecked Sendable {
    private let repository: SearchPageRepository

    public init(
        repository: SearchPageRepository,
        resourceHelper: ResourceHelper,
        messageHelper: MessageHelper,
        navigationHelper: NavigationHelper,
        ttiHelper: TTIHelper
    ) {
        self.repository = repository
        super.init(
            resourceHelper: resourceHelper,
            messageHelper: messageHelper,
            navigationHelper: navigationHelper,
            ttiHelper: ttiHelper
        )
    }

    public func invoke() async -> SearchPageVO? {
        do {
            return try await repository.fetchSearchPage()
        } catch let e as HttpResponseException {
            if e.isCommonErrorHandling() {
                executeCommonErrorHanding(e)
            }
            return nil
        } catch {
            return nil
        }
    }

    public func callAsFunction() async -> SearchPageVO? {
        await invoke()
    }
}
