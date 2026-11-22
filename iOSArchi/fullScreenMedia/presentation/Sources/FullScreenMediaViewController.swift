import Combine
import CommonPresentation
import FullScreenMediaDomain
import SwiftUI
#if canImport(UIKit)
import UIKit

@MainActor
public final class FullScreenMediaViewController: UIViewController {
    private let viewModel: FullScreenMediaViewModel
    private var cancellables = Set<AnyCancellable>()
    private var hasScrolledToInitialPage = false

    private lazy var collectionDataSource = FullScreenMediaCollectionDataSource(
        onPageSettled: { [weak self] page in
            self?.viewModel.onIntent(.pageChanged(page))
        },
        onFirstImageLoaded: { [weak self] in
            self?.viewModel.onIntent(.onFirstImageLoaded)
        }
    )

    private lazy var collectionView: UICollectionView = {
        let layout = UICollectionViewFlowLayout()
        layout.scrollDirection = .horizontal
        layout.minimumLineSpacing = 0
        layout.minimumInteritemSpacing = 0
        let cv = UICollectionView(frame: .zero, collectionViewLayout: layout)
        cv.isPagingEnabled = true
        cv.showsHorizontalScrollIndicator = false
        cv.backgroundColor = .black
        cv.register(
            FullScreenMediaPageCell.self,
            forCellWithReuseIdentifier: FullScreenMediaCollectionDataSource.cellReuseIdentifier
        )
        cv.dataSource = collectionDataSource
        cv.delegate = collectionDataSource
        cv.translatesAutoresizingMaskIntoConstraints = false
        return cv
    }()

    private let backButton = UIButton(type: .system)
    private let pageIndicatorLabel = UILabel()
    private let favoriteButton = UIButton(type: .system)

    public init(viewModel: FullScreenMediaViewModel) {
        self.viewModel = viewModel
        super.init(nibName: nil, bundle: nil)
    }

    public required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    public override func viewDidLoad() {
        super.viewDidLoad()
        viewModel.markViewCreated()
        setupUI()
        bindViewModel()
        viewModel.onIntent(.loadInitialItems)
    }

    private func setupUI() {
        view.backgroundColor = .black
        view.addSubview(collectionView)

        backButton.setImage(UIImage(systemName: "chevron.left"), for: .normal)
        backButton.tintColor = .white
        backButton.addTarget(self, action: #selector(onBackTapped), for: .touchUpInside)
        backButton.translatesAutoresizingMaskIntoConstraints = false

        pageIndicatorLabel.textColor = .white
        pageIndicatorLabel.font = .systemFont(ofSize: 16, weight: .semibold)
        pageIndicatorLabel.textAlignment = .center
        pageIndicatorLabel.translatesAutoresizingMaskIntoConstraints = false

        favoriteButton.setImage(UIImage(systemName: "heart"), for: .normal)
        favoriteButton.tintColor = .white
        favoriteButton.addTarget(self, action: #selector(onFavoriteTapped), for: .touchUpInside)
        favoriteButton.translatesAutoresizingMaskIntoConstraints = false

        view.addSubview(backButton)
        view.addSubview(pageIndicatorLabel)
        view.addSubview(favoriteButton)

        NSLayoutConstraint.activate([
            collectionView.topAnchor.constraint(equalTo: view.topAnchor),
            collectionView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            collectionView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            collectionView.bottomAnchor.constraint(equalTo: view.bottomAnchor),

            backButton.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 8),
            backButton.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 12),
            backButton.widthAnchor.constraint(equalToConstant: 44),
            backButton.heightAnchor.constraint(equalToConstant: 44),

            pageIndicatorLabel.centerYAnchor.constraint(equalTo: backButton.centerYAnchor),
            pageIndicatorLabel.centerXAnchor.constraint(equalTo: view.centerXAnchor),

            favoriteButton.centerYAnchor.constraint(equalTo: backButton.centerYAnchor),
            favoriteButton.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -12),
            favoriteButton.widthAnchor.constraint(equalToConstant: 44),
            favoriteButton.heightAnchor.constraint(equalToConstant: 44)
        ])
    }

    private func bindViewModel() {
        viewModel.$uiState
            .receive(on: DispatchQueue.main)
            .sink { [weak self] state in
                self?.render(state: state)
            }
            .store(in: &cancellables)
    }

    private func render(state: FullScreenMediaUIState) {
        collectionDataSource.updateItems(state.items, in: collectionView)

        if !state.items.isEmpty {
            pageIndicatorLabel.text = "\(state.currentIndex + 1) / \(state.items.count)"
            if !hasScrolledToInitialPage {
                hasScrolledToInitialPage = true
                DispatchQueue.main.async { [weak self] in
                    guard let self, state.currentIndex < state.items.count else { return }
                    let indexPath = IndexPath(item: state.currentIndex, section: 0)
                    self.collectionView.scrollToItem(at: indexPath, at: .centeredHorizontally, animated: false)
                }
            }
        } else {
            pageIndicatorLabel.text = state.isLoading ? "Loading..." : "0 / 0"
        }

        if let current = state.currentItem {
            let heartIcon = current.isFavorite ? "heart.fill" : "heart"
            favoriteButton.setImage(UIImage(systemName: heartIcon), for: .normal)
            favoriteButton.tintColor = current.isFavorite ? .systemRed : .white
            favoriteButton.isHidden = false
        } else {
            favoriteButton.isHidden = true
        }
    }

    @objc private func onBackTapped() {
        viewModel.onIntent(.navigateBack)
    }

    @objc private func onFavoriteTapped() {
        if let current = viewModel.currentState.currentItem {
            viewModel.onIntent(.toggleFavorite(current))
        }
    }
}
#endif

public struct FullScreenMediaPageView: View {
    @ObservedObject private var viewModel: FullScreenMediaViewModel

    public init(viewModel: FullScreenMediaViewModel) {
        self.viewModel = viewModel
    }

    public var body: some View {
        ZStack(alignment: .top) {
            DesignSystemThemeImpl.designSystemColor.black
                .ignoresSafeArea()

            if viewModel.uiState.items.isEmpty {
                if viewModel.uiState.isLoading {
                    ProgressView()
                        .tint(DesignSystemThemeImpl.designSystemColor.white)
                } else {
                    ArchiText(
                        text: "No media items",
                        style: DesignSystemThemeImpl.typeScale.textRegularM,
                        color: DesignSystemThemeImpl.designSystemColor.white
                    )
                }
            } else if let item = viewModel.uiState.currentItem {
                VStack {
                    Spacer()
                    ArchiAsyncImage(
                        urlString: !item.mediaUrl.isEmpty ? item.mediaUrl : item.thumbnailUrl,
                        contentMode: .fit,
                        onSuccess: {
                            viewModel.onIntent(.onFirstImageLoaded)
                        }
                    )
                    Spacer()
                    VStack(alignment: .leading, spacing: 4) {
                        ArchiText(
                            text: item.title,
                            style: DesignSystemThemeImpl.typeScale.textStrongM,
                            color: DesignSystemThemeImpl.designSystemColor.white
                        )
                        ArchiText(
                            text: "\(item.source) · \(formatKakaoDateTime(item.dateTime))",
                            style: DesignSystemThemeImpl.typeScale.textRegularXS,
                            color: DesignSystemThemeImpl.designSystemColor.textCaption
                        )
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(20)
                }
            }

            HStack {
                BackArrowButton(tintColor: DesignSystemThemeImpl.designSystemColor.white) {
                    viewModel.onIntent(.navigateBack)
                }
                Spacer()
                if !viewModel.uiState.items.isEmpty {
                    ArchiText(
                        text: "\(viewModel.uiState.currentIndex + 1) / \(viewModel.uiState.items.count)",
                        style: DesignSystemThemeImpl.typeScale.textStrongM,
                        color: DesignSystemThemeImpl.designSystemColor.white
                    )
                }
                Spacer()
                if let current = viewModel.uiState.currentItem {
                    FavoriteHeartButton(isFavorite: current.isFavorite) {
                        viewModel.onIntent(.toggleFavorite(current))
                    }
                }
            }
            .padding(.horizontal, 12)
            .padding(.top, 8)
        }
        .jankPageEffect(FullScreenMediaPage.path)
        .onAppear {
            viewModel.markViewCreated()
            viewModel.onIntent(.loadInitialItems)
        }
    }
}
