package ksughosh.github.com.bottombarview

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.view.menu.MenuBuilder
import android.support.v7.view.menu.MenuItemImpl
import android.view.MenuItem
import android.view.SubMenu

/**
 * Created by s.kumar on 29.03.18.
 * Copyright © 2017 LOOP. All rights reserved.
 */
@SuppressLint("RestrictedApi")
open class BottomBarMenu(context: Context?) : MenuBuilder(context) {
    override fun addSubMenu(title: CharSequence?): SubMenu {
        throw UnsupportedOperationException("BottomNavigationView does not support submenus")
    }

    override fun addInternal(group: Int, id: Int, categoryOrder: Int, title: CharSequence?): MenuItem {
        if (size() + 1 > MAX_ITEM_COUNT) {
            throw IllegalArgumentException(
                    "Maximum number of items supported by BottomNavigationView is " + MAX_ITEM_COUNT
                            + ". Limit can be checked with BottomNavigationView#getMaxItemCount()")
        }
        stopDispatchingItemsChanged()
        val item = super.addInternal(group, id, categoryOrder, title)
        if (item is MenuItemImpl) {
            item.isExclusiveCheckable = true
        }
        startDispatchingItemsChanged()
        return item
    }

    companion object {
        const val MAX_ITEM_COUNT = 5
    }
}