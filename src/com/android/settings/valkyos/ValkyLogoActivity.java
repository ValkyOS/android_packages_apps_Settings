/*
 * Copyright (C) 2026 The ValkyOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.valkyos;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.android.settings.R;

/**
 * ValkyOS easter egg, in memory of Valky: tap the V, it turns into wings that
 * fly up to the sky, a star appears and becomes a paw print. Opened by tapping
 * the ValkyOS version three times in About phone.
 */
public class ValkyLogoActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window w = getWindow();
        w.setDecorFitsSystemWindows(false);
        w.getAttributes().layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;
        ValkySkyView view = new ValkySkyView(this);
        view.setContentDescription(getString(R.string.valkyos_easter_egg_hint));
        setContentView(view);
    }
}
