import CommonPresentation
import FullScreenMediaDomain
import FullScreenMediaPresentation
import SwiftUI
#if canImport(UIKit)
import UIKit

public struct ViewControllerHostContainer: UIViewControllerRepresentable {
    private let viewModel: FullScreenMediaViewModel

    public init(viewModel: FullScreenMediaViewModel) {
        self.viewModel = viewModel
    }

    public func makeUIViewController(context: Context) -> FullScreenMediaViewController {
        FullScreenMediaViewController(viewModel: viewModel)
    }

    public func updateUIViewController(_ uiViewController: FullScreenMediaViewController, context: Context) {}
}
#else
public struct ViewControllerHostContainer: View {
    private let viewModel: FullScreenMediaViewModel

    public init(viewModel: FullScreenMediaViewModel) {
        self.viewModel = viewModel
    }

    public var body: some View {
        FullScreenMediaPageView(viewModel: viewModel)
    }
}
#endif
