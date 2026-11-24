package com.android.settings.notification;

import android.content.Context;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.core.TogglePreferenceController;

public final class ValkyOsCornerSpreadPreferenceController extends TogglePreferenceController {
    private static final String KEY_DUAL = "valkyos_qs_corner_spread_dual";

    private final String mSettingKey;

    public ValkyOsCornerSpreadPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
        mSettingKey = KEY_DUAL.equals(preferenceKey)
                ? Settings.Secure.VALKYOS_QS_CORNER_SPREAD_DUAL
                : Settings.Secure.VALKYOS_QS_CORNER_SPREAD;
    }

    @Override
    public boolean isChecked() {
        return Settings.Secure.getInt(mContext.getContentResolver(), mSettingKey, 0) != 0;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        return Settings.Secure.putInt(
                mContext.getContentResolver(), mSettingKey, isChecked ? 1 : 0);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return R.string.menu_key_notifications;
    }
}
