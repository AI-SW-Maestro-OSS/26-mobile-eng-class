package com.jongchan.androidarchi.etc.domain

import com.jongchan.androidarchi.common.domain.navigation.NavRoute
import com.jongchan.androidarchi.common.domain.navigation.Page

object EtcPage : Page {
    const val PATH = "/etc"

    override fun toRoute(): NavRoute = NavRoute(PATH)
}
