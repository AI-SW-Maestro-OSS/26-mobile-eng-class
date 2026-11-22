import CommonDomain
import SwiftUI

public struct SDUIRootView: View {
    private let item: SDUIViewTypeVO
    private let onActionClick: ((String) -> Void)?

    public init(
        item: SDUIViewTypeVO,
        onActionClick: ((String) -> Void)? = nil
    ) {
        self.item = item
        self.onActionClick = onActionClick
    }

    public var body: some View {
        switch item.viewType {
        case .titleDescription:
            if let vo = item.content as? TitleDescViewTypeVO {
                TitleDescView(vo: vo)
            }
        case .titleDescriptionEndImage:
            if let vo = item.content as? TitleDescEndImageViewTypeVO {
                TitleDescEndImageView(vo: vo)
            }
        case .startImageTitleDescriptionActionIcon:
            if let vo = item.content as? StartImageTitleDescActionIconViewTypeVO {
                StartImageTitleDescActionIconView(vo: vo, onActionClick: onActionClick)
            }
        }
    }
}
