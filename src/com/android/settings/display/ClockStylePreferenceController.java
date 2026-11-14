/*
 * Copyright (C) 2026 The ValkyOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.display;

import android.content.Context;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.core.BasePreferenceController;

/**
 * Lock screen clock style (0 = Android clock). Read by SystemUI ClockStyle,
 * ported from crDroid.
 */
public class ClockStylePreferenceController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    // Keep in sync with Settings.Secure.LOCK_SCREEN_CUSTOM_CLOCK_STYLE.
    static final String SETTING_CLOCK_STYLE = "lock_screen_custom_clock_style";

    public ClockStylePreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        Preference pref = screen.findPreference(getPreferenceKey());
        if (pref != null) {
            pref.setOnPreferenceChangeListener(this);
        }
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (!(preference instanceof ListPreference)) return;
        ListPreference list = (ListPreference) preference;
        int value = Settings.Secure.getIntForUser(mContext.getContentResolver(),
                SETTING_CLOCK_STYLE, 0, UserHandle.USER_CURRENT);
        list.setValue(Integer.toString(value));
        list.setSummary(list.getEntry());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final int value;
        try {
            value = Integer.parseInt((String) newValue);
        } catch (NumberFormatException e) {
            return false;
        }
        Settings.Secure.putIntForUser(mContext.getContentResolver(), SETTING_CLOCK_STYLE,
                value, UserHandle.USER_CURRENT);
        updateState(preference);
        return true;
    }
}
