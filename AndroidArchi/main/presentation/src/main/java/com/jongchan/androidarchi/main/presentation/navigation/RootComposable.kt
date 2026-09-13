package com.jongchan.androidarchi.main.presentation.navigation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.jongchan.androidarchi.common.domain.message.MessageEffect
import com.jongchan.androidarchi.common.domain.navigation.Page
import com.jongchan.androidarchi.common.presentation.R
import com.jongchan.androidarchi.common.presentation.component.ArchiText
import com.jongchan.androidarchi.common.presentation.helper.LocalMessageHelper
import com.jongchan.androidarchi.common.presentation.helper.LocalNavigationHelper
import com.jongchan.androidarchi.common.presentation.ui.theme.DesignSystemTheme
import com.jongchan.androidarchi.common.presentation.ui.theme.DesignSystemThemeImpl
import com.jongchan.androidarchi.etc.domain.EtcPage
import com.jongchan.androidarchi.favorite.domain.FavoritePage
import com.jongchan.androidarchi.intro.domain.IntroPage
import com.jongchan.androidarchi.search.domain.SearchPage
import kotlinx.coroutines.flow.Flow

@Composable
fun RootComposable(
    modifier: Modifier = Modifier,
    startStack: List<NavKey> = listOf(GenericNavKey(IntroPage.PATH)),
) {
    val snackBarHostState = remember { SnackbarHostState() }
    var oneButtonDialogEffect by remember {
        mutableStateOf<MessageEffect.ShowOneButtonDialog?>(null)
    }

    DesignSystemTheme {
        val backStack = rememberNavBackStack(*startStack.toTypedArray())
        val navigationHelper = LocalNavigationHelper.current
        val messageHelper = LocalMessageHelper.current

        val tabs = listOf(
            TopNavTab(stringResource(R.string.nav_tab_search), SearchPage),
            TopNavTab(stringResource(R.string.nav_tab_favorite), FavoritePage),
            TopNavTab(stringResource(R.string.nav_tab_etc), EtcPage),
        )
        val currentKey = backStack.lastOrNull() as? GenericNavKey
        val currentRoute = currentKey?.let { appRouteByPath[it.path] }

        val onShowOneButtonDialog = remember<(MessageEffect.ShowOneButtonDialog) -> Unit> {
            { oneButtonDialogEffect = it }
        }
        MessageEffect(
            messageEffectFlow = messageHelper.effect,
            snackBarHostState = snackBarHostState,
            onShowOneButtonDialog = onShowOneButtonDialog,
        )

        OneButtonDialogEffect(
            dialog = oneButtonDialogEffect,
            onDismiss = { oneButtonDialogEffect = null },
        )

        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .background(DesignSystemThemeImpl.designSystemColor.bgDefaultLevel1),
            topBar = {
                if (currentRoute?.isBottomTab == true) {
                    TopTabBar(
                        tabs = tabs,
                        currentPath = currentKey.path,
                        onTabSelected = { page -> navigationHelper.navigateTo(page) },
                    )
                }
            },
            snackbarHost = { SnackbarHost(snackBarHostState) },
        ) { innerPadding ->
            AppNavHost(
                backStack = backStack,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun TopTabBar(
    tabs: List<TopNavTab>,
    currentPath: String?,
    onTabSelected: (Page) -> Unit,
) {
    val selectedIndex = tabs.indexOfFirst { it.page.toRoute().path == currentPath }
        .coerceAtLeast(0)

    Box(
        modifier = Modifier
            .background(DesignSystemThemeImpl.designSystemColor.bgDefaultLevel1)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp),
            // 커스텀 topBar 라 상태바 inset 을 직접 소비해야 함(기본 TopAppBar 와 달리 자동 적용 X).
            // background 뒤에 적용해 흰 배경은 상태바까지 덮고, 탭 내용만 상태바 아래로 내린다.
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                tabs.forEachIndexed { index, tab ->
                    val selected = index == selectedIndex
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onTabSelected(tab.page) },
                        contentAlignment = Alignment.Center,
                    ) {
                        ArchiText(
                            text = tab.label,
                            style = if (selected) DesignSystemThemeImpl.typeScale.textStrongL else DesignSystemThemeImpl.typeScale.textRegularL,
                            color = if (selected) DesignSystemThemeImpl.designSystemColor.contentDefaultLevel1 else DesignSystemThemeImpl.designSystemColor.contentDefaultLevel3,
                        )
                        if (selected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .height(2.dp)
                                    .background(DesignSystemThemeImpl.designSystemColor.contentDefaultLevel1),
                            )
                        }
                    }
                }
            }
            HorizontalDivider(thickness = 0.5.dp, color = DesignSystemThemeImpl.designSystemColor.borderDefaultLevel1)
        }
    }
}

@Composable
private fun MessageEffect(
    messageEffectFlow: Flow<MessageEffect>,
    snackBarHostState: SnackbarHostState,
    onShowOneButtonDialog: (MessageEffect.ShowOneButtonDialog) -> Unit,
) {
    val appContext = LocalContext.current.applicationContext

    LaunchedEffect(Unit) {
        messageEffectFlow.collect { effect ->
            when (effect) {
                is MessageEffect.ShowToastMsg -> Toast.makeText(
                    appContext,
                    effect.message,
                    Toast.LENGTH_LONG
                ).show()

                is MessageEffect.ShowSnackBarError -> snackBarHostState.showSnackbar(effect.message)
                is MessageEffect.ShowOneButtonDialog -> onShowOneButtonDialog(effect)
            }
        }
    }
}

@Composable
private fun OneButtonDialogEffect(
    dialog: MessageEffect.ShowOneButtonDialog?,
    onDismiss: () -> Unit,
) {
    if (dialog == null) return

    AlertDialog(
        onDismissRequest = {
            if (!dialog.cantIgnore) onDismiss()
        },
        title = dialog.titleText?.let { titleText ->
            {
                ArchiText(
                    text = titleText,
                    style = DesignSystemThemeImpl.typeScale.titleStrongL,
                    color = DesignSystemThemeImpl.designSystemColor.contentDefaultLevel1,
                    maxLines = Int.MAX_VALUE,
                )
            }
        },
        text = {
            ArchiText(
                text = dialog.descText,
                style = DesignSystemThemeImpl.typeScale.textRegularL,
                color = DesignSystemThemeImpl.designSystemColor.contentDefaultLevel2,
                maxLines = Int.MAX_VALUE,
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    dialog.onClickButton?.invoke()
                    onDismiss()
                }
            ) {
                ArchiText(
                    text = dialog.buttonText,
                    style = DesignSystemThemeImpl.typeScale.textStrongL,
                    color = DesignSystemThemeImpl.designSystemColor.contentAccent,
                )
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = !dialog.cantIgnore,
            dismissOnClickOutside = !dialog.cantIgnore,
        ),
    )
}

private data class TopNavTab(
    val label: String,
    val page: Page,
)
