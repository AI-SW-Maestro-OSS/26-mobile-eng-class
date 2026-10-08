package com.jongchan.androidarchi.fullScreenMedia.presentation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RawRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import com.jongchan.androidarchi.common.entity.media.MediaItemVO
import com.jongchan.androidarchi.fullScreenMedia.domain.FullScreenMediaPage
import com.jongchan.androidarchi.fullScreenMedia.presentation.databinding.FragmentFullScreenMediaBinding
import com.jongchan.androidarchi.tti.TimelineCategory
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback
import kotlinx.coroutines.launch
import com.jongchan.androidarchi.common.presentation.R as CommonR

/**
 * 상세화면(전체화면 미디어) View 구현.
 *
 * - 스와이프 대상은 이미지 페이저(ViewPager2)뿐이다.
 * - 뒤로가기/타이틀/하트는 페이저 위에 고정된 오버레이로, 페이지 전환·즐겨찾기 변경 시
 *   현재 페이지 데이터에 맞춰 이 Fragment 가 갱신한다.
 * - 검색 진입: 선택 항목 고정 노출(스와이프 비활성).
 * - 즐겨찾기 진입: 좌우 스와이프로 이전/다음 즐겨찾기 페이징.
 * - 하트 토글은 [FullScreenMediaViewModel] 을 통해 검색/보관함/상세 3곳에 동기화된다.
 *
 * Args 는 호스트(Compose 라우터)가 NavRoute.args(String 맵)를 그대로 [arguments] 로 전달한다.
 */
@AndroidEntryPoint
class FullScreenMediaFragment : Fragment() {

    private var _binding: FragmentFullScreenMediaBinding? = null
    private val binding get() = _binding!!

    private val args: FullScreenMediaPage.Args by lazy {
        FullScreenMediaPage.Args.from(readArgs())
    }

    private fun readArgs(): Map<String, String> {
        val bundle = arguments ?: return emptyMap()
        return bundle.keySet().associateWith { key -> bundle.getString(key).orEmpty() }
    }

    private val viewModel: FullScreenMediaViewModel by viewModels(
        extrasProducer = {
            defaultViewModelCreationExtras.withCreationCallback<FullScreenMediaViewModel.Factory> { factory ->
                factory.create(args)
            }
        }
    )

    private lateinit var pagerAdapter: FullScreenMediaPagerAdapter

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            viewModel.onIntent(FullScreenMediaIntent.SelectPage(position))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // 트래킹 시작은 ViewModel 생성 시점(init)에 일어난다.
        // 이 첫 접근이 ViewModel 생성을 트리거하므로 "트래킹 시작 → view 마크" 순서가 보장된다.
        viewModel.ttiHelper.startTTITimeline(TimelineCategory.VIEW_CREATION_TIME)
        _binding = FragmentFullScreenMediaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPager()
        setupOverlay()
        observeState()
        viewModel.ttiHelper.endTTITimeline(TimelineCategory.VIEW_CREATION_TIME)
        viewModel.ttiHelper.startTTITimeline(TimelineCategory.VIEW_BINDING_TIME)
    }

    private fun setupPager() {
        pagerAdapter = FullScreenMediaPagerAdapter(onImageLoadStarted = {
            viewModel.ttiHelper.startTTITimeline(TimelineCategory.IMAGE_LOADED_TIME)
        }, onImageLoadCompleted = {
            viewModel.ttiHelper.endTTITimeline(TimelineCategory.IMAGE_LOADED_TIME)
            viewModel.ttiHelper.endTTITracking()
        })
        binding.mediaPager.apply {
            adapter = pagerAdapter
            offscreenPageLimit = 1
            registerOnPageChangeCallback(pageChangeCallback)
        }
    }

    private fun setupOverlay() {
        binding.backButton.setOnClickListener {
            viewModel.onIntent(FullScreenMediaIntent.ClickBackButton)
        }
        binding.favoriteButton.setOnClickListener {
            val state = viewModel.uiState.value
            val position = binding.mediaPager.currentItem
            val item = state.mediaItems.getOrNull(position) ?: return@setOnClickListener
            onFavoriteToggle(item, item.urlKey in state.favoriteUrls)
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    private fun render(state: FullScreenMediaUIState) {
        val showPager = !state.isLoading && state.mediaItems.isNotEmpty()
        binding.loadingIndicator.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        binding.emptyText.visibility =
            if (!state.isLoading && state.mediaItems.isEmpty()) View.VISIBLE else View.GONE
        binding.mediaPager.visibility = if (showPager) View.VISIBLE else View.GONE
        binding.titleContainer.visibility = if (showPager) View.VISIBLE else View.GONE
        binding.favoriteButton.visibility = if (showPager) View.VISIBLE else View.GONE

        binding.mediaPager.isUserInputEnabled = state.swipeEnabled
        pagerAdapter.submit(state.mediaItems)

        if (!showPager) return

        // 페이저 위치를 상태의 currentIndex 로 맞춘다. 뷰 최초 생성·회전 후 재생성 시 보존된
        // 위치로 복원하고, 즐겨찾기 셋 변경 등으로 위치가 그대로면 setCurrentItem 은 건너뛴다.
        if (binding.mediaPager.currentItem != state.currentIndex) {
            binding.mediaPager.setCurrentItem(state.currentIndex, false)
        }
        bindOverlay(state, state.currentIndex)
        viewModel.ttiHelper.endTTITimeline(TimelineCategory.VIEW_BINDING_TIME)
    }

    /** 현재 페이지의 타이틀/하트 상태를 오버레이에 반영한다. */
    private fun bindOverlay(state: FullScreenMediaUIState, position: Int) {
        val item = state.mediaItems.getOrNull(position) ?: return
        binding.titleText.text =
            item.title.ifEmpty { getString(CommonR.string.media_default_title) }
        binding.favoriteButton.setImageResource(
            if (item.urlKey in state.favoriteUrls) {
                CommonR.drawable.fill_favorite_24dp
            } else {
                CommonR.drawable.outlined_favorite_24dp
            }
        )
    }

    private fun onFavoriteToggle(item: MediaItemVO, isFavorite: Boolean) {
        if (isFavorite) {
            playHeartAnimation(CommonR.raw.clear_heart_animated)
            viewModel.onIntent(FullScreenMediaIntent.DeleteFavorite(item.urlKey))
        } else {
            playHeartAnimation(CommonR.raw.heart_animated)
            viewModel.onIntent(FullScreenMediaIntent.AddFavorite(item))
        }
    }

    private fun playHeartAnimation(@RawRes rawRes: Int) {
        binding.heartAnimation.apply {
            cancelAnimation()
            removeAllAnimatorListeners()
            setAnimation(rawRes)
            progress = 0f
            visibility = View.VISIBLE
            addAnimatorListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    visibility = View.GONE
                    removeAnimatorListener(this)
                }
            })
            playAnimation()
        }
    }

    override fun onDestroyView() {
        binding.mediaPager.unregisterOnPageChangeCallback(pageChangeCallback)
        binding.mediaPager.adapter = null
        _binding = null
        viewModel.ttiHelper.shotTTILogging()
        super.onDestroyView()
    }
}
