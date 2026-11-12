/*
 * Copyright (C) 2026 The ValkyOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.location;

import android.content.Context;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.core.BasePreferenceController;

/**
 * Which SUPL server assisted GPS uses: the GrapheneOS proxy (default) or the
 * standard server. Read by GnssConfiguration in system_server.
 */
public class SuplServerPreferenceController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    // Keep in sync with com.android.server.location.gnss.GnssConfiguration.
    static final String SETTING_SUPL_SERVER = "valkyos_supl_server";
    static final int SUPL_SERVER_GRAPHENEOS_PROXY = 0;

    public SuplServerPreferenceController(Context context, String key) {
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
        int value = Settings.Global.getInt(mContext.getContentResolver(),
                SETTING_SUPL_SERVER, SUPL_SERVER_GRAPHENEOS_PROXY);
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
        Settings.Global.putInt(mContext.getContentResolver(), SETTING_SUPL_SERVER, value);
        updateState(preference);
        return true;
    }
}
