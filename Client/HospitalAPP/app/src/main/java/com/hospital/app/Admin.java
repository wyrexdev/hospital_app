package com.hospital.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

public class Admin extends AppCompatActivity {

    ProgressDialog coreprog;

    ImageView homeIcon;
    ImageView doctorsIcon;
    ImageView settingIcon;

    ImageView[] icons;
    int[] normalIcons;
    int[] filledIcons;

    ViewPager2 viewPager;
    FragmentStateAdapter pagerAdapter;

    WebSocketManager webSocketManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);

        webSocketManager = Utils.getInstance(this);

        String token = getSharedPreferences("User", Context.MODE_PRIVATE).getString("token", "");
        
        viewPager = findViewById(R.id.view_pager);
        pagerAdapter = new ScreenSlidePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);


        LinearLayout homeLayout = findViewById(R.id.homeLayout);
        LinearLayout doctorsLayout = findViewById(R.id.doctorsLayout);
        LinearLayout settingLayout = findViewById(R.id.settingLayout);

        LinearLayout indicator = findViewById(R.id.indicator);
        homeIcon = findViewById(R.id.homeIcon);
        doctorsIcon = findViewById(R.id.doctors);
        settingIcon = findViewById(R.id.settingIcon);

        icons = new ImageView[]{homeIcon, doctorsIcon, settingIcon};
        normalIcons = new int[]{R.drawable.home, R.drawable.doctor_i, R.drawable.setting};
        filledIcons = new int[]{R.drawable.home_fill, R.drawable.doctor_fill, R.drawable.setting_fill};

        homeLayout.setOnClickListener(v -> onIconSelected(homeLayout, homeIcon, 0, indicator));
        doctorsLayout.setOnClickListener(v -> onIconSelected(doctorsLayout, doctorsIcon, 1, indicator));
        settingLayout.setOnClickListener(v -> onIconSelected(settingLayout, settingIcon, 2, indicator));

        viewPager.setUserInputEnabled(false);
        
        hideSystemUI();
    }

    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void onIconSelected(LinearLayout selectedLayout, ImageView selectedIcon, int selectedIndex, LinearLayout indicator) {
        int targetX = selectedLayout.getLeft() + selectedLayout.getWidth() / 2 - indicator.getWidth() / 2;

        ObjectAnimator.ofFloat(indicator, "translationX", targetX).setDuration(300).start();

        viewPager.setCurrentItem(selectedIndex);

        for (int i = 0; i < icons.length; i++) {
            if (i != selectedIndex) {
                animateIconChange(icons[i], normalIcons[i], false);
            }
        }

        animateIconChange(selectedIcon, filledIcons[selectedIndex], true);
    }

    private void animateIconChange(ImageView icon, int newIconRes, boolean applyAnimation) {
        if (applyAnimation) {
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(icon, "alpha", 1f, 0f);
            fadeOut.setDuration(150);

            fadeOut.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    icon.setImageResource(newIconRes);

                    ObjectAnimator fadeIn = ObjectAnimator.ofFloat(icon, "alpha", 0f, 1f);
                    fadeIn.setDuration(150);

                    ObjectAnimator scaleX = ObjectAnimator.ofFloat(icon, "scaleX", 0f, 1f);
                    ObjectAnimator scaleY = ObjectAnimator.ofFloat(icon, "scaleY", 0f, 1f);

                    AnimatorSet set = new AnimatorSet();
                    set.playTogether(fadeIn, scaleX, scaleY);
                    set.setDuration(300);
                    set.start();
                }
            });

            fadeOut.start();
        } else {
            icon.setImageResource(newIconRes);
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {
        private final List<Fragment> fragmentList = new ArrayList<>();

        public ScreenSlidePagerAdapter(FragmentActivity fa) {
            super(fa);
            fragmentList.add(new AdminHome());
            fragmentList.add(new Doctors());
            fragmentList.add(new AdminMyAppointments());
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getItemCount() {
            return fragmentList.size();
        }
    }
}