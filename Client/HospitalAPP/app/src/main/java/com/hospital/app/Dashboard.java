package com.hospital.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.window.OnBackInvokedCallback;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class Dashboard extends AppCompatActivity {

    User user;

    LinearLayout app_info;

    ProgressDialog coreprog;

    ImageView homeIcon;
    ImageView searchIcon;
    ImageView userIcon;

    ImageView directs;

    CircleImageView pp;

    ImageView[] icons;
    int[] normalIcons;
    int[] filledIcons;

    ViewPager2 viewPager;
    FragmentStateAdapter pagerAdapter;

    WebSocketManager webSocketManager;

    Database db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        viewPager = findViewById(R.id.view_pager);
        pagerAdapter = new ScreenSlidePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        webSocketManager = Utils.getInstance(this);

        String token = getSharedPreferences("User", Context.MODE_PRIVATE).getString("token", "");

        db = new Database(getApplicationContext(), token);

        /* db.update("users", "9b763f98-5155-46a5-8606-b00d860b73a3", "username=SELAM,name=SASD", new Database.OnSuccessListener() {
            @Override
            public void onSuccess(boolean isDone) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            if (isDone) {
                                ModernToast.showCustomToast(Dashboard.this, "Güncellendi");
                            } else {
                                ModernToast.showCustomToast(Dashboard.this, "Hata");
                            }
                        }, 100);
                    }
                });
            }

            @Override
            public void onError(Throwable throwable) {

            }
        });

        db.create("users", "username=123,address=asd", new Database.OnSuccessListener() {
            @Override
            public void onSuccess(boolean isDone) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            if (isDone) {
                                ModernToast.showCustomToast(Dashboard.this, "Eklendi");
                            } else {
                                ModernToast.showCustomToast(Dashboard.this, "Hata");
                            }
                        }, 100);
                    }
                });
            }

            @Override
            public void onError(Throwable throwable) {

            }
        });

        db.get("users", "username=" + "uchihasasukedev", new Database.OnCompleteListener() {
            @Override
            public void onSuccess(JSONObject data) throws JSONException {
                Log.e("asd", data.getString("username"));
            }

            @Override
            public void onError(Throwable throwable) {

            }
        }); */


        user = new User(Dashboard.this);

        LinearLayout homeLayout = findViewById(R.id.homeLayout);
        LinearLayout searchLayout = findViewById(R.id.searchLayout);
        LinearLayout userLayout = findViewById(R.id.userLayout);

        LinearLayout indicator = findViewById(R.id.indicator);
        homeIcon = findViewById(R.id.homeIcon);
        searchIcon = findViewById(R.id.searchIcon);
        userIcon = findViewById(R.id.userIcon);

        directs = findViewById(R.id.directs);

        pp = findViewById(R.id.pp);

        pp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), UserProfile.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        directs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), Directs.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        icons = new ImageView[]{homeIcon, searchIcon, userIcon};
        normalIcons = new int[]{R.drawable.home, R.drawable.search, R.drawable.user_m};
        filledIcons = new int[]{R.drawable.home_fill, R.drawable.search_fill, R.drawable.user_fill};

        homeLayout.setOnClickListener(v -> onIconSelected(homeLayout, homeIcon, 0, indicator));
        searchLayout.setOnClickListener(v -> onIconSelected(searchLayout, searchIcon, 1, indicator));
        userLayout.setOnClickListener(v -> onIconSelected(userLayout, userIcon, 2, indicator));

        viewPager.setPageTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                if (position < -1 || position > 1) {
                    page.setAlpha(0f);
                } else {
                    float alpha = 1 - Math.abs(position) * 0.5f;
                    page.setAlpha(alpha);

                    float scale = 0.75f + (1 - Math.abs(position)) * 0.25f;
                    page.setScaleX(scale);
                    page.setScaleY(scale);
                }
            }
        });


        user.getPp(new User.PpCallback() {
            @Override
            public void onSuccess(String ppUrl) {
                RequestOptions requestOptions = new RequestOptions()
                        .transform(new RoundedCorners(100));

                Glide.with(Dashboard.this)
                        .load(ppUrl)
                        .apply(requestOptions)
                        .into(pp);

                openLoader(false);
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e("PP_ERROR", errorMessage);

                openLoader(false);
            }
        });

        viewPager.setOffscreenPageLimit(4);

        viewPager.setUserInputEnabled(false);

        OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();
        dispatcher.addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
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

    public void openLoader(final boolean _visibility) {
        if (_visibility) {
            if (coreprog == null){
                coreprog = new ProgressDialog(this);
                coreprog.setCancelable(false);
                coreprog.setCanceledOnTouchOutside(false);

                coreprog.requestWindowFeature(Window.FEATURE_NO_TITLE);  coreprog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));

            }
            coreprog.show();
            coreprog.setContentView(R.layout.loading);


            LinearLayout linear2 = (LinearLayout)coreprog.findViewById(R.id.linear2);

            LinearLayout back = (LinearLayout)coreprog.findViewById(R.id.background);

            LinearLayout layout_progress = (LinearLayout)coreprog.findViewById(R.id.layout_progress);

            android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
            gd.setColor(Color.parseColor("#FFFFFF"));
            gd.setCornerRadius(45);
            gd.setStroke(0, Color.WHITE);
            linear2.setBackground(gd);

            RadialProgressBar progress = new RadialProgressBar(this);
            layout_progress.addView(progress);
        }
        else {
            if (coreprog != null){
                coreprog.dismiss();
            }
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {
        private final List<Fragment> fragmentList = new ArrayList<>();

        public ScreenSlidePagerAdapter(FragmentActivity fa) {
            super(fa);
            fragmentList.add(new MainFragment());
            fragmentList.add(new SearchFragment());
            fragmentList.add(new AppointmentsFragment());
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