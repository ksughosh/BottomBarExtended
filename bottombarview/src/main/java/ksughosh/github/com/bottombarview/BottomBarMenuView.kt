package ksughosh.github.com.bottombarview

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.support.annotation.DrawableRes
import android.support.annotation.LayoutRes
import android.support.transition.AutoTransition
import android.support.transition.TransitionManager
import android.support.transition.TransitionSet
import android.support.v4.content.ContextCompat
import android.support.v4.util.Pools
import android.support.v4.view.ViewCompat
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v7.view.menu.MenuBuilder
import android.support.v7.view.menu.MenuItemImpl
import android.support.v7.view.menu.MenuView
import android.util.AttributeSet
import android.util.SparseArray
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import kotlin.math.min

/**
 * Created by s.kumar on 29.03.18.
 * Copyright © 2017 LOOP. All rights reserved.
 */
@Suppress("MemberVisibilityCanBePrivate")
open class BottomBarMenuView : ViewGroup, MenuView {
    var presenter: BottomBarPresenter? = null

    protected var inactiveItemMaxWidth: Int = 0
    protected var inactiveItemMinWidth: Int = 0
    protected var activeItemMaxWidth: Int = 0
    protected var itemHeight: Int = 0
    protected val transitionSet: TransitionSet
    protected val tempChildWidths: IntArray

    @SuppressLint("UseSparseArrays")
    protected val mapOfBadges = SparseArray<Int?>(BottomBarMenu.MAX_ITEM_COUNT)
    @SuppressLint("UseSparseArrays")
    protected val mapOfBadgeDrawables = SparseArray<Int?>(BottomBarMenu.MAX_ITEM_COUNT)

    var selectedItemPosition = 0
    var selectedItemId = 0
    var disableText = false

    protected var menu: MenuBuilder? = null
    protected var buttons: Array<BottomBarItemView?>? = null
    protected var onClickDelegate: OnClickListener? = null

    protected val itemPool = Pools.SynchronizedPool<BottomBarItemView>(5)

    var itemBackgroundResource: Int = 0
        set(value) {
            if (value != 0) {
                field = value
                buttons?.apply {
                    forEach {
                        it?.setItemBackground(value)
                    }
                }
            }
        }

    var itemIconTintList: ColorStateList? = null
        set(value) {
            field = value
            value?.apply {
                if (buttons == null) return
                buttons?.forEach {
                    it?.iconTintList = this
                }
            }
        }

    var itemTextColor: ColorStateList? = null
        set(value) {
            field = value
            value?.apply {
                if (buttons == null) return
                buttons?.forEach {
                    it?.setTextColor(this)
                }
            }
        }


    var mode: BottomAnimationMode = BottomAnimationMode.SHIFTING

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        transitionSet = AutoTransition()
        tempChildWidths = IntArray(BottomBarMenu.MAX_ITEM_COUNT)
        resources?.apply {
            try {
                inactiveItemMaxWidth = getDimensionPixelSize(R.dimen.design_bottom_bar_item_max_width)
                inactiveItemMinWidth = getDimensionPixelSize(R.dimen.design_bottom_bar_item_min_width)
                activeItemMaxWidth = getDimensionPixelSize(R.dimen.design_bottom_bar_active_item_max_width)
                itemHeight = getDimensionPixelSize(R.dimen.design_bottom_bar_height)

                transitionSet.setOrdering(TransitionSet.ORDERING_TOGETHER)
                transitionSet.setDuration(ACTIVE_ANIMATION_DURATION_MS)
                transitionSet.setInterpolator(FastOutSlowInInterpolator())
                transitionSet.addTransition(BottomTextScale())

                onClickDelegate = OnClickListener {
                    val itemView = it as? BottomBarItemView
                    itemView?.itemData?.apply {
                        @SuppressLint("RestrictedApi")
                        if (menu?.performItemAction(this, presenter, 0) == false) {
                            this.isChecked = true
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val heightSpec = MeasureSpec.makeMeasureSpec(itemHeight, MeasureSpec.EXACTLY)
        val count = childCount
        when (mode) {
            BottomAnimationMode.SHIFTING -> {
                val inactiveCount = count - 1
                val activeMaxAvailable = width - inactiveCount * inactiveItemMinWidth
                val activeWidth = min(activeMaxAvailable, activeItemMaxWidth)
                val inactiveMaxAvailable = (width - activeWidth) / inactiveCount
                val inactiveWidth = min(inactiveMaxAvailable, inactiveItemMaxWidth)
                var extra = width - activeWidth - inactiveWidth * inactiveCount
                for (i in 0 until count) {
                    tempChildWidths[i] = if (i == selectedItemPosition) activeWidth else inactiveWidth
                    if (extra > 0) {
                        tempChildWidths[i]++
                        extra--
                    }
                }
            }
            BottomAnimationMode.SCALE, BottomAnimationMode.NONE -> {
                val maxAvailable = width / if (count == 0) 1 else count
                val childWidth = min(maxAvailable, activeItemMaxWidth)
                var extra = width - childWidth * count
                for (i in 0 until count) {
                    tempChildWidths[i] = childWidth
                    if (extra > 0) {
                        tempChildWidths[i]++
                        extra--
                    }
                }
            }
        }

        var totalWidth = 0
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.isGone) {
                continue
            }
            child.measure(MeasureSpec.makeMeasureSpec(tempChildWidths[i], MeasureSpec.EXACTLY), heightSpec)
            val params = child.layoutParams
            params.width = child.measuredWidth
            totalWidth += child.measuredWidth
        }
        setMeasuredDimension(
                View.resolveSizeAndState(totalWidth,
                        View.MeasureSpec.makeMeasureSpec(totalWidth, View.MeasureSpec.EXACTLY), 0),
                View.resolveSizeAndState(itemHeight, heightSpec, 0))
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val width = r - l
        val height = b - t
        var used = 0
        for (i in 0 until childCount) {
            val view = getChildAt(i)
            if (view.isGone)
                continue

            if (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL) {
                view.layout(width - used - view.measuredWidth, 0, width - used, height)
            } else {
                view.layout(used, 0, view.measuredWidth + used, height)
            }
            used += view.measuredWidth
        }
    }

    fun setBadge(@LayoutRes resource: Int, itemId: Int) {
        mapOfBadges.put(itemId, resource)
    }

    override fun getWindowAnimations(): Int = 0

    override fun initialize(menu: MenuBuilder?) {
        this.menu = menu
    }

    @SuppressLint("RestrictedApi")
    fun buildMenuView() {
        removeAllViews()
        buttons?.forEach {
            it?.apply { itemPool.release(this) }
        }
        val menu = this.menu ?: return

        if (menu.size() > BottomBarMenu.MAX_ITEM_COUNT) throw MenuMalFormedException()

        if (menu.size() == 0) {
            selectedItemId = 0
            selectedItemPosition = 0
            buttons = null
            return
        }
        mode = if (mode == BottomAnimationMode.SHIFTING && menu.size() <= 3) BottomAnimationMode.SCALE else mode
        buttons = arrayOfNulls(menu.size())
        (0 until menu.size()).forEach { i ->
            presenter?.updateSuspended = true
            menu.getItem(i)?.isCheckable = true
            presenter?.updateSuspended = false
            val child = getNewItem()
            buttons?.set(i, child)
            itemIconTintList?.apply { child.iconTintList = this }
            itemTextColor?.apply { child.setTextColor(this) }
            child.mode = mode
            if (disableText) {
                child.disableText()
            }
            val impl = menu.getItem(i) as? MenuItemImpl
            impl?.apply { child.initialize(this, 0) }
            child.itemPosition = (i)
            child.setOnClickListener(onClickDelegate)
            val resource = mapOfBadges.get(child.id, null)
                    ?: mapOfBadgeDrawables.get(child.id, null)
            resource?.apply {
                val drawable = try {
                    context?.getDrawableFromResource(this)
                } catch (e: Exception) {
                    null
                }
                if (drawable != null) {
                    child.setBadgeDrawable(drawable)
                } else {
                    child.setBadge(this)
                }
            }
            addView(child)
        }
        selectedItemPosition = min(menu.size() - 1, selectedItemPosition)
        menu.getItem(selectedItemPosition).isChecked = true
    }

    @SuppressLint("RestrictedApi")
    fun updateMenu() {
        val menuSize = menu?.size() ?: return
        if (menuSize != buttons?.size) {
            buildMenuView()
            return
        }
        val previousItemSelectedId = selectedItemId
        (0 until menuSize).forEach { i ->
            val item = menu?.getItem(i)
            if (item?.isChecked == true) {
                selectedItemId = item.itemId
                selectedItemPosition = i
            }
        }

        if (previousItemSelectedId != selectedItemId) {
            TransitionManager.beginDelayedTransition(this, transitionSet)
        }

        (0 until menuSize).forEach { i ->
            presenter?.updateSuspended = true
            buttons?.get(i)?.initialize(menu?.getItem(i) as? MenuItemImpl, 0)
            presenter?.updateSuspended = false
        }
    }

    private fun getNewItem(): BottomBarItemView {
        var item = itemPool.acquire()
        if (item == null) {
            item = BottomBarItemView(context)
        }
        return item
    }

    @SuppressLint("RestrictedApi")
    fun tryRestoreSelectedItemId(itemId: Int) {
        val menu = this.menu ?: return
        val size = menu.size()
        for (i in 0 until size) {
            val item = menu.getItem(i)
            if (itemId == item.itemId) {
                selectedItemId = itemId
                selectedItemPosition = i
                item.isChecked = true
                break
            }
        }
    }

    fun setBadgeDrawable(badgeResource: Int, badgeIndex: Int) {
        mapOfBadgeDrawables.put(badgeIndex, badgeResource)
    }

    fun removeBadges(badgeIndex: Int) {
        mapOfBadges.remove(badgeIndex)
        mapOfBadgeDrawables.remove(badgeIndex)
        removeAndInvalidate(badgeIndex)
    }

    private fun removeAndInvalidate(id: Int) {
        children().firstOrNull { it.id == id }?.removeBadge()
    }

    fun updateBadgeText(id: Int, updatedText: CharSequence) {
        children().firstOrNull { it.id == id }?.updateBadgeText(updatedText)
    }

    fun addBadge(layout: Int, id: Int) {
        children().firstOrNull { it.id == id }?.setBadge(layout)
        mapOfBadges.put(id, layout)
    }

    fun addBadgeDrawable(@DrawableRes drawableRes: Int, id: Int) {
        context?.getDrawableFromResource(drawableRes)?.apply {
            children().firstOrNull { it.id == id }?.setBadgeDrawable(this)
        }
    }

    private val View.isGone
        get() = this.visibility == View.GONE


    private fun Context.getDrawableFromResource(@DrawableRes resource: Int) =
            ContextCompat.getDrawable(this, resource)

    private fun children(): List<BottomBarItemView> {
        val children = mutableListOf<BottomBarItemView>()
        for (i in 0 until childCount) {
            (getChildAt(i) as? BottomBarItemView)?.apply { children.add(this) }
        }
        return children
    }

    companion object {
        private const val ACTIVE_ANIMATION_DURATION_MS = 115L
    }
}