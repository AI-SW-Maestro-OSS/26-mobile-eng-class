import CommonDomain
import CommonPresentation
import Foundation
import IntroDomain
import IntroEntity

public enum IntroIntent: MviIntent {
    case loadIntro
    case enterMain
}

public enum IntroReducerEvent: ReducerEvent {
    case setLoading(Bool)
    case setIntro(IntroVO)
    case setLoadFailed
}

public struct IntroUIState: UiState, Equatable {
    public var isLoading: Bool
    public var intro: IntroVO?
    public var isLoadFailed: Bool

    public init(
        isLoading: Bool = false,
        intro: IntroVO? = nil,
        isLoadFailed: Bool = false
    ) {
        self.isLoading = isLoading
        self.intro = intro
        self.isLoadFailed = isLoadFailed
    }
}

private struct SearchDestinationPage: Page {
    func toRoute() -> NavRoute {
        NavRoute(path: "/search")
    }
}

@MainActor
public final class IntroViewModel: MviViewModel<IntroIntent, IntroUIState, IntroReducerEvent> {
    private let getIntroUseCase: GetIntroUseCase
    public let messageHelper: MessageHelper
    public let navigationHelper: NavigationHelper
    public let loggingHelper: LoggingHelper

    public init(
        getIntroUseCase: GetIntroUseCase,
        messageHelper: MessageHelper,
        navigationHelper: NavigationHelper,
        loggingHelper: LoggingHelper
    ) {
        self.getIntroUseCase = getIntroUseCase
        self.messageHelper = messageHelper
        self.navigationHelper = navigationHelper
        self.loggingHelper = loggingHelper
        super.init(initialState: IntroUIState(isLoading: true))
    }

    public override func onIntent(_ intent: IntroIntent) {
        switch intent {
        case .loadIntro:
            fetchIntro()
        case .enterMain:
            navigationHelper.navigateTo(SearchDestinationPage())
        }
    }

    public override func reduce(state: IntroUIState, event: IntroReducerEvent) -> IntroUIState {
        var next = state
        switch event {
        case .setLoading(let isLoading):
            next.isLoading = isLoading
            next.isLoadFailed = false
        case .setIntro(let vo):
            next.isLoading = false
            next.intro = vo
            next.isLoadFailed = false
        case .setLoadFailed:
            next.isLoading = false
            next.isLoadFailed = true
        }
        return next
    }

    private func fetchIntro() {
        dispatch(.setLoading(true))
        Task { [weak self] in
            guard let self else { return }
            if let intro = await self.getIntroUseCase() {
                self.dispatch(.setIntro(intro))
                try? await Task.sleep(nanoseconds: 500_000_000)
                self.navigationHelper.navigateTo(SearchDestinationPage())
            } else {
                self.dispatch(.setLoadFailed)
            }
        }
    }
}
