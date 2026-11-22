import CommonDomain
import CommonEntity
import CommonPresentation
import EtcDomain
import Foundation

@MainActor
public final class EtcViewModel: MviViewModel<EtcIntent, EtcUIState, EtcReducerEvent> {
    private let remoteConfigRepository: RemoteConfigRepository
    public let messageHelper: MessageHelper
    public let navigationHelper: NavigationHelper
    public let localizationHelper: LocalizationHelper
    public let loggingHelper: LoggingHelper

    public init(
        remoteConfigRepository: RemoteConfigRepository,
        messageHelper: MessageHelper,
        navigationHelper: NavigationHelper,
        localizationHelper: LocalizationHelper,
        loggingHelper: LoggingHelper
    ) {
        self.remoteConfigRepository = remoteConfigRepository
        self.messageHelper = messageHelper
        self.navigationHelper = navigationHelper
        self.localizationHelper = localizationHelper
        self.loggingHelper = loggingHelper

        super.init(initialState: EtcUIState())
        onIntent(.loadDiagnostics)
    }

    public override func onIntent(_ intent: EtcIntent) {
        switch intent {
        case .loadDiagnostics:
            let isShowFeatureA = remoteConfigRepository.get(.IsShowFeatureA)
            let promoUrl = remoteConfigRepository.get(.MarketingPromotionUrl)
            let retryCount = remoteConfigRepository.get(.NetworkRetryCount)
            let bottomSheet = remoteConfigRepository.get(.BottomSheetTooltip)
            let accounts = remoteConfigRepository.get(.BuiltInTestAccounts)
            let sduiItems: [SDUIViewTypeVO] = [
                SDUIViewTypeVO(
                    viewType: .titleDescriptionEndImage,
                    content: TitleDescEndImageViewTypeVO(
                        titleText: "Clean Architecture + MVI + SDUI",
                        descriptionText: "Server-Driven UI banner rendered in EtcPage",
                        endImage: SDUIImageVO(
                            imgUrl: "sf:square.stack.3d.up.fill",
                            size: SDUISizeVO(width: 44, height: 44),
                            bgColor: "bgSecondary"
                        )
                    )
                ),
                SDUIViewTypeVO(
                    viewType: .startImageTitleDescriptionActionIcon,
                    content: StartImageTitleDescActionIconViewTypeVO(
                        titleText: "Try searching 'Architecture'",
                        descriptionText: "Tap to run quick deep-link search",
                        image: SDUIImageVO(
                            imgUrl: "sf:magnifyingglass",
                            size: SDUISizeVO(width: 40, height: 40),
                            bgColor: "bgSecondary"
                        ),
                        actionIcon: SDUIActionIconVO(
                            imgUrl: "",
                            size: SDUISizeVO(width: 24, height: 24),
                            action: SDUIActionVO(
                                navigateLink: "app://example.architecture/search?query=Architecture",
                                actionType: "DEEPLINK"
                            )
                        )
                    )
                )
            ]
            dispatch(
                .diagnosticsLoaded(
                    isLowEndDevice: !isShowFeatureA,
                    forceUpdateVersion: "\(retryCount)",
                    maintenanceMessage: promoUrl,
                    bottomSheet: bottomSheet,
                    testAccount: accounts.first ?? TestAccountVO.empty,
                    sduiViewItems: sduiItems
                )
            )
        case .triggerSampleToast:
            messageHelper.showToast("Sample toast from EtcViewModel")
        case .triggerSampleAlert:
            messageHelper.showAlert(
                titleText: "Remote Config",
                descText: "Promotion URL: \(currentState.maintenanceMessage)",
                cantIgnore: false,
                buttonText: "Ok",
                onTapButton: nil
            )
        }
    }

    public override func reduce(state: EtcUIState, event: EtcReducerEvent) -> EtcUIState {
        switch event {
        case let .diagnosticsLoaded(
            isLowEndDevice,
            forceUpdateVersion,
            maintenanceMessage,
            bottomSheet,
            testAccount,
            sduiViewItems
        ):
            return EtcUIState(
                isLowEndDevice: isLowEndDevice,
                forceUpdateVersion: forceUpdateVersion,
                maintenanceMessage: maintenanceMessage,
                bottomSheet: bottomSheet,
                testAccount: testAccount,
                sduiViewItems: sduiViewItems
            )
        }
    }
}
