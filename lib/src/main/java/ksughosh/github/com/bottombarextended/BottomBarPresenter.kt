package ksughosh.github.com.bottombarextended

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.support.v7.view.menu.*
import android.view.ViewGroup

/**
 * Created by s.kumar on 29.03.18.
 * Copyright © 2017 LOOP. All rights reserved.
 */
open class BottomBarPresenter(protected val menuView: BottomBarMenuView) : MenuPresenter {
    var menu: MenuBuilder? = null
    var updateSuspended: Boolean = false
    protected var mId = 0

    override fun updateMenuView(cleared: Boolean) {
        if (updateSuspended) return
        if (cleared) {
            menuView.buildMenuView()
        } else {
            menuView.updateMenu()
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val selectedItemId = menuView.selectedItemId
        return SavedState(selectedItemId)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is SavedState) {
            menuView.tryRestoreSelectedItemId(state.selectedItemId)
        }
    }

    fun setId(id: Int) {
        mId = id
    }

    override fun getId(): Int = mId

    override fun collapseItemActionView(menu: MenuBuilder?, item: MenuItemImpl?): Boolean = false

    override fun expandItemActionView(menu: MenuBuilder?, item: MenuItemImpl?): Boolean = false

    override fun flagActionItems(): Boolean = false

    override fun onSubMenuSelected(subMenu: SubMenuBuilder?): Boolean = false

    override fun getMenuView(root: ViewGroup?): MenuView = menuView

    override fun setCallback(cb: MenuPresenter.Callback?) {}

    override fun onCloseMenu(menu: MenuBuilder?, allMenusAreClosing: Boolean) {}

    override fun initForMenu(context: Context?, menu: MenuBuilder?) {
        menuView.initialize(menu)
        this.menu = menu
    }

    internal class SavedState(val selectedItemId: Int) : Parcelable {
        constructor(source: Parcel) : this(source.readInt())

        override fun describeContents() = 0

        override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
            writeInt(selectedItemId)
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(source: Parcel): SavedState = SavedState(source)
                override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
            }
        }
    }
}