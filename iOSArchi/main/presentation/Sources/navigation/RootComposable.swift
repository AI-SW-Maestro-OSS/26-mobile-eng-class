import CommonDomain
import CommonPresentation
import EtcDomain
import FavoriteDomain
import Foundation
import IntroDomain
import SearchDomain
import SwiftUI

public struct RootComposable: View {
    private let rootContainer: DIContainer
    private let resourceHelper: ResourceHelper
    private let navigationHelper: NavigationHelper
    private let messageHelper: MessageHelper
    private let jankReporter: JankReporter

    @State private var backStack: [GenericNavKey]
    @State private var hasHandledInitialLaunch = false
    @State private var toastMessage: String?
    @State private var activeDialog: MessageEffect.OneButtonDialogSpec?

    public init(
        rootContainer: DIContainer = .shared,
        initialDeeplink: URL? = nil
    ) {
        self.rootContainer = rootContainer
        self.resourceHelper = rootContainer.resolve(ResourceHelper.self)
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
            _backStack = State(initialValue: [GenericNavKey(routePath: IntroPage.PATH)])
        }
    }

    private var currentKey: GenericNavKey {
        backStack.last ?? GenericNavKey(routePath: SearchPage.PATH)
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
                        resourceHelper: resourceHelper,
                        onSelectTab: { tabRoute in
                            selectTopTab(tabRoute)
                        }
                    )
                }

                AppNavHost(
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
        .environment(\.localJankReporter, jankReporter)
        .task {
            await withTaskGroup(of: Void.self) { group in
                group.addTask { @MainActor in
                    for await signal in navigationHelper.navigationFlow {
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
            let isCold = !hasHandledInitialLaunch && backStack.count == 1 && backStack.first?.routePath == IntroPage.PATH
            hasHandledInitialLaunch = true
            handleDeeplink(url: url, isColdStart: isCold)
        }
        .alert(item: $activeDialog) { spec in
            Alert(
                title: Text(spec.titleText ?? resourceHelper.getString(.APP_NAME)),
                message: Text(spec.descText),
                dismissButton: .default(Text(spec.buttonText), action: {
                    spec.onClickButton?()
                })
            )
        }
    }

    private func selectTopTab(_ tabRoute: AppRoute) {
        guard currentKey.routePath != tabRoute.path else { return }
        if tabRoute.path == SearchPage.PATH {
            if let searchIndex = backStack.firstIndex(where: { $0.routePath == SearchPage.PATH }) {
                backStack = Array(backStack.prefix(through: searchIndex))
            } else {
                backStack = [GenericNavKey(routePath: SearchPage.PATH)]
            }
        } else {
            let searchRoot = backStack.first(where: { $0.routePath == SearchPage.PATH })
                ?? GenericNavKey(routePath: SearchPage.PATH)
            let existingTab = backStack.first(where: { $0.routePath == tabRoute.path })
                ?? GenericNavKey(routePath: tabRoute.path)
            backStack = [searchRoot, existingTab]
        }
        ViewModelStoreCache.shared.prune(keeping: Set(backStack.map(\.id)))
    }

    private func handleNavSignal(_ signal: NavSignal) {
        switch signal {
        case .GoToDestPage(let route), .DeepLink(let route):
            let newKey = AppRouteRegistry.toNavKey(route)
            if currentKey.routePath == IntroPage.PATH && route.path == SearchPage.PATH {
                hasHandledInitialLaunch = true
                backStack = [newKey]
            } else if AppRouteRegistry.route(for: route.path)?.isBottomTab == true,
                      let tabRoute = AppRouteRegistry.route(for: route.path) {
                selectTopTab(tabRoute)
            } else {
                backStack.append(newKey)
            }
            ViewModelStoreCache.shared.prune(keeping: Set(backStack.map(\.id)))

        case .Back:
            guard backStack.count > 1 else { return }
            _ = backStack.popLast()
            ViewModelStoreCache.shared.prune(keeping: Set(backStack.map(\.id)))

        case .BackTo(let route):
            if let targetIndex = backStack.lastIndex(where: { $0.routePath == route.path }) {
                backStack = Array(backStack.prefix(through: targetIndex))
                ViewModelStoreCache.shared.prune(keeping: Set(backStack.map(\.id)))
            }

        case .BackToInitialPage:
            backStack = [GenericNavKey(routePath: IntroPage.PATH)]
            ViewModelStoreCache.shared.prune(keeping: Set(backStack.map(\.id)))
        }
    }

    private func handleDeeplink(url: URL, isColdStart: Bool) {
        if let newStack = AppRouteRegistry.resolveDeeplink(
            url: url,
            isColdStart: isColdStart,
            currentStack: backStack
        ) {
            backStack = newStack
            ViewModelStoreCache.shared.prune(keeping: Set(backStack.map(\.id)))
        }
    }

    private func handleMessageEffect(_ effect: MessageEffect) {
        switch effect {
        case .ShowToastMsg(let message), .ShowSnackBarError(let message):
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
        case .ShowOneButtonDialog(let spec):
            activeDialog = spec
        }
    }
}

private struct TopTabBar: View {
    let tabs: [AppRoute]
    let selectedPath: String
    let resourceHelper: ResourceHelper
    let onSelectTab: (AppRoute) -> Void

    var body: some View {
        HStack(spacing: 0) {
            ForEach(tabs, id: \.path) { tab in
                let isSelected = tab.path == selectedPath
                let title = tab.tabTitleKey.map { resourceHelper.getString($0) } ?? tab.path

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
