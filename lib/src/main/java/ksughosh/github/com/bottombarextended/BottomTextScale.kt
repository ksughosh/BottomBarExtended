package ksughosh.github.com.bottombarextended

import android.animation.Animator
import android.animation.ValueAnimator
import android.support.annotation.RequiresApi
import android.support.transition.Transition
import android.support.transition.TransitionValues
import android.view.ViewGroup
import android.widget.TextView

/**
 * Created by s.kumar on 29.03.18.
 * Copyright © 2017 LOOP. All rights reserved.
 */
@RequiresApi(14)
class BottomTextScale : Transition() {

    override fun captureStartValues(transitionValues: TransitionValues) {
        captureValues(transitionValues)
    }

    override fun captureEndValues(transitionValues: TransitionValues) {
        captureValues(transitionValues)
    }

    private fun captureValues(transitionValues: TransitionValues) {
        if (transitionValues.view is TextView) {
            val textview = transitionValues.view as TextView
            transitionValues.values[PROPNAME_SCALE] = textview.scaleX
        }
    }

    override fun createAnimator(sceneRoot: ViewGroup, startValues: TransitionValues?,
                                endValues: TransitionValues?): Animator? {
        if (startValues == null || endValues == null || startValues.view !is TextView
                || endValues.view !is TextView) {
            return null
        }
        val view = endValues.view as? TextView
        val startVals = startValues.values
        val endVals = endValues.values
        val startSize = if (startVals[PROPNAME_SCALE] != null)
            startVals[PROPNAME_SCALE] as Float
        else
            1f
        val endSize = if (endVals[PROPNAME_SCALE] != null)
            endVals[PROPNAME_SCALE] as Float
        else
            1f
        if (startSize == endSize) {
            return null
        }

        val animator = ValueAnimator.ofFloat(startSize, endSize)

        animator.addUpdateListener { valueAnimator ->
            val animatedValue = valueAnimator.animatedValue as Float
            view?.scaleX = animatedValue
            view?.scaleY = animatedValue
        }
        return animator
    }

    companion object {
        private const val PROPNAME_SCALE = "android:textscale:scale"
    }
}
