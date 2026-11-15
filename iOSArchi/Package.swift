// swift-tools-version: 5.10
import PackageDescription

let package = Package(
    name: "iOSArchi",
    defaultLocalization: "en",
    platforms: [
        .iOS(.v17),
        .macOS(.v14),
    ],
    products: [
        .library(name: "TTI", targets: ["TTI"]),
        .library(name: "CommonEntity", targets: ["CommonEntity"]),
        .library(name: "CommonDomain", targets: ["CommonDomain"]),
        .library(name: "CommonData", targets: ["CommonData"]),
        .library(name: "CommonPresentation", targets: ["CommonPresentation"]),
        .library(name: "MainEntity", targets: ["MainEntity"]),
        .library(name: "MainDomain", targets: ["MainDomain"]),
        .library(name: "MainData", targets: ["MainData"]),
        .library(name: "MainPresentation", targets: ["MainPresentation"]),
        .library(name: "IntroEntity", targets: ["IntroEntity"]),
        .library(name: "IntroDomain", targets: ["IntroDomain"]),
        .library(name: "IntroData", targets: ["IntroData"]),
        .library(name: "IntroPresentation", targets: ["IntroPresentation"]),
        .library(name: "SearchEntity", targets: ["SearchEntity"]),
        .library(name: "SearchDomain", targets: ["SearchDomain"]),
        .library(name: "SearchData", targets: ["SearchData"]),
        .library(name: "SearchPresentation", targets: ["SearchPresentation"]),
        .library(name: "FavoriteEntity", targets: ["FavoriteEntity"]),
        .library(name: "FavoriteDomain", targets: ["FavoriteDomain"]),
        .library(name: "FavoriteData", targets: ["FavoriteData"]),
        .library(name: "FavoritePresentation", targets: ["FavoritePresentation"]),
        .library(name: "FullScreenMediaEntity", targets: ["FullScreenMediaEntity"]),
        .library(name: "FullScreenMediaDomain", targets: ["FullScreenMediaDomain"]),
        .library(name: "FullScreenMediaData", targets: ["FullScreenMediaData"]),
        .library(name: "FullScreenMediaPresentation", targets: ["FullScreenMediaPresentation"]),
        .library(name: "EtcEntity", targets: ["EtcEntity"]),
        .library(name: "EtcDomain", targets: ["EtcDomain"]),
        .library(name: "EtcData", targets: ["EtcData"]),
        .library(name: "EtcPresentation", targets: ["EtcPresentation"]),
        .library(
            name: "iOSArchiAllModules",
            targets: [
                "TTI",
                "CommonEntity", "CommonDomain", "CommonData", "CommonPresentation",
                "MainEntity", "MainDomain", "MainData", "MainPresentation",
                "IntroEntity", "IntroDomain", "IntroData", "IntroPresentation",
                "SearchEntity", "SearchDomain", "SearchData", "SearchPresentation",
                "FavoriteEntity", "FavoriteDomain", "FavoriteData", "FavoritePresentation",
                "FullScreenMediaEntity", "FullScreenMediaDomain", "FullScreenMediaData", "FullScreenMediaPresentation",
                "EtcEntity", "EtcDomain", "EtcData", "EtcPresentation",
            ]
        ),
    ],
    targets: [
        // MARK: - TTI (Pure Swift)
        .target(
            name: "TTI",
            dependencies: [],
            path: "tti/Sources"
        ),

        // MARK: - Common (4-Layer)
        .target(
            name: "CommonEntity",
            dependencies: [],
            path: "common/entity/Sources"
        ),
        .target(
            name: "CommonDomain",
            dependencies: [
                "TTI",
                "CommonEntity",
            ],
            path: "common/domain/Sources"
        ),
        .target(
            name: "CommonData",
            dependencies: [
                "CommonDomain",
                "CommonEntity",
            ],
            path: "common/data/Sources"
        ),
        .target(
            name: "CommonPresentation",
            dependencies: [
                "CommonDomain",
                "CommonEntity",
                "TTI",
            ],
            path: "common/presentation/Sources"
        ),

        // MARK: - Intro (4-Layer)
        .target(
            name: "IntroEntity",
            dependencies: [
                "CommonEntity",
            ],
            path: "intro/entity/Sources"
        ),
        .target(
            name: "IntroDomain",
            dependencies: [
                "CommonDomain",
                "CommonEntity",
                "IntroEntity",
                "SearchDomain",
                "TTI",
            ],
            path: "intro/domain/Sources"
        ),
        .target(
            name: "IntroData",
            dependencies: [
                "CommonData",
                "CommonDomain",
                "CommonEntity",
                "IntroDomain",
                "IntroEntity",
            ],
            path: "intro/data/Sources"
        ),
        .target(
            name: "IntroPresentation",
            dependencies: [
                "CommonPresentation",
                "CommonDomain",
                "IntroDomain",
                "IntroEntity",
                "TTI",
            ],
            path: "intro/presentation/Sources"
        ),

        // MARK: - Search (4-Layer)
        .target(
            name: "SearchEntity",
            dependencies: [
                "CommonEntity",
            ],
            path: "search/entity/Sources"
        ),
        .target(
            name: "SearchDomain",
            dependencies: [
                "CommonDomain",
                "CommonEntity",
                "SearchEntity",
                "TTI",
            ],
            path: "search/domain/Sources"
        ),
        .target(
            name: "SearchData",
            dependencies: [
                "CommonData",
                "CommonDomain",
                "CommonEntity",
                "SearchDomain",
                "SearchEntity",
            ],
            path: "search/data/Sources"
        ),
        .target(
            name: "SearchPresentation",
            dependencies: [
                "CommonPresentation",
                "CommonDomain",
                "CommonEntity",
                "SearchDomain",
                "SearchEntity",
                "FullScreenMediaDomain",
            ],
            path: "search/presentation/Sources"
        ),

        // MARK: - Favorite (4-Layer)
        .target(
            name: "FavoriteEntity",
            dependencies: [
                "CommonEntity",
            ],
            path: "favorite/entity/Sources"
        ),
        .target(
            name: "FavoriteDomain",
            dependencies: [
                "CommonDomain",
                "CommonEntity",
                "FavoriteEntity",
            ],
            path: "favorite/domain/Sources"
        ),
        .target(
            name: "FavoriteData",
            dependencies: [
                "CommonData",
                "CommonDomain",
                "CommonEntity",
                "FavoriteDomain",
                "FavoriteEntity",
            ],
            path: "favorite/data/Sources"
        ),
        .target(
            name: "FavoritePresentation",
            dependencies: [
                "CommonPresentation",
                "CommonDomain",
                "CommonEntity",
                "FavoriteDomain",
                "FavoriteEntity",
                "FullScreenMediaDomain",
            ],
            path: "favorite/presentation/Sources"
        ),

        // MARK: - FullScreenMedia (4-Layer)
        .target(
            name: "FullScreenMediaEntity",
            dependencies: [
                "CommonEntity",
            ],
            path: "fullScreenMedia/entity/Sources"
        ),
        .target(
            name: "FullScreenMediaDomain",
            dependencies: [
                "CommonDomain",
                "CommonEntity",
                "FullScreenMediaEntity",
                "TTI",
            ],
            path: "fullScreenMedia/domain/Sources"
        ),
        .target(
            name: "FullScreenMediaData",
            dependencies: [
                "CommonData",
                "CommonDomain",
                "CommonEntity",
                "FullScreenMediaDomain",
                "FullScreenMediaEntity",
            ],
            path: "fullScreenMedia/data/Sources"
        ),
        .target(
            name: "FullScreenMediaPresentation",
            dependencies: [
                "CommonPresentation",
                "CommonDomain",
                "CommonEntity",
                "FullScreenMediaDomain",
                "FullScreenMediaEntity",
                "TTI",
            ],
            path: "fullScreenMedia/presentation/Sources"
        ),

        // MARK: - Etc (4-Layer)
        .target(
            name: "EtcEntity",
            dependencies: [
                "CommonEntity",
            ],
            path: "etc/entity/Sources"
        ),
        .target(
            name: "EtcDomain",
            dependencies: [
                "CommonDomain",
                "CommonEntity",
                "EtcEntity",
            ],
            path: "etc/domain/Sources"
        ),
        .target(
            name: "EtcData",
            dependencies: [
                "CommonData",
                "CommonDomain",
                "CommonEntity",
                "EtcDomain",
                "EtcEntity",
            ],
            path: "etc/data/Sources"
        ),
        .target(
            name: "EtcPresentation",
            dependencies: [
                "CommonPresentation",
                "CommonDomain",
                "CommonEntity",
                "EtcDomain",
                "EtcEntity",
            ],
            path: "etc/presentation/Sources"
        ),

        // MARK: - Main (4-Layer)
        .target(
            name: "MainEntity",
            dependencies: [
                "CommonEntity",
            ],
            path: "main/entity/Sources"
        ),
        .target(
            name: "MainDomain",
            dependencies: [
                "CommonDomain",
                "MainEntity",
            ],
            path: "main/domain/Sources"
        ),
        .target(
            name: "MainData",
            dependencies: [
                "CommonData",
                "CommonDomain",
                "MainDomain",
                "MainEntity",
            ],
            path: "main/data/Sources"
        ),
        .target(
            name: "MainPresentation",
            dependencies: [
                "MainDomain",
                "MainEntity",
                "CommonPresentation",
                "CommonDomain",
                "CommonEntity",
                "IntroPresentation",
                "SearchPresentation",
                "FavoritePresentation",
                "FullScreenMediaPresentation",
                "EtcPresentation",
                "IntroDomain",
                "SearchDomain",
                "FavoriteDomain",
                "FullScreenMediaDomain",
                "EtcDomain",
            ],
            path: "main/presentation/Sources"
        ),

        // MARK: - Unit Tests
        .testTarget(
            name: "CommonDomainTests",
            dependencies: [
                "CommonDomain",
                "CommonEntity",
                "TTI",
            ],
            path: "common/domain/Tests"
        ),
        .testTarget(
            name: "IntroDomainTests",
            dependencies: [
                "IntroDomain",
                "IntroEntity",
                "SearchDomain",
                "CommonDomain",
                "CommonEntity",
                "TTI",
            ],
            path: "intro/domain/Tests"
        ),
        .testTarget(
            name: "SearchDomainTests",
            dependencies: [
                "SearchDomain",
                "SearchEntity",
                "CommonDomain",
                "CommonEntity",
                "TTI",
            ],
            path: "search/domain/Tests"
        ),
        .testTarget(
            name: "MainDomainTests",
            dependencies: [
                "MainDomain",
                "CommonDomain",
            ],
            path: "main/domain/Tests"
        ),
    ]
)
