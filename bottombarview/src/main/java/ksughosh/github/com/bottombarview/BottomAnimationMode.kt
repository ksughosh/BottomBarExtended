package ksughosh.github.com.bottombarview

/**
 * Created by s.kumar on 29.03.18.
 * Copyright © 2017 LOOP. All rights reserved.
 */
enum class BottomAnimationMode(val value: Int) {
    SHIFTING(0),
    SCALE(1),
    NONE(2);


    companion object {
        fun getFor(value: Int): BottomAnimationMode {
            values().forEach {
                if (it.value == value) {
                    return it
                }
            }
            return SCALE
        }
    }
}