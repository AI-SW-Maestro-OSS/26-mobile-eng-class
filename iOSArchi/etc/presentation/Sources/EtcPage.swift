import CommonDomain
import CommonPresentation
import EtcDomain
import SwiftUI

public struct EtcPageView: View {
    @ObservedObject private var viewModel: EtcViewModel
    private let onOpenDeeplink: ((String) -> Void)?

    public init(
        viewModel: EtcViewModel,
        onOpenDeeplink: ((String) -> Void)? = nil
    ) {
        self.viewModel = viewModel
        self.onOpenDeeplink = onOpenDeeplink
    }

    public var body: some View {
        EtcScreenContent(
            uiState: viewModel.uiState,
            titleText: viewModel.resourceHelper.getString(.NAV_TAB_ETC),
            devicePerformanceTitle: "Device & Feature Flag",
            devicePerformanceValue: viewModel.uiState.isLowEndDevice
                ? "Low-End / Reduced Mode"
                : "Standard / Full Mode",
            remoteConfigTitle: "Remote Config Snapshot",
            onTriggerToast: { viewModel.onIntent(.triggerSampleToast) },
            onTriggerAlert: { viewModel.onIntent(.triggerSampleAlert) },
            onSduiDeeplinkClick: { deeplink in
                onOpenDeeplink?(deeplink)
            }
        )
        .jankPageEffect(EtcPage.PATH)
    }
}

public struct EtcScreenContent: View {
    private let uiState: EtcUIState
    private let titleText: String
    private let devicePerformanceTitle: String
    private let devicePerformanceValue: String
    private let remoteConfigTitle: String
    private let onTriggerToast: () -> Void
    private let onTriggerAlert: () -> Void
    private let onSduiDeeplinkClick: (String) -> Void

    public init(
        uiState: EtcUIState,
        titleText: String = "Etc",
        devicePerformanceTitle: String = "Device Performance Tier",
        devicePerformanceValue: String = "Standard Device (Full animations)",
        remoteConfigTitle: String = "Remote Config Snapshot",
        onTriggerToast: @escaping () -> Void,
        onTriggerAlert: @escaping () -> Void,
        onSduiDeeplinkClick: @escaping (String) -> Void = { _ in }
    ) {
        self.uiState = uiState
        self.titleText = titleText
        self.devicePerformanceTitle = devicePerformanceTitle
        self.devicePerformanceValue = devicePerformanceValue
        self.remoteConfigTitle = remoteConfigTitle
        self.onTriggerToast = onTriggerToast
        self.onTriggerAlert = onTriggerAlert
        self.onSduiDeeplinkClick = onSduiDeeplinkClick
    }

    public var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                ArchiText(
                    text: titleText,
                    style: DesignSystemThemeImpl.typeScale.titleStrongL,
                    color: DesignSystemThemeImpl.designSystemColor.textPrimary
                )

                if !uiState.sduiViewItems.isEmpty {
                    VStack(spacing: 8) {
                        ForEach(Array(uiState.sduiViewItems.enumerated()), id: \.offset) { _, component in
                            SDUIRootView(item: component, onActionClick: onSduiDeeplinkClick)
                        }
                    }
                }

                VStack(alignment: .leading, spacing: 8) {
                    ArchiText(
                        text: devicePerformanceTitle,
                        style: DesignSystemThemeImpl.typeScale.textStrongM,
                        color: DesignSystemThemeImpl.designSystemColor.textPrimary
                    )
                    ArchiText(
                        text: devicePerformanceValue,
                        style: DesignSystemThemeImpl.typeScale.textRegularS,
                        color: DesignSystemThemeImpl.designSystemColor.textSecondary
                    )
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(16)
                .background(
                    RoundedRectangle(cornerRadius: 12)
                        .fill(DesignSystemThemeImpl.designSystemColor.bgSecondary)
                )

                VStack(alignment: .leading, spacing: 8) {
                    ArchiText(
                        text: remoteConfigTitle,
                        style: DesignSystemThemeImpl.typeScale.textStrongM,
                        color: DesignSystemThemeImpl.designSystemColor.textPrimary
                    )
                    ArchiText(
                        text: "Network Retry Count: \(uiState.forceUpdateVersion)",
                        style: DesignSystemThemeImpl.typeScale.textRegularS,
                        color: DesignSystemThemeImpl.designSystemColor.textSecondary
                    )
                    ArchiText(
                        text: "Promo Title: \(uiState.bottomSheet.title)",
                        style: DesignSystemThemeImpl.typeScale.textRegularS,
                        color: DesignSystemThemeImpl.designSystemColor.textSecondary
                    )
                    ArchiText(
                        text: "Test Account ID: \(uiState.testAccount.id.isEmpty ? "none" : uiState.testAccount.id)",
                        style: DesignSystemThemeImpl.typeScale.textRegularS,
                        color: DesignSystemThemeImpl.designSystemColor.textSecondary
                    )
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(16)
                .background(
                    RoundedRectangle(cornerRadius: 12)
                        .fill(DesignSystemThemeImpl.designSystemColor.bgSecondary)
                )

                HStack(spacing: 12) {
                    Button(action: onTriggerToast) {
                        ArchiText(
                            text: "Show Toast",
                            style: DesignSystemThemeImpl.typeScale.textStrongM,
                            color: DesignSystemThemeImpl.designSystemColor.white
                        )
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 12)
                        .background(
                            RoundedRectangle(cornerRadius: 10)
                                .fill(DesignSystemThemeImpl.designSystemColor.accentBlue)
                        )
                    }
                    .buttonStyle(.plain)

                    Button(action: onTriggerAlert) {
                        ArchiText(
                            text: "Show Alert",
                            style: DesignSystemThemeImpl.typeScale.textStrongM,
                            color: DesignSystemThemeImpl.designSystemColor.textPrimary
                        )
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 12)
                        .background(
                            RoundedRectangle(cornerRadius: 10)
                                .fill(DesignSystemThemeImpl.designSystemColor.bgTertiary)
                        )
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(16)
        }
        .background(DesignSystemThemeImpl.designSystemColor.bgPrimary)
    }
}
