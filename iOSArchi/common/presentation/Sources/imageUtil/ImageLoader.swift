import Foundation
import SwiftUI

public final class ImageCacheStore: @unchecked Sendable {
    public static let shared = ImageCacheStore()
    private let cache = NSCache<NSString, NSData>()

    private init() {
        cache.countLimit = 250
    }

    public func data(for url: String) -> Data? {
        cache.object(forKey: url as NSString) as Data?
    }

    public func setData(_ data: Data, for url: String) {
        cache.setObject(data as NSData, forKey: url as NSString)
    }
}

public struct ArchiAsyncImage: View {
    private let urlString: String
    private let contentMode: ContentMode
    private let onSuccess: (() -> Void)?
    private let onError: (() -> Void)?

    @State private var loadedImage: Image?
    @State private var loadFailed = false

    public init(
        urlString: String,
        contentMode: ContentMode = .fill,
        onSuccess: (() -> Void)? = nil,
        onError: (() -> Void)? = nil
    ) {
        self.urlString = urlString
        self.contentMode = contentMode
        self.onSuccess = onSuccess
        self.onError = onError
    }

    public var body: some View {
        ZStack {
            if urlString.hasPrefix("sf:") {
                ZStack {
                    DesignSystemThemeImpl.designSystemColor.bgTertiary
                    Image(systemName: String(urlString.dropFirst(3)))
                        .foregroundColor(DesignSystemThemeImpl.designSystemColor.accentBlue)
                }
            } else if let loadedImage {
                loadedImage
                    .resizable()
                    .aspectRatio(contentMode: contentMode)
            } else if loadFailed {
                ZStack {
                    DesignSystemThemeImpl.designSystemColor.bgTertiary
                    Image(systemName: "photo")
                        .foregroundColor(DesignSystemThemeImpl.designSystemColor.textCaption)
                }
            } else {
                ZStack {
                    DesignSystemThemeImpl.designSystemColor.bgSecondary
                    ProgressView()
                }
            }
        }
        .task(id: urlString) {
            guard !urlString.hasPrefix("sf:") else { return }
            await load()
        }
    }

    @MainActor
    private func load() async {
        guard !urlString.isEmpty, let url = URL(string: urlString) else {
            loadFailed = true
            onError?()
            return
        }

        if let cached = ImageCacheStore.shared.data(for: urlString),
           let image = Self.makeImage(from: cached) {
            loadedImage = image
            onSuccess?()
            return
        }

        do {
            let (data, _) = try await URLSession.shared.data(from: url)
            if let image = Self.makeImage(from: data) {
                ImageCacheStore.shared.setData(data, for: urlString)
                loadedImage = image
                onSuccess?()
            } else {
                loadFailed = true
                onError?()
            }
        } catch {
            loadFailed = true
            onError?()
        }
    }

    private static func makeImage(from data: Data) -> Image? {
        #if canImport(UIKit)
        if let uiImage = UIImage(data: data) {
            return Image(uiImage: uiImage)
        }
        return nil
        #elseif canImport(AppKit)
        if let nsImage = NSImage(data: data) {
            return Image(nsImage: nsImage)
        }
        return nil
        #else
        return nil
        #endif
    }
}
