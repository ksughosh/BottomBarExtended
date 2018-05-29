package ksughosh.github.com.bottombarextended

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.support.annotation.DrawableRes
import android.support.annotation.LayoutRes
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v4.view.PointerIconCompat
import android.support.v4.view.ViewCompat
import android.support.v7.view.menu.MenuItemImpl
import android.support.v7.widget.TooltipCompat
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.design_bottom_bar_item.view.*

@Suppress("MemberVisibilityCanBePrivate")
@SuppressLint("RestrictedApi")
open class BottomBarItemView : FrameLayout, BottomItemView {
    var mode: BottomAnimationMode = BottomAnimationMode.SHIFTING

    private var itemData: MenuItemImpl? = null

    protected var defaultMargin: Int = 0
    protected var shiftAmount: Int = 0
    protected var scaleUpFactor: Float = 0f
    protected var scaleDownFactor: Float = 0f
    protected open var disableText = false
    var itemPosition = INVALID_ITEM_POSITION
    var iconTintList: ColorStateList? = null
        set(value) {
            field = value
            if (itemData != null) {
                setIcon(itemData?.icon)
            }
        }

    protected open val badgeText: TextView? by lazy {
        var badgeText: TextView? = null
        badge?.forEachChild {
            if (it is TextView) {
                badgeText = it
                return@forEachChild
            } else if (it is ViewGroup && it.childCount > 0) {
                it.forEachChild innerLoop@{
                    if (it is TextView) {
                        badgeText = it
                        return@innerLoop
                    }
                }
                if (badgeText != null) return@forEachChild
            }
        }
        badgeText
    }

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val resource = resources ?: return
        val inactiveLabelSize = resource.getDimensionPixelSize(R.dimen.design_bottom_bar_text_size)
        val activeLabelSize = resource.getDimensionPixelSize(R.dimen.design_bottom_bar_active_text_size)
        defaultMargin = resource.getDimensionPixelOffset(R.dimen.design_bottom_bar_margin)
        shiftAmount = inactiveLabelSize - activeLabelSize
        scaleUpFactor = 1f * activeLabelSize / inactiveLabelSize
        scaleDownFactor = 1f * inactiveLabelSize / activeLabelSize

        inflate(R.layout.design_bottom_bar_item, true)
        setBackgroundResource(R.drawable.design_bottom_bar_item_background)
    }


    @SuppressLint("RestrictedApi")
    override fun initialize(itemData: MenuItemImpl?, menuType: Int) {
        this.itemData = itemData ?: return
        setCheckable(itemData.isCheckable)
        setChecked(itemData.isChecked)
        isEnabled = itemData.isEnabled
        setIcon(itemData.icon)
        setTitle(itemData.title)
        id = itemData.itemId
        contentDescription = itemData.contentDescription
        TooltipCompat.setTooltipText(this, itemData.tooltipText)
    }

    override fun getItemData(): MenuItemImpl? = itemData

    override fun setCheckable(checkable: Boolean) {
        refreshDrawableState()
    }

    override fun setTitle(title: CharSequence?) {
        smallLabel?.text = title
        largeLabel?.text = title
    }

    override fun setChecked(checked: Boolean) {
        largeLabel?.apply {
            pivotX = (width / 2).toFloat()
            pivotY = (height / 2).toFloat()
        }
        smallLabel?.apply {
            pivotX = (width / 2).toFloat()
            pivotY = (height / 2).toFloat()
        }
        when (mode) {
            BottomAnimationMode.SHIFTING -> {
                if (checked) {
                    val params = icon?.layoutParams as? LayoutParams
                    params?.apply {
                        this.gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
                        topMargin = defaultMargin
                        icon?.layoutParams = this
                    }
                    if (disableText) {
                        largeLabel?.makeGone()
                        smallLabel?.makeGone()
                    } else {
                        largeLabel?.makeVisible()
                        largeLabel?.scaleX = 1f
                        largeLabel?.scaleY = 1f
                    }
                } else {
                    val params = icon?.layoutParams as? LayoutParams
                    params?.apply {
                        this.gravity = Gravity.CENTER
                        topMargin = defaultMargin
                        icon?.layoutParams = this
                    }
                    if (disableText) {
                        largeLabel?.makeGone()
                        smallLabel?.makeGone()
                    } else {
                        largeLabel?.hide()
                        largeLabel?.scaleX = 0.5f
                        largeLabel?.scaleY = 0.5f
                    }
                }
                if (!disableText) {
                    smallLabel?.hide()
                }
            }
            BottomAnimationMode.SCALE -> {
                if (checked) {
                    val params = icon?.layoutParams as? LayoutParams
                    params?.apply {
                        this.gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
                        topMargin = defaultMargin + shiftAmount
                        icon?.layoutParams = this
                    }
                    if (disableText) {
                        largeLabel?.makeGone()
                        smallLabel?.makeGone()
                    } else {
                        largeLabel?.makeVisible()
                        smallLabel?.hide()

                        largeLabel?.scaleX = 1f
                        largeLabel?.scaleY = 1f
                        smallLabel?.scaleX = scaleUpFactor
                        smallLabel?.scaleY = scaleUpFactor
                    }
                } else {

                    val params = icon?.layoutParams as? LayoutParams
                    params?.apply {
                        this.gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
                        topMargin = defaultMargin
                        icon?.layoutParams = this
                    }

                    if (disableText) {
                        largeLabel?.makeGone()
                        smallLabel?.makeGone()
                    } else {
                        largeLabel?.hide()
                        smallLabel?.makeVisible()

                        largeLabel?.scaleX = scaleDownFactor
                        largeLabel?.scaleY = scaleDownFactor
                        smallLabel?.scaleX = 1f
                        smallLabel?.scaleY = 1f
                    }
                }
            }

            BottomAnimationMode.NONE -> {
                val params = icon?.layoutParams as? LayoutParams
                params?.apply {
                    this.gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
                    topMargin = defaultMargin
                    icon?.layoutParams = this
                }
                if (disableText) {
                    largeLabel?.makeGone()
                    smallLabel?.makeGone()
                } else {
                    largeLabel?.makeGone()
                    if (checked) {
                        smallLabel?.makeVisible()
                        largeLabel?.scaleX = 1f
                        largeLabel?.scaleY = 1f
                    } else {
                        smallLabel?.makeVisible()
                        smallLabel?.scaleX = 1f
                        smallLabel?.scaleY = 1f
                    }
                }
            }
        }
    }

    fun disableText() {
        disableText = true
        largeLabel?.makeGone()
        smallLabel?.makeGone()
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        smallLabel?.isEnabled = enabled
        largeLabel?.isEnabled = enabled
        icon?.isEnabled = enabled
        badge?.isEnabled = enabled
        if (enabled) {
            ViewCompat.setPointerIcon(this, PointerIconCompat.getSystemIcon(context,
                    PointerIconCompat.TYPE_HAND))
        } else {
            ViewCompat.setPointerIcon(this, null)
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + 1)
        if (itemData != null && itemData?.isCheckable == true && itemData?.isChecked == true) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET)
        }
        return drawableState
    }

    override fun setIcon(ic: Drawable?) {
        var icons = ic
        if (icons != null) {
            val state = icons.constantState
            icons = DrawableCompat.wrap(if (state == null) icons else state.newDrawable()).mutate()
            DrawableCompat.setTintList(icons, iconTintList)
        }
        icon?.setImageDrawable(icons)
    }

    override fun showsIcon(): Boolean = true

    override fun prefersCondensedTitle(): Boolean = false

    override fun setShortcut(showShortcut: Boolean, shortcutKey: Char) {}


    fun setItemBackground(background: Int) {
        val backgroundDrawable = if (background == 0) null
        else context?.getDrawableFromResource(background)
        ViewCompat.setBackground(this, backgroundDrawable)
    }

    fun setTextColor(colorStateList: ColorStateList) {
        largeLabel?.setTextColor(colorStateList)
        smallLabel?.setTextColor(colorStateList)
    }

    override fun setBadge(@LayoutRes resource: Int) {
        badge?.makeVisible()
        badge?.inflate(resource, true)

    }

    override fun removeBadge() {
        badge?.makeGone()
    }

    fun updateBadgeText(updatedText: CharSequence) {
        badge?.makeVisible()
        badgeText?.text = updatedText
    }

    override fun getBadgeLayout(): FrameLayout? = badge

    private fun ViewGroup.inflate(@LayoutRes resource: Int, attachToRoot: Boolean): View? =
            context?.let { LayoutInflater.from(it).inflate(resource, this, attachToRoot) }

    private fun ViewGroup.forEachChild(func: (View) -> Unit) {
        for (i in 0 until childCount) {
            func(getChildAt(i))
        }
    }

    private fun View.makeVisible() {
        if (visibility != VISIBLE) {
            visibility = VISIBLE
        }
    }

    private fun View.hide() {
        if (visibility != INVISIBLE) {
            visibility = INVISIBLE
        }
    }

    private fun View.makeGone() {
        if (visibility != GONE) {
            visibility = GONE
        }
    }

    private fun Context.getDrawableFromResource(@DrawableRes resource: Int) =
            ContextCompat.getDrawable(this, resource)

    fun setBadgeDrawable(drawableFromResource: Drawable) {
        badge?.makeVisible()
        val simpleBadge = FrameLayout(context)
        val size = context?.resources?.getDimensionPixelSize(R.dimen.design_bottom_bar_badge_min_size)
        size?.apply { simpleBadge.layoutParams = FrameLayout.LayoutParams(this, this) }
        simpleBadge.background = drawableFromResource
        badge?.addView(simpleBadge)
    }

    companion object {
        const val INVALID_ITEM_POSITION = -1
        val CHECKED_STATE_SET = intArrayOf(android.R.attr.state_checked)
    }
}
