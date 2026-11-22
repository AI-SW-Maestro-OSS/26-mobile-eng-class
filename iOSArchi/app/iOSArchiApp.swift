import CommonData
import CommonDomain
import CommonPresentation
import FavoriteData
import FullScreenMediaData
import IntroData
import MainPresentation
import SearchData
import SwiftUI

public enum AppDIAssembler {
    public static func registerAllModules(in container: DIContainer = .shared) {
        ConcurrencyModule.register(in: container)
        NetworkModule.register(in: container)
        MediaSearchDataModule.register(in: container)
        MetricLoggingDataModule.register(in: container)
        TtiLoggingDataModule.register(in: container)
        RemoteConfigDataModule.register(in: container)
        CommonPresentationModule.register(in: container)
        JankModule.register(in: container)
        IntroDataModule.register(in: container)
        SearchDataModule.register(in: container)
        FavoriteDataModule.register(in: container)
        FullScreenMediaDataModule.register(in: container)
    }
}

@main
public struct iOSArchiApp: App {
    public init() {
        AppDIAssembler.registerAllModules(in: .shared)
    }

    public var body: some Scene {
        WindowGroup {
            MainView(rootContainer: .shared)
        }
    }
}
