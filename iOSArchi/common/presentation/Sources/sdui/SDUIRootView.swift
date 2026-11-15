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
        case .TitleDescriptionViewType:
            if let vo = item.content as? TitleDescViewTypeVO {
                TitleDescView(vo: vo)
            }
        case .TitleDescriptionEndImageViewType:
            if let vo = item.content as? TitleDescEndImageViewTypeVO {
                TitleDescEndImageView(vo: vo)
            }
        case .StartImageTitleDescriptionActionIconType:
            if let vo = item.content as? StartImageTitleDescActionIconViewTypeVO {
                StartImageTitleDescActionIconView(vo: vo, onActionClick: onActionClick)
            }
        }
    }
}
