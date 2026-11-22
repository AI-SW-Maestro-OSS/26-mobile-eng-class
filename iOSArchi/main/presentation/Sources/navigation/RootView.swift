import CommonDomain
import CommonPresentation
import EtcDomain
import FavoriteDomain
import Foundation
import IntroDomain
import SearchDomain
import SwiftUI

public struct RootView: View {
    private let rootContainer: DIContainer
    private let localizationHelper: LocalizationHelper
    private let navigationHelper: NavigationHelper
    private let messageHelper: MessageHelper
    private let jankReporter: JankReporter

    @State private var backStack: [GenericNavKey]
    @State private var hasHandledInitialLaunch = false
    @State private var toastMessage: String?
    @State private var activeAlert: MessageEffect.AlertSpec?

    public init(
        rootContainer: DIContainer = .shared,
        initialDeeplink: URL? = nil
    ) {
        self.rootContainer = rootContainer
        self.localizationHelper = rootContainer.resolve(LocalizationHelper.self)
        self.navigationHelper = rootContainer.resolve(NavigationHelper.self)
        self.messageHelper = rootContainer.resolve(MessageHelper.self)
        self.jankReporter = rootContainer.resolve(JankReporter.self)

        if let initialDeeplink,
           let resolved = AppRouteRegistry.resolveDeeplink(
               url: initialDeeplink,
               isColdStart: true,
               currentStack: []
           ) {
            _backStack = State(initialValue: resolved)
            _hasHandledInitialLaunch = State(initialValue: true)
        } else {
            _backStack = State(initialValue: [GenericNavKey(routePath: IntroPage.path)])
        }
    }

    private var currentKey: GenericNavKey {
        backStack.last ?? GenericNavKey(routePath: SearchPage.path)
    }

    private var currentRoute: AppRoute? {
        AppRouteRegistry.route(for: currentKey.routePath)
    }

    public var body: some View {
        ZStack {
            VStack(spacing: 0) {
                if currentRoute?.isBottomTab == true {
                    TopTabBar(
                        tabs: AppRouteRegistry.tabRoutes,
                        selectedPath: currentKey.routePath,
                        localizationHelper: localizationHelper,
                        onSelectTab: { tabRoute in
                            selectTopTab(tabRoute)
                        }
                    )
                }

                AppNavigationHost(
                    currentKey: currentKey,
                    rootContainer: rootContainer,
                    onOpenDeeplink: { deeplinkString in
                        if let url = URL(string: deeplinkString) {
                            handleDeeplink(url: url, isColdStart: false)
                        }
                    }
                )
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            }

            if let toastMessage {
                VStack {
                    Spacer()
                    ArchiText(
                        text: toastMessage,
                        style: DesignSystemThemeImpl.typeScale.textRegularS,
                        color: DesignSystemThemeImpl.designSystemColor.white,
                        textAlign: .center
                    )
                    .padding(.horizontal, 18)
                    .padding(.vertical, 10)
                    .background(
                        Capsule()
                            .fill(DesignSystemThemeImpl.designSystemColor.dimMax)
                    )
                    .padding(.bottom, 32)
                }
                .transition(.opacity)
                .allowsHitTesting(false)
            }
        }
        .environment(\.jankReporter, jankReporter)
        .task {
            await withTaskGroup(of: Void.self) { group in
                group.addTask { @MainActor in
                    for await signal in navigationHelper.navigationStream {
                        handleNavSignal(signal)
                    }
                }
                group.addTask { @MainActor in
                    for await effect in messageHelper.effect {
                        handleMessageEffect(effect)
                    }
                }
            }
        }
        .onOpenURL { url in
            let isCold = !hasHandledInitialLaunch && backStack.count == 1 && backStack.first?.routePath == IntroPage.path
            hasHandledInitialLaunch = true
            handleDeeplink(url: url, isColdStart: isCold)
        }
        .alert(item: $activeAlert) { spec in
            Alert(
                title: Text(spec.titleText ?? localizationHelper.string(for: .appName)),
                message: Text(spec.descText),
                dismissButton: .default(Text(spec.buttonText), action: {
                    spec.onTapButton?()
                })
            )
        }
    }

    private func selectTopTab(_ tabRoute: AppRoute) {
        guard currentKey.routePath != tabRoute.path else { return }
        if tabRoute.path == SearchPage.path {
            if let searchIndex = backStack.firstIndex(where: { $0.routePath == SearchPage.path }) {
                backStack = Array(backStack.prefix(through: searchIndex))
            } else {
                backStack = [GenericNavKey(routePath: SearchPage.path)]
            }
        } else {
            let searchRoot = backStack.first(where: { $0.routePath == SearchPage.path })
                ?? GenericNavKey(routePath: SearchPage.path)
            let existingTab = backStack.first(where: { $0.routePath == tabRoute.path })
                ?? GenericNavKey(routePath: tabRoute.path)
            backStack = [searchRoot, existingTab]
        }
        ViewModelCache.shared.prune(keeping: Set(backStack.map(\.id)))
    }

    private func handleNavSignal(_ signal: NavSignal) {
        switch signal {
        case .goToDestPage(let route), .deepLink(let route):
            let newKey = AppRouteRegistry.toNavKey(route)
            if currentKey.routePath == IntroPage.path && route.path == SearchPage.path {
                hasHandledInitialLaunch = true
                backStack = [newKey]
            } else if AppRouteRegistry.route(for: route.path)?.isBottomTab == true,
                      let tabRoute = AppRouteRegistry.route(for: route.path) {
                selectTopTab(tabRoute)
            } else {
                backStack.append(newKey)
            }
            ViewModelCache.shared.prune(keeping: Set(backStack.map(\.id)))

        case .back:
            guard backStack.count > 1 else { return }
            _ = backStack.popLast()
            ViewModelCache.shared.prune(keeping: Set(backStack.map(\.id)))

        case .backTo(let route):
            if let targetIndex = backStack.lastIndex(where: { $0.routePath == route.path }) {
                backStack = Array(backStack.prefix(through: targetIndex))
                ViewModelCache.shared.prune(keeping: Set(backStack.map(\.id)))
            }

        case .backToInitialPage:
            backStack = [GenericNavKey(routePath: IntroPage.path)]
            ViewModelCache.shared.prune(keeping: Set(backStack.map(\.id)))
        }
    }

    private func handleDeeplink(url: URL, isColdStart: Bool) {
        if let newStack = AppRouteRegistry.resolveDeeplink(
            url: url,
            isColdStart: isColdStart,
            currentStack: backStack
        ) {
            backStack = newStack
            ViewModelCache.shared.prune(keeping: Set(backStack.map(\.id)))
        }
    }

    private func handleMessageEffect(_ effect: MessageEffect) {
        switch effect {
        case .showToast(let message), .showBannerError(let message):
            withAnimation {
                toastMessage = message
            }
            DispatchQueue.main.asyncAfter(deadline: .now() + 2.2) {
                if toastMessage == message {
                    withAnimation {
                        toastMessage = nil
                    }
                }
            }
        case .showAlert(let spec):
            activeAlert = spec
        }
    }
}

private struct TopTabBar: View {
    let tabs: [AppRoute]
    let selectedPath: String
    let localizationHelper: LocalizationHelper
    let onSelectTab: (AppRoute) -> Void

    var body: some View {
        HStack(spacing: 0) {
            ForEach(tabs, id: \.path) { tab in
                let isSelected = tab.path == selectedPath
                let title = tab.tabTitleKey.map { localizationHelper.string(for: $0) } ?? tab.path

                Button(action: { onSelectTab(tab) }) {
                    VStack(spacing: 8) {
                        ArchiText(
                            text: title,
                            style: isSelected
                                ? DesignSystemThemeImpl.typeScale.textStrongM
                                : DesignSystemThemeImpl.typeScale.textRegularM,
                            color: isSelected
                                ? DesignSystemThemeImpl.designSystemColor.accentBlue
                                : DesignSystemThemeImpl.designSystemColor.textSecondary
                        )
                        .padding(.top, 12)

                        Rectangle()
                            .fill(
                                isSelected
                                    ? DesignSystemThemeImpl.designSystemColor.accentBlue
                                    : DesignSystemThemeImpl.designSystemColor.transparent
                            )
                            .frame(height: 2)
                    }
                    .frame(maxWidth: .infinity)
                    .contentShape(Rectangle())
                }
                .buttonStyle(.plain)
            }
        }
        .background(DesignSystemThemeImpl.designSystemColor.bgPrimary)
        .overlay(
            Rectangle()
                .fill(DesignSystemThemeImpl.designSystemColor.strokeSecondary)
                .frame(height: 1),
            alignment: .bottom
        )
    }
}
