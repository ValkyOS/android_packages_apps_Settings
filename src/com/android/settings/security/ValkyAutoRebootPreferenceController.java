/*
 * Copyright (C) 2026 The ValkyOS Project
 * SPDX-License-Identifier: Apache-2.0
 */
package com.android.settings.security;

import android.content.Context;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.core.TogglePreferenceController;

/** Toggle for the ValkyOS automatic security reboot (eight hours locked). */
public final class ValkyAutoRebootPreferenceController extends TogglePreferenceController {
    private static final String KEY = "valkyos_auto_reboot_enabled";

    public ValkyAutoRebootPreferenceController(Context context) {
        super(context, KEY);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public boolean isChecked() {
        return Settings.Global.getInt(mContext.getContentResolver(), KEY, 1) != 0;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        return Settings.Global.putInt(mContext.getContentResolver(), KEY, isChecked ? 1 : 0);
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return R.string.menu_key_security;
    }
}
