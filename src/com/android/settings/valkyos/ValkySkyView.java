/*
 * Copyright (C) 2026 The ValkyOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.valkyos;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;

import com.android.settings.R;

import java.util.ArrayList;
import java.util.Random;

/** Draws the "Valky in the sky" animation. All drawing is in pixels of this view. */
public class ValkySkyView extends View {

    private static final float T_OPEN = 1.2f, T_RISE = 4.6f, T_STAR = 5.8f, T_PAW = 7.2f;

    private static final int STATE_IDLE = 0, STATE_PLAYING = 1, STATE_DONE = 2;

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mGlow = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path mPath = new Path();
    private final float[][] mStars = new float[70][4];
    private final ArrayList<float[]> mParticles = new ArrayList<>(); // x, y, vx, vy, life, r
    private final Random mRandom = new Random();
    private final boolean mReduceMotion;
    private final String mHint;
    private final String mName;

    private int mState = STATE_IDLE;
    private long mStart;
    private long mJumpStart = -100000;
    private long mLast = SystemClock.uptimeMillis();

    public ValkySkyView(Context context) {
        super(context);
        Random seeded = new Random(7);
        for (float[] s : mStars) {
            s[0] = seeded.nextFloat();
            s[1] = seeded.nextFloat() * 0.8f;
            s[2] = 0.4f + seeded.nextFloat() * 1.2f;
            s[3] = seeded.nextFloat() * 6.28f;
        }
        mReduceMotion = Settings.Global.getFloat(context.getContentResolver(),
                Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f;
        mHint = context.getString(R.string.valkyos_easter_egg_hint);
        mName = context.getString(R.string.valkyos_easter_egg_name);
        mText.setTextAlign(Paint.Align.CENTER);
        mText.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        setFocusable(true);
        setClickable(true);
    }

    // ---------- helpers ----------

    private static float lerp(float a, float b, float t) { return a + (b - a) * t; }

    private static float clamp(float v) { return Math.max(0f, Math.min(1f, v)); }

    private static float ease(float t) {
        return t < 0.5f ? 4 * t * t * t : 1 - (float) Math.pow(-2 * t + 2, 3) / 2;
    }

    private static int mix(int a, int b, float t) {
        return Color.rgb(
                Math.round(lerp(Color.red(a), Color.red(b), t)),
                Math.round(lerp(Color.green(a), Color.green(b), t)),
                Math.round(lerp(Color.blue(a), Color.blue(b), t)));
    }

    private float dp(float v) { return v * getResources().getDisplayMetrics().density; }

    // ---------- input ----------

    private void start() {
        mState = STATE_PLAYING;
        mStart = SystemClock.uptimeMillis();
        mParticles.clear();
        mJumpStart = -100000;
        if (mReduceMotion) mStart -= (long) ((T_PAW + 1.5f) * 1000);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_UP) performClick();
        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        if (mState == STATE_IDLE) start();
        else if (mState == STATE_DONE) mJumpStart = SystemClock.uptimeMillis();
        return true;
    }

    // ---------- drawing ----------

    private void drawSky(Canvas c, float w, float h, float night, float now) {
        mPaint.setShader(new LinearGradient(0, 0, 0, h,
                new int[] {
                        mix(0xFF2B1F4A, 0xFF070B1F, night),
                        mix(0xFF8A4B7A, 0xFF16204A, night),
                        mix(0xFFF2A46B, 0xFF2E3470, night)},
                new float[] {0f, 0.55f, 1f}, Shader.TileMode.CLAMP));
        c.drawRect(0, 0, w, h, mPaint);
        mPaint.setShader(null);
        mPaint.setColor(Color.WHITE);
        for (float[] s : mStars) {
            double tw = Math.sin(now * 1.6 + s[3]);
            float a = night * (0.45f + 0.55f * (float) (tw * tw));
            if (a < 0.02f) continue;
            mPaint.setAlpha(Math.round(255 * a));
            c.drawCircle(s[0] * w, s[1] * h, dp(s[2]), mPaint);
        }
        mPaint.setAlpha(255);
    }

    /** One wing: root at (0,0). open 0 = straight V stroke, 1 = full wing. */
    private void wing(Canvas c, float ang, float len, float open, int color, int alpha) {
        c.save();
        c.rotate((float) Math.toDegrees(ang));
        float depth = len * 0.42f * open;
        float thick = lerp(len * 0.07f, 0, open);
        Path p = mPath;
        p.reset();
        p.moveTo(0, -thick);
        p.quadTo(len * 0.5f, -thick - len * 0.12f * open, len, 0);
        int n = 4;
        for (int i = n; i >= 1; i--) {
            float x0 = len * i / n, x1 = len * (i - 1) / n;
            float d = depth * (0.55f + 0.45f * i / n) + thick;
            p.quadTo((x0 + x1) / 2 + len * 0.04f, d * 1.15f,
                    x1, d * (i == 1 ? 0.25f : 0.55f) + thick);
        }
        p.lineTo(0, thick);
        p.close();
        mPaint.setColor(color);
        mPaint.setAlpha(alpha);
        mPaint.setShadowLayer(dp(9), 0, 0, Color.argb(Math.round(0.85f * alpha), 255, 214, 150));
        c.drawPath(p, mPaint);
        mPaint.clearShadowLayer();
        mPaint.setAlpha(255);
        c.restore();
    }

    private void drawWings(Canvas c, float x, float y, float size, float open, float flap,
            float alpha) {
        int a = Math.round(255 * alpha);
        c.save();
        c.translate(x, y);
        float base = (float) (-Math.PI / 2);
        float spread = lerp(0.42f, 0.95f, open);
        float f = (float) Math.sin(flap) * 0.38f * open;
        int color = mix(0xFFFFF4DC, 0xFFFFE0A3, open);
        c.save(); c.scale(1, -1); wing(c, -(base + spread + f), size, open, color, a); c.restore();
        c.save(); c.scale(-1, -1); wing(c, -(base + spread + f), size, open, color, a); c.restore();
        mPaint.setColor(color);
        mPaint.setAlpha(a);
        c.drawCircle(0, 0, lerp(size * 0.07f, size * 0.09f, open), mPaint);
        mPaint.setAlpha(255);
        c.restore();
    }

    private void glow(Canvas c, float r, int rgb, float centerAlpha, float alpha) {
        int a0 = Math.round(255 * centerAlpha * alpha);
        mGlow.setShader(new RadialGradient(0, 0, r,
                Color.argb(a0, Color.red(rgb), Color.green(rgb), Color.blue(rgb)),
                Color.argb(0, Color.red(rgb), Color.green(rgb), Color.blue(rgb)),
                Shader.TileMode.CLAMP));
        c.drawCircle(0, 0, r, mGlow);
        mGlow.setShader(null);
    }

    private void drawStar(Canvas c, float x, float y, float r, float alpha) {
        if (alpha <= 0 || r <= 0) return;
        c.save();
        c.translate(x, y);
        glow(c, r * 4, 0xFFFFECBE, 0.9f, alpha);
        Path p = mPath;
        p.reset();
        for (int i = 0; i < 8; i++) {
            double a = i * Math.PI / 4 - Math.PI / 2;
            float rr = i % 2 == 0 ? r : r * 0.28f;
            float px = (float) Math.cos(a) * rr, py = (float) Math.sin(a) * rr;
            if (i == 0) p.moveTo(px, py); else p.lineTo(px, py);
        }
        p.close();
        mPaint.setColor(0xFFFFF8E8);
        mPaint.setAlpha(Math.round(255 * alpha));
        c.drawPath(p, mPaint);
        mPaint.setAlpha(255);
        c.restore();
    }

    private void drawPaw(Canvas c, float x, float y, float s, float alpha, float sx, float sy) {
        if (alpha <= 0 || s <= 0) return;
        int a = Math.round(255 * alpha);
        c.save();
        c.translate(x, y);
        c.scale(sx, sy);
        glow(c, s * 2.4f, 0xFFFFD696, 0.55f, alpha);
        mPaint.setColor(0xFFFFE9C2);
        mPaint.setAlpha(a);
        mPaint.setShadowLayer(dp(7), 0, 0, Color.argb(Math.round(0.9f * a), 255, 214, 150));
        Path p = mPath;
        p.reset();
        p.moveTo(0, -s * 0.15f);
        p.cubicTo(s * 0.55f, -s * 0.2f, s * 0.75f, s * 0.55f, s * 0.32f, s * 0.68f);
        p.cubicTo(s * 0.12f, s * 0.75f, -s * 0.12f, s * 0.75f, -s * 0.32f, s * 0.68f);
        p.cubicTo(-s * 0.75f, s * 0.55f, -s * 0.55f, -s * 0.2f, 0, -s * 0.15f);
        p.close();
        c.drawPath(p, mPaint);
        float[][] toes = {
                {-0.62f, -0.42f, 0.2f, 0.26f, -0.35f}, {-0.22f, -0.78f, 0.21f, 0.28f, -0.12f},
                {0.22f, -0.78f, 0.21f, 0.28f, 0.12f}, {0.62f, -0.42f, 0.2f, 0.26f, 0.35f}};
        for (float[] t : toes) {
            c.save();
            c.translate(t[0] * s, t[1] * s);
            c.rotate((float) Math.toDegrees(t[4]));
            c.drawOval(-t[2] * s, -t[3] * s, t[2] * s, t[3] * s, mPaint);
            c.restore();
        }
        mPaint.clearShadowLayer();
        mPaint.setAlpha(255);
        c.restore();
    }

    private void emit(float x, float y) {
        if (mReduceMotion) return;
        for (int i = 0; i < 2; i++) {
            mParticles.add(new float[] {
                    x + (mRandom.nextFloat() - 0.5f) * dp(30), y,
                    (mRandom.nextFloat() - 0.5f) * dp(18), dp(20) + mRandom.nextFloat() * dp(30),
                    1f, dp(0.8f) + mRandom.nextFloat() * dp(1.8f)});
        }
    }

    private void burst(float x, float y) {
        for (int i = 0; i < 14; i++) {
            double a = mRandom.nextFloat() * 6.283;
            float v = dp(30) + mRandom.nextFloat() * dp(50);
            mParticles.add(new float[] {x, y, (float) Math.cos(a) * v, (float) Math.sin(a) * v,
                    1f, dp(0.8f) + mRandom.nextFloat() * dp(1.5f)});
        }
    }

    private void drawParticles(Canvas c, float dt) {
        mPaint.setColor(0xFFFFE7B8);
        for (int i = mParticles.size() - 1; i >= 0; i--) {
            float[] p = mParticles.get(i);
            p[0] += p[2] * dt;
            p[1] += p[3] * dt;
            p[4] -= dt * 0.7f;
            if (p[4] <= 0) { mParticles.remove(i); continue; }
            mPaint.setAlpha(Math.round(255 * p[4]));
            c.drawCircle(p[0], p[1], p[5], mPaint);
        }
        mPaint.setAlpha(255);
    }

    private void drawCenteredText(Canvas c, String s, float x, float y, float size, int color,
            float alpha, boolean shadow) {
        if (alpha <= 0) return;
        mText.setTextSize(size);
        mText.setColor(color);
        mText.setAlpha(Math.round(255 * alpha));
        if (shadow) {
            mText.setShadowLayer(dp(6), 0, 0, Color.argb(Math.round(180 * alpha), 255, 214, 150));
        } else {
            mText.clearShadowLayer();
        }
        Paint.FontMetrics fm = mText.getFontMetrics();
        c.drawText(s, x, y - (fm.ascent + fm.descent) / 2, mText);
    }

    @Override
    protected void onDraw(Canvas c) {
        long nowMs = SystemClock.uptimeMillis();
        float dt = Math.min(0.05f, (nowMs - mLast) / 1000f);
        mLast = nowMs;
        float now = nowMs / 1000f;
        float w = getWidth(), h = getHeight();
        float cx = w / 2, cy = h * 0.62f, size = Math.min(w, h) * 0.3f;
        float pawY = h * 0.2f, pawS = w * 0.085f;

        if (mState == STATE_IDLE) {
            drawSky(c, w, h, 0, now);
            float bob = mReduceMotion ? 0 : (float) Math.sin(nowMs / 700.0) * dp(3);
            drawWings(c, cx, cy + bob, size, 0, 0, 1);
            double pulse = Math.sin(nowMs / 500.0);
            drawCenteredText(c, mHint, cx, h * 0.86f, w * 0.042f, 0xFFFFF4DC,
                    0.6f + 0.4f * (float) (pulse * pulse), false);
        } else {
            float t = (nowMs - mStart) / 1000f;
            float night = ease(clamp((t - 0.6f) / (T_STAR - 0.6f)));
            drawSky(c, w, h, night, now);

            float open = ease(clamp(t / T_OPEN));
            float rise = ease(clamp((t - T_OPEN) / (T_RISE - T_OPEN)));
            float wy = lerp(cy, pawY, rise);
            float ws = size * lerp(1, 0.12f, rise);
            float wAlpha = 1 - clamp((t - (T_RISE - 0.4f)) / 0.6f);
            float flap = t * lerp(5, 9, rise);
            if (wAlpha > 0) {
                drawWings(c, cx, wy, ws, open, flap, wAlpha);
                if (t > T_OPEN && t < T_RISE) emit(cx, wy + ws * 0.2f);
            }
            drawParticles(c, dt);

            float starIn = clamp((t - (T_RISE - 0.3f)) / 0.6f);
            float toPaw = ease(clamp((t - T_STAR) / (T_PAW - T_STAR)));
            float tw = 1 + 0.15f * (float) Math.sin(nowMs / 160.0);
            drawStar(c, cx, pawY, pawS * 0.9f * tw * (1 - toPaw * 0.8f), starIn * (1 - toPaw));

            float jy = 0, sx = 1, sy = 1;
            float jt = (nowMs - mJumpStart) / 1000f;
            if (jt >= 0 && jt < 0.9f) {
                float p = jt / 0.9f;
                jy = -(float) Math.sin(Math.PI * Math.min(1, p / 0.8f)) * h * 0.07f;
                if (p < 0.12f) {
                    float q = p / 0.12f; sx = 1 + 0.15f * q; sy = 1 - 0.15f * q;
                } else if (p > 0.8f) {
                    float q = (float) Math.sin(Math.PI * (p - 0.8f) / 0.2f);
                    sx = 1 + 0.18f * q; sy = 1 - 0.18f * q;
                } else {
                    sx = 0.94f; sy = 1.08f;
                }
                if (jt < dt * 1.5f) burst(cx, pawY);
            }
            drawPaw(c, cx, pawY + jy, pawS * lerp(0.4f, 1, toPaw), toPaw, sx, sy);
            drawCenteredText(c, mName, cx, h * 0.34f, w * 0.085f, 0xFFFFF4DC,
                    clamp((t - T_PAW + 0.4f) / 1.0f), true);

            if (mState == STATE_PLAYING && t > T_PAW + 0.6f) mState = STATE_DONE;
        }
        postInvalidateOnAnimation();
    }
}
