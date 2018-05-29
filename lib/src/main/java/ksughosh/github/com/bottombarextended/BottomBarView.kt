package ksughosh.github.com.bottombarextended

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.DrawableRes
import android.support.annotation.IdRes
import android.support.annotation.LayoutRes
import android.support.v4.content.ContextCompat
import android.support.v4.view.AbsSavedState
import android.support.v4.view.ViewCompat
import android.support.v7.content.res.AppCompatResources
import android.support.v7.view.SupportMenuInflater
import android.support.v7.view.menu.MenuBuilder
import android.support.v7.widget.TintTypedArray
import android.util.AttributeSet
import android.util.TypedValue
import android.view.*
import android.widget.FrameLayout

/**
 * Created by s.kumar on 29.03.18.
 * Copyright © 2017 LOOP. All rights reserved.
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
@SuppressLint("RestrictedApi")
open class BottomBarView : FrameLayout {
    val menu: Menu
        get() = menuBuilder

    protected val menuBuilder: MenuBuilder
    protected val presenter: BottomBarPresenter
    protected val menuInflater by lazy { SupportMenuInflater(context) }

    val menuView: BottomBarMenuView

    var itemIconTintList: ColorStateList?
        get() = menuView.itemIconTintList
        set(value) {
            menuView.itemIconTintList = value
        }

    var itemTextColor: ColorStateList?
        get() = menuView.itemTextColor
        set(value) {
            menuView.itemTextColor = value
        }

    var itemBackgroundResource: Int
        get() = menuView.itemBackgroundResource
        set(value) {
            menuView.itemBackgroundResource = value
        }

    var selectedItemId: Int
        get() = menuView.selectedItemId
        set(itemId) {
            val item = menuBuilder.findItem(itemId)
            if (item != null) {
                if (!menuBuilder.performItemAction(item, presenter, 0)) {
                    item.isChecked = true
                }
            }
        }

    protected open var reselectedListener: ((MenuItem) -> Unit)? = null
    protected open var selectedListener: ((MenuItem) -> Boolean)? = null

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        // optional can be removed
        context?.checkAppCompatTheme()

        menuBuilder = BottomBarMenu(context)
        menuView = BottomBarMenuView(context)
        val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        params.gravity = Gravity.CENTER
        menuView.layoutParams = params
        presenter = BottomBarPresenter(menuView)
        presenter.id = MENU_PRESENTER_ID
        menuBuilder.addMenuPresenter(presenter)
        presenter.initForMenu(context, menuBuilder)

        val tintArray = TintTypedArray.obtainStyledAttributes(context, attrs,
                R.styleable.BottomBarView, defStyleAttr,
                R.style.Widget_BottomBarView)

        if (tintArray != null) {
            if (tintArray.hasValue(R.styleable.BottomBarView_itemIconTint)) {
                menuView.itemIconTintList = tintArray.getColorStateList(R.styleable.BottomBarView_itemIconTint)
            } else {
                menuView.itemIconTintList = createDefaultColorStateList(android.R.attr.textColorSecondary)
            }
            if (tintArray.hasValue(R.styleable.BottomBarView_itemTextColor)) {
                menuView.itemTextColor = tintArray.getColorStateList(R.styleable.BottomBarView_itemTextColor)
            } else {
                menuView.itemTextColor = createDefaultColorStateList(android.R.attr.textColorSecondary)
            }
            if (tintArray.hasValue(R.styleable.BottomBarView_elevation)) {
                ViewCompat.setElevation(this, tintArray.getDimensionPixelSize(
                        R.styleable.BottomBarView_elevation, 0).toFloat())
            }

            val itemBackground = tintArray.getResourceId(R.styleable.BottomBarView_itemBackground, 0)
            menuView.itemBackgroundResource = itemBackground

            var mode = BottomAnimationMode.NONE
            if (tintArray.hasValue(R.styleable.BottomBarView_mode)) {
                val type = tintArray.getInt(R.styleable.BottomBarView_mode, -1)
                mode = BottomAnimationMode.getFor(type)
            }
            menuView.mode = mode

            if (tintArray.hasValue(R.styleable.BottomBarView_disableText)) {
                val disableText = tintArray.getBoolean(R.styleable.BottomBarView_disableText, false)
                menuView.disableText = disableText
            }
            if (tintArray.hasValue(R.styleable.BottomBarView_badgeItemId)) {
                val badgeIndex = tintArray.getResourceId(R.styleable.BottomBarView_badgeItemId, 0)
                if (tintArray.hasValue(R.styleable.BottomBarView_badgeLayout)) {
                    val badgeResource = tintArray.getResourceId(R.styleable.BottomBarView_badgeLayout, 0)
                    if (badgeResource != 0 && badgeIndex != 0) {
                        menuView.setBadge(badgeResource, badgeIndex)
                    }
                } else if (tintArray.hasValue(R.styleable.BottomBarView_badgeDrawable)) {
                    val badgeResource = tintArray.getResourceId(R.styleable.BottomBarView_badgeDrawable, 0)
                    if (badgeResource != 0 && badgeIndex != 0) {
                        menuView.setBadgeDrawable(badgeResource, badgeIndex)
                    }
                }

            }
            if (tintArray.hasValue(R.styleable.BottomBarView_menu)) {
                inflateMenu(tintArray.getResourceId(R.styleable.BottomBarView_menu, 0))
            }
            tintArray.recycle()
        }

        this.addView(menuView, params)

        if (Build.VERSION.SDK_INT < 21) {
            context?.apply { addCompatibilityTopDivider(this) }
        }

        menuBuilder.setCallback(BottomMenuBuilderCallback())
    }

    fun setOnItemReselect(func: (MenuItem) -> Unit) {
        reselectedListener = func
    }

    fun setOnItemSelect(func: (MenuItem) -> Boolean) {
        selectedListener = func
    }

    private fun inflateMenu(resourceId: Int) {
        presenter.updateSuspended = true
        menuInflater.inflate(resourceId, menuBuilder)
        presenter.updateSuspended = false
        presenter.updateMenuView(true)
    }

    private fun addCompatibilityTopDivider(context: Context) {
        val divider = View(context)
        divider.setBackgroundColor(
                ContextCompat.getColor(context, R.color.design_bottom_bar_shadow_color))
        val dividerParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                resources.getDimensionPixelSize(R.dimen.design_bottom_bar_shadow_height))
        divider.layoutParams = dividerParams
        addView(divider)
    }

    fun addBadge(@IdRes id: Int, @LayoutRes layout: Int) {
        menuView.addBadge(layout, id)
    }

    fun removeBadge(@IdRes menuItemId: Int) {
        menuView.removeBadges(menuItemId)
    }

    fun addBadgeDrawable(@IdRes id: Int, @DrawableRes drawableRes: Int) {
        menuView.addBadgeDrawable(drawableRes, id)
    }

    fun updateBadgeText(id: Int, updateText: CharSequence) {
        menuView.updateBadgeText(id, updateText)
    }


    private fun createDefaultColorStateList(textColorSecondary: Int): ColorStateList? {
        val value = TypedValue()
        if (context?.theme?.resolveAttribute(textColorSecondary, value, true) == true) {
            val baseColor = AppCompatResources.getColorStateList(context, value.resourceId)
            if (context?.theme?.resolveAttribute(android.support.v7.appcompat.R.attr.colorPrimary,
                            value, true) == true) {
                val colorPrimary = value.data
                val defaultColor = baseColor.defaultColor

                return ColorStateList(arrayOf(DISABLED_STATE_SET,
                        CHECKED_STATE_SET,
                        View.EMPTY_STATE_SET),
                        intArrayOf(baseColor.getColorForState(DISABLED_STATE_SET, defaultColor),
                                colorPrimary,
                                defaultColor
                        ))

            } else {
                return null
            }
        } else {
            return null
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState: Parcelable? = super.onSaveInstanceState()  // can be nullable state
        // ! should not delegate the state if null
        superState?.apply {
            val savedState = SavedState(this)
            savedState.menuPresenterState = Bundle()
            menuBuilder.savePresenterStates(savedState.menuPresenterState)
            return this
        }
        return superState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        super.onRestoreInstanceState(state.superState)
        menuBuilder.restorePresenterStates(state.menuPresenterState)
    }

    internal class SavedState : AbsSavedState {
        var menuPresenterState: Bundle? = null

        constructor(superState: Parcelable) : super(superState)

        constructor(source: Parcel, loader: ClassLoader?) : super(source, loader) {
            readFromParcel(source, loader)
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeBundle(menuPresenterState)
        }

        private fun readFromParcel(source: Parcel, loader: ClassLoader?) {
            menuPresenterState = source.readBundle(loader)
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.ClassLoaderCreator<SavedState> {
                override fun createFromParcel(source: Parcel, loader: ClassLoader): SavedState = SavedState(source, loader)
                override fun createFromParcel(source: Parcel): SavedState = SavedState(source, null)
                override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
            }
        }
    }

    protected open inner class BottomMenuBuilderCallback : MenuBuilder.Callback {

        override fun onMenuItemSelected(menu: MenuBuilder?, item: MenuItem?): Boolean {
            if (item == null) return false
            if (reselectedListener != null && item.itemId == selectedItemId) {
                reselectedListener?.invoke(item)
                return true
            }
            return selectedListener != null && selectedListener?.invoke(item) == false
        }

        override fun onMenuModeChange(menu: MenuBuilder?) {}
    }

    private fun Context.checkAppCompatTheme() {
        val appCompatCheck = intArrayOf(android.support.v7.appcompat.R.attr.colorPrimary)
        val array = obtainStyledAttributes(appCompatCheck)
        val failed = !array.hasValue(0)
        array.recycle()
        if (failed) {
            throw IllegalArgumentException("You need to use a Theme.AppCompat theme " +
                    "(or descendant) with the design library.")
        }
    }


    companion object {
        private const val MENU_PRESENTER_ID = 1
        private val CHECKED_STATE_SET = intArrayOf(android.R.attr.state_checked)
        private val DISABLED_STATE_SET = intArrayOf(-android.R.attr.state_enabled)
    }
}