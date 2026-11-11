/*
 * Copyright (C) 2026 The ValkyOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.valkyos;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.android.settings.R;

/**
 * Asks for a donation to help pay the build servers and test phones.
 * Opened from About phone and from the donation reminder notification.
 */
public class DonateActivity extends Activity {

    private static final String TAG = "ValkyOSDonate";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DonateReceiver.cancelNotification(this);
        DonateReceiver.markSeen(this);

        boolean night = (getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        int theme = night ? android.R.style.Theme_DeviceDefault_Dialog_Alert
                : android.R.style.Theme_DeviceDefault_Light_Dialog_Alert;

        new AlertDialog.Builder(this, theme)
                .setTitle(R.string.valkyos_donate_title)
                .setMessage(R.string.valkyos_donate_message)
                .setPositiveButton(R.string.valkyos_donate_button, (d, w) -> openDonatePage())
                .setNegativeButton(R.string.valkyos_donate_later, (d, w) -> finish())
                .setOnCancelListener(d -> finish())
                .show();
    }

    private void openDonatePage() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(getString(R.string.valkyos_donate_url)))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (ActivityNotFoundException e) {
            Log.w(TAG, "No browser to open the donation page", e);
        }
        finish();
    }
}
