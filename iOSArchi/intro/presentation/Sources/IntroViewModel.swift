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
    public let isLoading: Bool
    public let intro: IntroVO?
    public let isLoadFailed: Bool

    public init(
        isLoading: Bool = false,
        intro: IntroVO? = nil,
        isLoadFailed: Bool = false
    ) {
        self.isLoading = isLoading
        self.intro = intro
        self.isLoadFailed = isLoadFailed
    }

    public func copy(
        isLoading: Bool? = nil,
        intro: IntroVO?? = nil,
        isLoadFailed: Bool? = nil
    ) -> IntroUIState {
        IntroUIState(
            isLoading: isLoading ?? self.isLoading,
            intro: intro ?? self.intro,
            isLoadFailed: isLoadFailed ?? self.isLoadFailed
        )
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
        switch event {
        case .setLoading(let isLoading):
            return state.copy(isLoading: isLoading, isLoadFailed: false)
        case .setIntro(let vo):
            return state.copy(isLoading: false, intro: .some(vo), isLoadFailed: false)
        case .setLoadFailed:
            return state.copy(isLoading: false, isLoadFailed: true)
        }
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
