import CommonPresentation
import Foundation
#if canImport(UIKit)
import UIKit

@MainActor
public final class FullScreenMediaPagerAdapter: NSObject, UICollectionViewDataSource, UICollectionViewDelegateFlowLayout {
    public static let cellReuseIdentifier = "FullScreenMediaPageCell"

    private var items: [MediaItemUiState] = []
    private let onPageSettled: (Int) -> Void
    private let onFirstImageLoaded: () -> Void

    public init(
        onPageSettled: @escaping (Int) -> Void,
        onFirstImageLoaded: @escaping () -> Void
    ) {
        self.onPageSettled = onPageSettled
        self.onFirstImageLoaded = onFirstImageLoaded
        super.init()
    }

    public func submitList(_ newItems: [MediaItemUiState], in collectionView: UICollectionView) {
        self.items = newItems
        collectionView.reloadData()
    }

    public func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        items.count
    }

    public func collectionView(
        _ collectionView: UICollectionView,
        cellForItemAt indexPath: IndexPath
    ) -> UICollectionViewCell {
        guard let cell = collectionView.dequeueReusableCell(
            withReuseIdentifier: Self.cellReuseIdentifier,
            for: indexPath
        ) as? FullScreenMediaPageCell else {
            return UICollectionViewCell()
        }
        let item = items[indexPath.item]
        cell.configure(with: item) { [weak self] in
            self?.onFirstImageLoaded()
        }
        return cell
    }

    public func collectionView(
        _ collectionView: UICollectionView,
        layout collectionViewLayout: UICollectionViewLayout,
        sizeForItemAt indexPath: IndexPath
    ) -> CGSize {
        collectionView.bounds.size
    }

    public func scrollViewDidEndDecelerating(_ scrollView: UIScrollView) {
        let width = max(1, scrollView.bounds.width)
        let page = Int(round(scrollView.contentOffset.x / width))
        onPageSettled(page)
    }
}

@MainActor
public final class FullScreenMediaPageCell: UICollectionViewCell {
    private let imageView = UIImageView()
    private let titleLabel = UILabel()
    private let subtitleLabel = UILabel()
    private var currentTask: Task<Void, Never>?

    public override init(frame: CGRect) {
        super.init(frame: frame)
        setupViews()
    }

    public required init?(coder: NSCoder) {
        super.init(coder: coder)
        setupViews()
    }

    public override func prepareForReuse() {
        super.prepareForReuse()
        currentTask?.cancel()
        imageView.image = nil
    }

    private func setupViews() {
        contentView.backgroundColor = .black

        imageView.contentMode = .scaleAspectFit
        imageView.translatesAutoresizingMaskIntoConstraints = false

        titleLabel.textColor = .white
        titleLabel.font = .systemFont(ofSize: 16, weight: .semibold)
        titleLabel.numberOfLines = 2
        titleLabel.translatesAutoresizingMaskIntoConstraints = false

        subtitleLabel.textColor = .lightGray
        subtitleLabel.font = .systemFont(ofSize: 13, weight: .regular)
        subtitleLabel.numberOfLines = 1
        subtitleLabel.translatesAutoresizingMaskIntoConstraints = false

        contentView.addSubview(imageView)
        contentView.addSubview(titleLabel)
        contentView.addSubview(subtitleLabel)

        NSLayoutConstraint.activate([
            imageView.topAnchor.constraint(equalTo: contentView.topAnchor),
            imageView.leadingAnchor.constraint(equalTo: contentView.leadingAnchor),
            imageView.trailingAnchor.constraint(equalTo: contentView.trailingAnchor),
            imageView.bottomAnchor.constraint(equalTo: contentView.bottomAnchor),

            subtitleLabel.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: 20),
            subtitleLabel.trailingAnchor.constraint(equalTo: contentView.trailingAnchor, constant: -20),
            subtitleLabel.bottomAnchor.constraint(equalTo: contentView.safeAreaLayoutGuide.bottomAnchor, constant: -24),

            titleLabel.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: 20),
            titleLabel.trailingAnchor.constraint(equalTo: contentView.trailingAnchor, constant: -20),
            titleLabel.bottomAnchor.constraint(equalTo: subtitleLabel.topAnchor, constant: -6)
        ])
    }

    public func configure(with item: MediaItemUiState, onLoaded: @escaping () -> Void) {
        titleLabel.text = item.title
        subtitleLabel.text = "\(item.source) · \(formatKakaoDateTime(item.dateTime))"

        let urlStr = !item.mediaUrl.isEmpty ? item.mediaUrl : item.thumbnailUrl
        if let cached = ImageCacheStore.shared.data(for: urlStr),
           let uiImage = UIImage(data: cached) {
            imageView.image = uiImage
            onLoaded()
            return
        }

        guard let url = URL(string: urlStr) else {
            onLoaded()
            return
        }

        currentTask = Task { [weak self] in
            if let (data, _) = try? await URLSession.shared.data(from: url),
               !Task.isCancelled,
               let uiImage = UIImage(data: data) {
                ImageCacheStore.shared.setData(data, for: urlStr)
                await MainActor.run {
                    self?.imageView.image = uiImage
                    onLoaded()
                }
            }
        }
    }
}
#endif
