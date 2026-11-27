package com.android.settings.privacy;

import android.content.Context;
import android.provider.DeviceConfig;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

public final class ClipboardAutoClearPreferenceController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    public static final String KEY = "clipboard_auto_clear_timeout";

    private static final String PROPERTY_AUTO_CLEAR_ENABLED = "auto_clear_enabled";
    private static final String PROPERTY_AUTO_CLEAR_TIMEOUT = "auto_clear_timeout";
    private static final long DEFAULT_TIMEOUT_MILLIS = 3_600_000L;

    private ListPreference mPreference;

    public ClipboardAutoClearPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreference = screen.findPreference(getPreferenceKey());
    }

    @Override
    public void updateState(Preference preference) {
        if (mPreference == null) {
            return;
        }
        boolean enabled = DeviceConfig.getBoolean(DeviceConfig.NAMESPACE_CLIPBOARD,
                PROPERTY_AUTO_CLEAR_ENABLED, true);
        long timeout = enabled
                ? DeviceConfig.getLong(DeviceConfig.NAMESPACE_CLIPBOARD,
                        PROPERTY_AUTO_CLEAR_TIMEOUT, DEFAULT_TIMEOUT_MILLIS)
                : 0L;
        String value = String.valueOf(timeout);
        if (mPreference.findIndexOfValue(value) < 0) {
            value = String.valueOf(DEFAULT_TIMEOUT_MILLIS);
        }
        mPreference.setValue(value);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        long timeout = Long.parseLong((String) newValue);
        if (timeout == 0L) {
            return DeviceConfig.setProperty(DeviceConfig.NAMESPACE_CLIPBOARD,
                    PROPERTY_AUTO_CLEAR_ENABLED, "false", false);
        }
        boolean timeoutUpdated = DeviceConfig.setProperty(DeviceConfig.NAMESPACE_CLIPBOARD,
                PROPERTY_AUTO_CLEAR_TIMEOUT, String.valueOf(timeout), false);
        boolean enabledUpdated = DeviceConfig.setProperty(DeviceConfig.NAMESPACE_CLIPBOARD,
                PROPERTY_AUTO_CLEAR_ENABLED, "true", false);
        return timeoutUpdated && enabledUpdated;
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return R.string.menu_key_privacy;
    }
}
