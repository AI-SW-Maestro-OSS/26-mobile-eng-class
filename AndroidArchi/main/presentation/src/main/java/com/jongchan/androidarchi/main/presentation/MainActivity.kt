package com.jongchan.androidarchi.main.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.metrics.performance.JankStats
import com.jongchan.androidarchi.common.domain.helper.MessageHelper
import com.jongchan.androidarchi.common.domain.helper.NavigationHelper
import com.jongchan.androidarchi.common.presentation.helper.LocalMessageHelper
import com.jongchan.androidarchi.common.presentation.helper.LocalNavigationHelper
import com.jongchan.androidarchi.common.presentation.jank.JankReporter
import com.jongchan.androidarchi.common.presentation.jank.LocalJankReporter
import com.jongchan.androidarchi.main.presentation.deeplink.resolveNewIntentRoute
import com.jongchan.androidarchi.main.presentation.deeplink.resolveStartStack
import com.jongchan.androidarchi.main.presentation.navigation.RootComposable
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var navigationHelper: NavigationHelper

    @Inject
    lateinit var messageHelper: MessageHelper

    @Inject
    lateinit var jankReporter: JankReporter

    private var jankStats: JankStats? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // App Link 진입 시 Intent.data 에서 시작 백스택을 구성한다.
        val startStack = resolveStartStack(intent?.data)

        // JankStats 는 DecorView 가 생성된 이후에만 만들 수 있다. setContent 가 DecorView 를
        // 보장하므로 lifecycle observer 안에서 lazy 하게 생성하고, tracking on/off 와 백그라운드
        // flush 도 함께 처리한다. listener 는 main thread 에서 호출됨이 보장된다.
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                if (jankStats == null) {
                    jankStats = JankStats.createAndTrack(window) { frameData ->
                        jankReporter.onFrame(frameData)
                    }
                }
                jankStats?.isTrackingEnabled = true
            }

            override fun onPause(owner: LifecycleOwner) {
                jankStats?.isTrackingEnabled = false
            }

            override fun onStop(owner: LifecycleOwner) {
                // 앱이 백그라운드로 진입할 때 누적된 통계를 손실 없이 flush.
                jankReporter.onAppBackground()
            }
        })

        enableEdgeToEdge()
        setContent {
            CompositionLocalProvider(
                LocalNavigationHelper provides navigationHelper,
                LocalMessageHelper provides messageHelper,
                LocalJankReporter provides jankReporter,
                ) {
                RootComposable(startStack = startStack)
            }
        }
    }

    /**
     * launchMode 가 singleTop / singleTask 인 경우, 앱 실행 중 들어오는 새 딥링크를 처리한다.
     * URI 를 NavRoute 로 해석만 하고, 실제 dispatch 는 NavigationHelper 의 deep-link 플로우가 담당한다.
     * 웜 스타트는 콜드 스타트의 synthetic 스택과 달리 기존 스택을 보존하고 대상만 최전면으로 올린다
     * (bring-to-front, [com.jongchan.androidarchi.main.presentation.navigation.handleDeepLink]).
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        val uri = intent.data ?: return
        val route = resolveNewIntentRoute(uri) ?: return
        navigationHelper.navigateDeepLink(route)
    }
}
