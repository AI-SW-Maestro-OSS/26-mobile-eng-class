import CommonDomain
import CommonPresentation
import EtcDomain
import EtcPresentation
import FavoriteDomain
import FavoritePresentation
import Foundation
import FullScreenMediaDomain
import FullScreenMediaPresentation
import IntroDomain
import IntroPresentation
import MainDomain
import SearchDomain
import SearchPresentation
import SwiftUI
import TTI

public enum AppRouteRegistry {
    public static let routes: [AppRoute] = [
        AppRoute(
            path: IntroPage.PATH,
            isBottomTab: false,
            syntheticStack: { _ in [] },
            contentBuilder: { key, scopeContainer, _ in
                let vm = ViewModelStoreCache.shared.resolve(for: key.id) {
                    IntroViewModel(
                        getIntroUseCase: scopeContainer.resolve(GetIntroUseCase.self),
                        messageHelper: scopeContainer.resolve(MessageHelper.self),
                        navigationHelper: scopeContainer.resolve(NavigationHelper.self),
                        loggingHelper: scopeContainer.resolve(LoggingHelper.self)
                    )
                }
                return AnyView(IntroPageView(viewModel: vm))
            }
        ),
        AppRoute(
            path: SearchPage.PATH,
            isBottomTab: true,
            tabTitleKey: .NAV_TAB_SEARCH,
            syntheticStack: { _ in [] },
            contentBuilder: { key, scopeContainer, onDeeplink in
                let args = key.decodeArgs(SearchPage.Args.self)
                let vm = ViewModelStoreCache.shared.resolve(for: key.id) {
                    SearchViewModel(
                        initialQuery: args?.query,
                        searchUseCase: scopeContainer.resolve(SearchUseCase.self),
                        searchPageUseCase: scopeContainer.resolve(SearchPageUseCase.self),
                        getFavoriteItemsUseCase: scopeContainer.resolve(GetFavoriteItemsUseCase.self),
                        registerFavoriteItemUseCase: scopeContainer.resolve(RegisterFavoriteItemUseCase.self),
                        removeFavoriteItemUseCase: scopeContainer.resolve(RemoveFavoriteItemUseCase.self),
                        remoteConfigRepository: scopeContainer.resolve(RemoteConfigRepository.self),
                        globalStore: scopeContainer.resolve(GlobalStore.self),
                        messageHelper: scopeContainer.resolve(MessageHelper.self),
                        navigationHelper: scopeContainer.resolve(NavigationHelper.self),
                        resourceHelper: scopeContainer.resolve(ResourceHelper.self),
                        loggingHelper: scopeContainer.resolve(LoggingHelper.self)
                    )
                }
                return AnyView(SearchPageView(viewModel: vm, onOpenDeeplink: onDeeplink))
            }
        ),
        AppRoute(
            path: FavoritePage.PATH,
            isBottomTab: true,
            tabTitleKey: .NAV_TAB_FAVORITE,
            syntheticStack: { _ in
                [GenericNavKey(routePath: SearchPage.PATH)]
            },
            contentBuilder: { key, scopeContainer, _ in
                let vm = ViewModelStoreCache.shared.resolve(for: key.id) {
                    FavoriteViewModel(
                        getFavoriteItemsUseCase: scopeContainer.resolve(GetFavoriteItemsUseCase.self),
                        removeFavoriteItemUseCase: scopeContainer.resolve(RemoveFavoriteItemUseCase.self),
                        remoteConfigRepository: scopeContainer.resolve(RemoteConfigRepository.self),
                        globalStore: scopeContainer.resolve(GlobalStore.self),
                        messageHelper: scopeContainer.resolve(MessageHelper.self),
                        navigationHelper: scopeContainer.resolve(NavigationHelper.self),
                        resourceHelper: scopeContainer.resolve(ResourceHelper.self),
                        loggingHelper: scopeContainer.resolve(LoggingHelper.self)
                    )
                }
                return AnyView(FavoritePageView(viewModel: vm))
            }
        ),
        AppRoute(
            path: EtcPage.PATH,
            isBottomTab: true,
            tabTitleKey: .NAV_TAB_ETC,
            syntheticStack: { _ in
                [GenericNavKey(routePath: SearchPage.PATH)]
            },
            contentBuilder: { key, scopeContainer, onDeeplink in
                let vm = ViewModelStoreCache.shared.resolve(for: key.id) {
                    EtcViewModel(
                        remoteConfigRepository: scopeContainer.resolve(RemoteConfigRepository.self),
                        messageHelper: scopeContainer.resolve(MessageHelper.self),
                        navigationHelper: scopeContainer.resolve(NavigationHelper.self),
                        resourceHelper: scopeContainer.resolve(ResourceHelper.self),
                        loggingHelper: scopeContainer.resolve(LoggingHelper.self)
                    )
                }
                return AnyView(EtcPageView(viewModel: vm, onOpenDeeplink: onDeeplink))
            }
        ),
        AppRoute(
            path: FullScreenMediaPage.PATH,
            isBottomTab: false,
            syntheticStack: { key in
                let args = key.decodeArgs(FullScreenMediaPage.Args.self) ?? FullScreenMediaPage.Args()
                switch args.origin {
                case .FAVORITE:
                    return [
                        GenericNavKey(routePath: SearchPage.PATH),
                        GenericNavKey(routePath: FavoritePage.PATH)
                    ]
                case .SEARCH:
                    let searchArgsJson = NavRouteUriParser.buildArgsJson(
                        pathParams: [:],
                        queryParams: ["query": args.query]
                    )
                    return [
                        GenericNavKey(routePath: SearchPage.PATH, argsJson: searchArgsJson)
                    ]
                }
            },
            contentBuilder: { key, scopeContainer, _ in
                let args = key.decodeArgs(FullScreenMediaPage.Args.self) ?? FullScreenMediaPage.Args()
                let vm = ViewModelStoreCache.shared.resolve(for: key.id) {
                    FullScreenMediaViewModel.Factory(resolver: scopeContainer).create(args: args)
                }
                return AnyView(
                    FragmentHostContainer(viewModel: vm)
                        .ignoresSafeArea()
                )
            }
        )
    ]

    public static var tabRoutes: [AppRoute] {
        routes.filter(\.isBottomTab)
    }

    public static func route(for path: String) -> AppRoute? {
        RouteMatcher.match(actualPath: path, candidates: routes, pathSelector: \.path)?.route
    }

    public static func toNavKey(_ route: NavRoute) -> GenericNavKey {
        let json = NavRouteUriParser.buildArgsJson(pathParams: [:], queryParams: route.args)
        return GenericNavKey(routePath: route.path, argsJson: json)
    }

    public static func toNavKey(_ page: any Page) -> GenericNavKey {
        toNavKey(page.toRoute())
    }

    public static func resolveDeeplink(
        url: URL,
        isColdStart: Bool,
        currentStack: [GenericNavKey]
    ) -> [GenericNavKey]? {
        guard let parsed = NavRouteUriParser.parse(url),
              let match = RouteMatcher.match(
                  actualPath: parsed.path,
                  candidates: routes,
                  pathSelector: \.path
              ) else {
            return nil
        }

        let argsJson = NavRouteUriParser.buildArgsJson(
            pathParams: match.pathParams,
            queryParams: parsed.queryParams
        )
        let targetKey = GenericNavKey(routePath: match.route.path, argsJson: argsJson)

        if isColdStart
            || match.route.isBottomTab
            || currentStack.isEmpty
            || (currentStack.count == 1 && currentStack.first?.routePath == IntroPage.PATH) {
            var newStack = match.route.syntheticStack(targetKey)
            newStack.append(targetKey)
            return newStack
        } else {
            var updated = currentStack
            updated.append(targetKey)
            return updated
        }
    }
}

@MainActor
public final class ViewModelStoreCache {
    public static let shared = ViewModelStoreCache()

    private var stores: [UUID: AnyObject] = [:]
    private var scopes: [UUID: DIContainer] = [:]

    private init() {}

    public func scope(for id: UUID, parent: DIContainer) -> DIContainer {
        if let existing = scopes[id] {
            return existing
        }
        let created = parent.makeViewModelScope()
        scopes[id] = created
        return created
    }

    public func resolve<T: AnyObject>(for id: UUID, factory: () -> T) -> T {
        if let existing = stores[id] as? T {
            return existing
        }
        let instance = factory()
        stores[id] = instance
        return instance
    }

    public func prune(keeping activeIds: Set<UUID>) {
        stores = stores.filter { activeIds.contains($0.key) }
        scopes = scopes.filter { activeIds.contains($0.key) }
    }
}
