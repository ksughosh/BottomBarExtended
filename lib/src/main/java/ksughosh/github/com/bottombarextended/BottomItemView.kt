package ksughosh.github.com.bottombarextended

import android.support.annotation.LayoutRes
import android.support.v7.view.menu.MenuView
import android.widget.FrameLayout

/**
 * Created by s.kumar on 29.03.18.
 * Copyright © 2017 LOOP. All rights reserved.
 */
interface BottomItemView : MenuView.ItemView {
    fun setBadge(@LayoutRes resource: Int)
    fun removeBadge()
    fun getBadgeLayout(): FrameLayout?
}