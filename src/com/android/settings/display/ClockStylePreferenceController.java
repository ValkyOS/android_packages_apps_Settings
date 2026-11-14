/*
 * Copyright (C) 2026 The ValkyOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.display;

import android.content.Context;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.display.clock.ClockUtils;

/**
 * Lock screen clock style entry. Opens the clock picker and shows the current
 * style (0 = Android clock). SystemUI ClockStyle reads the setting.
 */
public class ClockStylePreferenceController extends BasePreferenceController {

    public ClockStylePreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public CharSequence getSummary() {
        final String[] names = mContext.getResources()
                .getStringArray(R.array.valkyos_clock_style_entries);
        final int value = Settings.Secure.getIntForUser(mContext.getContentResolver(),
                ClockUtils.SETTING_CLOCK_STYLE, 0, UserHandle.USER_CURRENT);
        return (value >= 0 && value < names.length) ? names[value] : names[0];
    }
}
