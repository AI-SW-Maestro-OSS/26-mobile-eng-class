import CommonDomain
import CommonPresentation
import IntroDomain
import IntroEntity
import SwiftUI

public struct IntroPageView: View {
    @ObservedObject private var viewModel: IntroViewModel
    private let onNavigateToSearch: (() -> Void)?

    public init(
        viewModel: IntroViewModel,
        onNavigateToSearch: (() -> Void)? = nil
    ) {
        self.viewModel = viewModel
        self.onNavigateToSearch = onNavigateToSearch
    }

    public var body: some View {
        IntroPageContent(
            uiState: viewModel.uiState,
            onRetry: { viewModel.onIntent(.loadIntro) },
            onEnterMain: {
                if let onNavigateToSearch {
                    onNavigateToSearch()
                } else {
                    viewModel.onIntent(.enterMain)
                }
            }
        )
        .jankPageEffect(IntroPage.path)
        .onAppear {
            if viewModel.uiState.intro == nil && !viewModel.uiState.isLoadFailed {
                viewModel.onIntent(.loadIntro)
            }
        }
    }
}

public struct IntroPageContent: View {
    private let uiState: IntroUIState
    private let onRetry: () -> Void
    private let onEnterMain: () -> Void

    public init(
        uiState: IntroUIState,
        onRetry: @escaping () -> Void,
        onEnterMain: @escaping () -> Void
    ) {
        self.uiState = uiState
        self.onRetry = onRetry
        self.onEnterMain = onEnterMain
    }

    public var body: some View {
        ZStack {
            DesignSystemThemeImpl.designSystemColor.bgPrimary
                .ignoresSafeArea()

            VStack(spacing: 16) {
                Image(systemName: "square.stack.3d.up.fill")
                    .font(DesignSystemThemeImpl.typeScale.titleStrongL.font)
                    .foregroundColor(DesignSystemThemeImpl.designSystemColor.accentBlue)
                    .padding(18)
                    .background(
                        Circle()
                            .fill(DesignSystemThemeImpl.designSystemColor.bgSecondary)
                    )

                ArchiText(
                    text: uiState.intro?.title ?? "iOSArchi",
                    style: DesignSystemThemeImpl.typeScale.titleStrongL,
                    color: DesignSystemThemeImpl.designSystemColor.textPrimary,
                    textAlign: .center
                )

                ArchiText(
                    text: uiState.intro?.subtitle ?? "Multi-Module Clean Architecture + MVI + SwiftUI",
                    style: DesignSystemThemeImpl.typeScale.textRegularM,
                    color: DesignSystemThemeImpl.designSystemColor.textSecondary,
                    textAlign: .center
                )

                if uiState.isLoading {
                    ProgressView()
                        .padding(.top, 12)
                } else if uiState.isLoadFailed {
                    Button(action: onRetry) {
                        ArchiText(
                            text: "Retry",
                            style: DesignSystemThemeImpl.typeScale.textStrongM,
                            color: DesignSystemThemeImpl.designSystemColor.white
                        )
                        .padding(.horizontal, 20)
                        .padding(.vertical, 10)
                        .background(
                            RoundedRectangle(cornerRadius: 10)
                                .fill(DesignSystemThemeImpl.designSystemColor.accentBlue)
                        )
                    }
                    .buttonStyle(.plain)
                    .padding(.top, 12)
                } else {
                    Button(action: onEnterMain) {
                        ArchiText(
                            text: "Get Started",
                            style: DesignSystemThemeImpl.typeScale.textStrongM,
                            color: DesignSystemThemeImpl.designSystemColor.white
                        )
                        .padding(.horizontal, 24)
                        .padding(.vertical, 12)
                        .background(
                            RoundedRectangle(cornerRadius: 10)
                                .fill(DesignSystemThemeImpl.designSystemColor.accentBlue)
                        )
                    }
                    .buttonStyle(.plain)
                    .padding(.top, 12)
                }
            }
            .padding(24)
        }
    }
}
