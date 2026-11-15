import CommonDomain
import CommonEntity
import CommonPresentation
import Foundation

public struct EtcUIState: UiState, Equatable {
    public let isLowEndDevice: Bool
    public let forceUpdateVersion: String
    public let maintenanceMessage: String
    public let bottomSheet: BottomSheetVO
    public let testAccount: TestAccountVO
    public let sduiViewItems: [SDUIViewTypeVO]

    public init(
        isLowEndDevice: Bool = false,
        forceUpdateVersion: String = "1.0.0",
        maintenanceMessage: String = "",
        bottomSheet: BottomSheetVO = BottomSheetVO(),
        testAccount: TestAccountVO = TestAccountVO(),
        sduiViewItems: [SDUIViewTypeVO] = []
    ) {
        self.isLowEndDevice = isLowEndDevice
        self.forceUpdateVersion = forceUpdateVersion
        self.maintenanceMessage = maintenanceMessage
        self.bottomSheet = bottomSheet
        self.testAccount = testAccount
        self.sduiViewItems = sduiViewItems
    }
}
