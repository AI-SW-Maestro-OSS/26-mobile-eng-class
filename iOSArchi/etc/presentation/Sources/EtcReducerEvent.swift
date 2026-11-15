import CommonDomain
import CommonEntity
import CommonPresentation
import Foundation

public enum EtcReducerEvent: ReducerEvent {
    case diagnosticsLoaded(
        isLowEndDevice: Bool,
        forceUpdateVersion: String,
        maintenanceMessage: String,
        bottomSheet: BottomSheetVO,
        testAccount: TestAccountVO,
        sduiViewItems: [SDUIViewTypeVO]
    )
}
