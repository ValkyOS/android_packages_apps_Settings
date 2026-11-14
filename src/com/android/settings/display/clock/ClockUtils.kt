/*
 * SPDX-FileCopyrightText: 2026 kenway214
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.display.clock

import com.android.settings.R

object ClockUtils {

    // Keep in sync with Settings.Secure.LOCK_SCREEN_CUSTOM_CLOCK_STYLE.
    const val SETTING_CLOCK_STYLE = "lock_screen_custom_clock_style"


    val CLOCK_LAYOUTS = intArrayOf(
        R.layout.keyguard_clock_default,
        R.layout.keyguard_clock_oos, // 1
        R.layout.keyguard_clock_oos2, // 2
        R.layout.keyguard_clock_center, // 3
        R.layout.keyguard_clock_simple, // 4
        R.layout.keyguard_clock_miui, // 5
        R.layout.keyguard_clock_ide, // 6
        R.layout.keyguard_clock_moto, // 7
        R.layout.keyguard_clock_stylish, // 8
        R.layout.keyguard_clock_stylish2, // 9
        R.layout.keyguard_clock_stylish3, // 10
        R.layout.keyguard_clock_stylish4, // 11
        R.layout.keyguard_clock_stylish5, // 12
        R.layout.keyguard_clock_stylish6, // 13
        R.layout.keyguard_clock_stylish7, // 14
        R.layout.keyguard_clock_stylish8, // 15
        R.layout.keyguard_clock_stylish9, // 16
        R.layout.keyguard_clock_stylish10, // 17
        R.layout.keyguard_clock_word, // 18
        R.layout.keyguard_clock_life, // 19
        R.layout.keyguard_clock_a9, // 20
        R.layout.keyguard_clock_nos1, // 21
        R.layout.keyguard_clock_nos2, // 22
        R.layout.keyguard_clock_num, // 23
        R.layout.keyguard_clock_accent, // 24
        R.layout.keyguard_clock_analog, // 25
        R.layout.keyguard_clock_block, // 26
        R.layout.keyguard_clock_bubble, // 27
        R.layout.keyguard_clock_label, // 28
        R.layout.keyguard_clock_ios, // 29
        R.layout.keyguard_clock_taden, // 30
        R.layout.keyguard_clock_mont, // 31
        R.layout.keyguard_clock_encode, // 32
        R.layout.keyguard_clock_nos3, // 33
        R.layout.keyguard_anci_clock_outline, // 34
        R.layout.keyguard_anci_clock_ovalium, // 35
        R.layout.keyguard_anci_clock_rectangle, // 36
        R.layout.keyguard_anci_clock_wallet, // 37
        R.layout.keyguard_anci_clockdate_clavicula, // 38
        R.layout.keyguard_anci_clockdate_kln, // 39
        R.layout.keyguard_anci_clockdate_miring, // 40
        R.layout.keyguard_anci_clockdate_scapula, // 41
        R.layout.keyguard_anci_clockdate_sternum, // 42
        R.layout.keyguard_sparkCircle, // 43
        R.layout.keyguard_sparkList, // 44
        R.layout.keyguard_clock_miui2, // 45
        R.layout.keyguard_clock_ios2, // 46
        R.layout.keyguard_clock_ios3, // 47
        R.layout.keyguard_clock_ios4, // 48
        R.layout.keyguard_clock_ios5, // 49
        R.layout.keyguard_clock_ios6, // 50
        R.layout.keyguard_clock_ios7, // 51
        R.layout.keyguard_clock_ios8, // 52
        R.layout.keyguard_clock_ios9, // 53
        R.layout.keyguard_clock_ios10, // 54
        R.layout.keyguard_clock_ios11, // 55
        R.layout.keyguard_clock_ios12, // 56
        R.layout.keyguard_clock_ios13, // 57
        R.layout.keyguard_clock_ios14, // 58
        R.layout.keyguard_clock_big1, // 59
        R.layout.keyguard_clock_big2, // 60
        R.layout.keyguard_clock_big3, // 61
        R.layout.keyguard_clock_big4, // 62
        R.layout.keyguard_clock_sweet, // 63
        R.layout.keyguard_clock_pixel, // 64
        R.layout.keyguard_clock_samurai, // 65
        R.layout.keyguard_clock_gateway, // 66
        R.layout.keyguard_clock_tall, // 67
    )
}
