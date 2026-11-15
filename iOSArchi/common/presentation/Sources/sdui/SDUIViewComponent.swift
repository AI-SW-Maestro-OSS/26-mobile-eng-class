import CommonDomain
import SwiftUI

public struct TitleDescView: View {
    private let vo: TitleDescViewTypeVO

    public init(vo: TitleDescViewTypeVO) {
        self.vo = vo
    }

    public var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            if !vo.titleText.isEmpty {
                ArchiText(
                    attributedText: RichTextBuilder.decodeAndBuild(from: vo.titleText),
                    style: DesignSystemThemeImpl.typeScale.textStrongM
                )
            }
            if !vo.descriptionText.isEmpty {
                ArchiText(
                    attributedText: RichTextBuilder.decodeAndBuild(from: vo.descriptionText),
                    style: DesignSystemThemeImpl.typeScale.textRegularS
                )
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(14)
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(DesignSystemThemeImpl.designSystemColor.bgSecondary)
        )
    }
}

public struct TitleDescEndImageView: View {
    private let vo: TitleDescEndImageViewTypeVO

    public init(vo: TitleDescEndImageViewTypeVO) {
        self.vo = vo
    }

    public var body: some View {
        HStack(spacing: 12) {
            VStack(alignment: .leading, spacing: 6) {
                if !vo.titleText.isEmpty {
                    ArchiText(
                        attributedText: RichTextBuilder.decodeAndBuild(from: vo.titleText),
                        style: DesignSystemThemeImpl.typeScale.textStrongM
                    )
                }
                if !vo.descriptionText.isEmpty {
                    ArchiText(
                        attributedText: RichTextBuilder.decodeAndBuild(from: vo.descriptionText),
                        style: DesignSystemThemeImpl.typeScale.textRegularS
                    )
                }
            }
            Spacer(minLength: 8)
            if !vo.endImage.imgUrl.isEmpty {
                ArchiAsyncImage(urlString: vo.endImage.imgUrl)
                    .frame(
                        width: vo.endImage.size.width > 0 ? CGFloat(vo.endImage.size.width) : 56,
                        height: vo.endImage.size.height > 0 ? CGFloat(vo.endImage.size.height) : 56
                    )
                    .clipShape(RoundedRectangle(cornerRadius: 8))
            }
        }
        .padding(14)
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(DesignSystemThemeImpl.designSystemColor.bgSecondary)
        )
    }
}

public struct StartImageTitleDescActionIconView: View {
    private let vo: StartImageTitleDescActionIconViewTypeVO
    private let onActionClick: ((String) -> Void)?

    public init(
        vo: StartImageTitleDescActionIconViewTypeVO,
        onActionClick: ((String) -> Void)? = nil
    ) {
        self.vo = vo
        self.onActionClick = onActionClick
    }

    public var body: some View {
        HStack(spacing: 12) {
            if !vo.image.imgUrl.isEmpty {
                ArchiAsyncImage(urlString: vo.image.imgUrl)
                    .frame(
                        width: vo.image.size.width > 0 ? CGFloat(vo.image.size.width) : 44,
                        height: vo.image.size.height > 0 ? CGFloat(vo.image.size.height) : 44
                    )
                    .clipShape(RoundedRectangle(cornerRadius: 8))
            }
            VStack(alignment: .leading, spacing: 4) {
                if !vo.titleText.isEmpty {
                    ArchiText(
                        attributedText: RichTextBuilder.decodeAndBuild(from: vo.titleText),
                        style: DesignSystemThemeImpl.typeScale.textStrongM
                    )
                }
                if !vo.descriptionText.isEmpty {
                    ArchiText(
                        attributedText: RichTextBuilder.decodeAndBuild(from: vo.descriptionText),
                        style: DesignSystemThemeImpl.typeScale.textRegularS
                    )
                }
            }
            Spacer(minLength: 8)
            Image(systemName: "chevron.right")
                .font(DesignSystemThemeImpl.typeScale.textRegularS.font)
                .foregroundColor(DesignSystemThemeImpl.designSystemColor.textCaption)
        }
        .padding(14)
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(DesignSystemThemeImpl.designSystemColor.bgSecondary)
        )
        .contentShape(Rectangle())
        .onTapGesture {
            if !vo.actionIcon.action.navigateLink.isEmpty {
                onActionClick?(vo.actionIcon.action.navigateLink)
            }
        }
    }
}
