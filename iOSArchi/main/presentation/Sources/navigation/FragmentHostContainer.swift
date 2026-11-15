import CommonPresentation
import FullScreenMediaDomain
import FullScreenMediaPresentation
import SwiftUI
#if canImport(UIKit)
import UIKit

public struct FragmentHostContainer: UIViewControllerRepresentable {
    private let viewModel: FullScreenMediaViewModel

    public init(viewModel: FullScreenMediaViewModel) {
        self.viewModel = viewModel
    }

    public func makeUIViewController(context: Context) -> FullScreenMediaFragment {
        FullScreenMediaFragment(viewModel: viewModel)
    }

    public func updateUIViewController(_ uiViewController: FullScreenMediaFragment, context: Context) {}
}
#else
public struct FragmentHostContainer: View {
    private let viewModel: FullScreenMediaViewModel

    public init(viewModel: FullScreenMediaViewModel) {
        self.viewModel = viewModel
    }

    public var body: some View {
        FullScreenMediaPageView(viewModel: viewModel)
    }
}
#endif
