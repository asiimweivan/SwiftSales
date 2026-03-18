package com.example.swiftsales.ui.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;
import com.example.swiftsales.R;
import com.example.swiftsales.ui.BaseActivity;
import com.example.swiftsales.ui.auth.SignInActivity;
import com.example.swiftsales.ui.auth.SignUpActivity;

public class OnboardingActivity extends BaseActivity {

    private ViewPager2 viewPager;
    private TextView   btnNext, btnSkip, btnGoSignIn;
    private int        currentPage = 0;

    // ── Slide emojis (visual — no translation needed) ────────────────────────
    private final String[] emojis = {"🛒", "📊", "👥"};

    // ── Background tints (unchanged) ─────────────────────────────────────────
    private final int[] bgColors = {
            0xFFEFF6FF,   // light blue
            0xFFF0FDF4,   // light green
            0xFFFFF7ED    // light orange
    };

    // ── Slide content — loaded from string resources so locale applies ────────
    private String[] titles;
    private String[] subtexts;
    private String[][] pills;

    // ═══════════════════════════════════════════════════════════════════════════
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        // Load localised strings AFTER super.onCreate so locale is already applied
        loadLocalizedStrings();

        viewPager   = findViewById(R.id.viewPager);
        btnNext     = findViewById(R.id.btnNext);
        btnSkip     = findViewById(R.id.btnSkip);
        btnGoSignIn = findViewById(R.id.btnGoSignIn);
        DotsIndicator dotsIndicator = findViewById(R.id.dotsIndicator);

        // Set initial button text from resources
        btnNext.setText(getString(R.string.next));
        btnSkip.setText(getString(R.string.skip));
        btnGoSignIn.setText(getString(R.string.sign_in));

        OnboardingAdapter adapter = new OnboardingAdapter();
        viewPager.setAdapter(adapter);
        dotsIndicator.attachTo(viewPager);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentPage = position;
                viewPager.setBackgroundColor(bgColors[position]);

                if (position == emojis.length - 1) {
                    btnNext.setText(getString(R.string.get_started) + "  🚀");
                    btnSkip.setVisibility(View.INVISIBLE);
                } else {
                    btnNext.setText(getString(R.string.next) + "  →");
                    btnSkip.setVisibility(View.VISIBLE);
                }
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentPage < emojis.length - 1) {
                viewPager.setCurrentItem(currentPage + 1, true);
            } else {
                goToSignUp();
            }
        });

        btnSkip.setOnClickListener(v -> goToSignIn());
        btnGoSignIn.setOnClickListener(v -> goToSignIn());
    }

    // ── Load string arrays from resources ─────────────────────────────────────

    private void loadLocalizedStrings() {
        titles = new String[]{
                getString(R.string.onboard1_title),
                getString(R.string.onboard2_title),
                getString(R.string.onboard3_title)
        };

        subtexts = new String[]{
                getString(R.string.onboard1_sub),
                getString(R.string.onboard2_sub),
                getString(R.string.onboard3_sub)
        };

        pills = new String[][]{
                {
                        getString(R.string.onboard1_pill1),
                        getString(R.string.onboard1_pill2),
                        getString(R.string.onboard1_pill3)
                },
                {
                        getString(R.string.onboard2_pill1),
                        getString(R.string.onboard2_pill2),
                        getString(R.string.onboard2_pill3)
                },
                {
                        getString(R.string.onboard3_pill1),
                        getString(R.string.onboard3_pill2),
                        getString(R.string.onboard3_pill3)
                }
        };
    }

    // ── Navigation (identical to original) ───────────────────────────────────

    private void goToSignUp() {
        startActivity(new Intent(this, SignUpActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void goToSignIn() {
        startActivity(new Intent(this, SignInActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    // ── Adapter (same IDs as item_onboarding.xml) ─────────────────────────────

    class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.VH> {

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_onboarding, parent, false);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(VH h, int position) {
            h.emoji.setText(emojis[position]);
            h.title.setText(titles[position]);
            h.subtext.setText(subtexts[position]);
            h.slideNumber.setText("0" + (position + 1) + " / 0" + emojis.length);
            h.pill1.setText(pills[position][0]);
            h.pill2.setText(pills[position][1]);
            h.pill3.setText(pills[position][2]);
        }

        @Override
        public int getItemCount() { return emojis.length; }

        class VH extends RecyclerView.ViewHolder {
            TextView emoji, title, subtext, slideNumber, pill1, pill2, pill3;

            VH(View v) {
                super(v);
                emoji       = v.findViewById(R.id.onboardingEmoji);
                title       = v.findViewById(R.id.onboardingTitle);
                subtext     = v.findViewById(R.id.onboardingSubtext);
                slideNumber = v.findViewById(R.id.tvSlideNumber);
                pill1       = v.findViewById(R.id.pill1);
                pill2       = v.findViewById(R.id.pill2);
                pill3       = v.findViewById(R.id.pill3);
            }
        }
    }
}