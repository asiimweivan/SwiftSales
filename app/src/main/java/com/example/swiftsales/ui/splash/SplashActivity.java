package com.example.swiftsales.ui.splash;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import com.example.swiftsales.R;
import com.example.swiftsales.ui.BaseActivity;
import com.example.swiftsales.ui.auth.BusinessSetupActivity;
import com.example.swiftsales.ui.dashboard.DashboardActivity;
import com.example.swiftsales.ui.language.LanguageActivity;
import com.example.swiftsales.ui.onboarding.OnboardingActivity;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // ── Null-safe view lookups ────────────────────────────────────────────
        View     glowCircle  = findViewById(R.id.glowCircle);
        TextView logoIcon    = findViewById(R.id.logoIcon);
        TextView appName     = findViewById(R.id.appName);
        TextView tagline     = findViewById(R.id.tagline);
        View     loadingDots = findViewById(R.id.loadingDots);

        // ── Tagline — null-safe string lookup ─────────────────────────────────
        try {
            if (tagline != null)
                tagline.setText(getString(R.string.splash_tagline));
        } catch (Exception ignored) {
            // splash_tagline missing from strings.xml — use hardcoded fallback
            if (tagline != null)
                tagline.setText("Run Your Business Smarter");
        }

        // ── Animate only views that actually exist ────────────────────────────
        AnimatorSet set = new AnimatorSet();

        if (glowCircle != null) {
            ObjectAnimator glowFade = ObjectAnimator.ofFloat(glowCircle, "alpha", 0f, 0.6f);
            glowFade.setDuration(800);
            glowFade.setStartDelay(100);
            set.playTogether(glowFade);
        }

        if (logoIcon != null) {
            ObjectAnimator logoScaleX = ObjectAnimator.ofFloat(logoIcon, "scaleX", 0.3f, 1f);
            ObjectAnimator logoScaleY = ObjectAnimator.ofFloat(logoIcon, "scaleY", 0.3f, 1f);
            ObjectAnimator logoFade   = ObjectAnimator.ofFloat(logoIcon, "alpha",  0f, 1f);
            logoScaleX.setInterpolator(new OvershootInterpolator(2f));
            logoScaleY.setInterpolator(new OvershootInterpolator(2f));
            logoScaleX.setDuration(700); logoScaleX.setStartDelay(300);
            logoScaleY.setDuration(700); logoScaleY.setStartDelay(300);
            logoFade.setDuration(400);   logoFade.setStartDelay(300);
            set.playTogether(logoScaleX, logoScaleY, logoFade);
        }

        if (appName != null) {
            ObjectAnimator nameFade  = ObjectAnimator.ofFloat(appName, "alpha",        0f,  1f);
            ObjectAnimator nameSlide = ObjectAnimator.ofFloat(appName, "translationY", 30f, 0f);
            nameFade.setDuration(500);  nameFade.setStartDelay(800);
            nameSlide.setDuration(500); nameSlide.setStartDelay(800);
            set.playTogether(nameFade, nameSlide);
        }

        if (tagline != null) {
            ObjectAnimator tagFade  = ObjectAnimator.ofFloat(tagline, "alpha",        0f,  1f);
            ObjectAnimator tagSlide = ObjectAnimator.ofFloat(tagline, "translationY", 20f, 0f);
            tagFade.setDuration(500);  tagFade.setStartDelay(1000);
            tagSlide.setDuration(500); tagSlide.setStartDelay(1000);
            set.playTogether(tagFade, tagSlide);
        }

        if (loadingDots != null) {
            ObjectAnimator dotsFade = ObjectAnimator.ofFloat(loadingDots, "alpha", 0f, 1f);
            dotsFade.setDuration(400);
            dotsFade.setStartDelay(1200);
            set.playTogether(dotsFade);
        }

        set.start();
        animateDots();

        // ── Navigate after 3.2s ───────────────────────────────────────────────
        new Handler().postDelayed(() -> {
            try {
                SharedPreferences prefs = getSharedPreferences("SwiftSalesAuth", MODE_PRIVATE);
                boolean isLoggedIn    = prefs.getBoolean("is_logged_in",  false);
                boolean setupComplete = prefs.getBoolean("setup_complete", false);
                boolean langChosen    = prefs.getBoolean("lang_chosen",    false);

                Intent intent;
                if (isLoggedIn && setupComplete) {
                    intent = new Intent(this, DashboardActivity.class);
                } else if (isLoggedIn) {
                    intent = new Intent(this, BusinessSetupActivity.class);
                } else if (langChosen) {
                    intent = new Intent(this, OnboardingActivity.class);
                } else {
                    intent = new Intent(this, LanguageActivity.class);
                }

                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();

            } catch (Exception e) {
                // Last resort — go straight to Language screen
                startActivity(new Intent(this, LanguageActivity.class));
                finish();
            }
        }, 3200);
    }

    private void animateDots() {
        try {
            animateDot(findViewById(R.id.dot1), 1400);
            animateDot(findViewById(R.id.dot2), 1700);
            animateDot(findViewById(R.id.dot3), 2000);
        } catch (Exception ignored) {}
    }

    private void animateDot(View dot, long delay) {
        if (dot == null) return;
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(dot, "scaleX", 1f, 1.6f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(dot, "scaleY", 1f, 1.6f, 1f);
        ObjectAnimator alpha  = ObjectAnimator.ofFloat(dot, "alpha",  0.4f, 1f, 0.4f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY, alpha);
        set.setDuration(500);
        set.setStartDelay(delay);
        set.start();
    }
}