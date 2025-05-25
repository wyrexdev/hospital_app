package com.hospital.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.haytham.coder.curveview.CurveView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private List<SliderItem> sliderItems;
    private LinearLayout dotsLayout;

    private TextView next;

    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        hideSystemUI();

        pref = getSharedPreferences("User", Context.MODE_PRIVATE);

        viewPager = findViewById(R.id.viewPager);
        dotsLayout = findViewById(R.id.dotsLayout);
        next = findViewById(R.id.next);

        sliderItems = new ArrayList<>();
        sliderItems.add(new SliderItem("Hızlı ve Güvenilir Sağlık Hizmetleri", "Acil durumlar için 7/24 hizmet veren ekibimizle, hızlı ve güvenilir sağlık çözümleri sunuyoruz. Sağlığınızı bize emanet edin, huzur içinde olun!", R.drawable.contact));
        sliderItems.add(new SliderItem("Kişiye Özel Sağlık Deneyimi", "Her bireyin sağlık ihtiyaçları farklıdır. Kişisel sağlık hedeflerinize ulaşmanız için özel olarak tasarlanmış hizmetlerimizle size en iyi deneyimi sunmayı amaçlıyoruz.", R.drawable.pills));
        sliderItems.add(new SliderItem("Sağlığınıza Değer Veriyoruz", "Uzman hekimlerimiz ve modern teknolojimizle, sağlığınızı ön planda tutarak en iyi hizmeti sunmayı hedefliyoruz. Sağlık yolculuğunuzda yanınızdayız!", R.drawable.connect));

        SliderAdapter adapter = new SliderAdapter(sliderItems);
        viewPager.setAdapter(adapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setupDotsIndicator(position);
                animateDots(position);
            }
        });

        Typeface nunitoBold = Typeface.createFromAsset(getAssets(), "Nunito-Bold.ttf");
        next.setTypeface(nunitoBold);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(viewPager.getCurrentItem() >= (sliderItems.size() - 1)){
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putBoolean("isDoneOpenScene", true);
                    editor.apply();

                    Intent i = new Intent();
                    i.setClass(getApplicationContext(), Login.class);
                    startActivity(i);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                } else {
                    viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
                }
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

    private int previousPosition = -1;

    private void animateDots(int position) {
        int dotsCount = sliderItems.size();

        for (int i = 0; i < dotsCount; i++) {
            final View dot = dotsLayout.getChildAt(i);
            ViewGroup.LayoutParams layoutParams = dot.getLayoutParams();
            layoutParams.width = 30;
            dot.setLayoutParams(layoutParams);
        }

        if (previousPosition != -1 && previousPosition != position) {
            final View previousDot = dotsLayout.getChildAt(previousPosition);
            ValueAnimator shrinkAnimator = ValueAnimator.ofFloat(60f, 30f);
            shrinkAnimator.setDuration(150);
            shrinkAnimator.addUpdateListener(animation -> {
                float animatedValue = (float) animation.getAnimatedValue();
                ViewGroup.LayoutParams inactiveLayoutParams = previousDot.getLayoutParams();
                inactiveLayoutParams.width = (int) animatedValue;
                previousDot.setLayoutParams(inactiveLayoutParams);
            });

            ValueAnimator colorAnimatorInactive = ValueAnimator.ofArgb(Color.parseColor("#007BFF"), Color.parseColor("#A1C8FF"));
            colorAnimatorInactive.setDuration(150);
            colorAnimatorInactive.addUpdateListener(animation -> {
                int color = (int) animation.getAnimatedValue();
                previousDot.setBackgroundTintMode(PorterDuff.Mode.SRC_ATOP);
                previousDot.setBackgroundTintList(ColorStateList.valueOf(color));
            });

            shrinkAnimator.start();
            colorAnimatorInactive.start();
        }

        final View newActiveDot = dotsLayout.getChildAt(position);
        ValueAnimator growAnimator = ValueAnimator.ofFloat(30f, 60f);
        growAnimator.setDuration(150);
        growAnimator.addUpdateListener(animation -> {
            float animatedValue = (float) animation.getAnimatedValue();
            ViewGroup.LayoutParams activeLayoutParams = newActiveDot.getLayoutParams();
            activeLayoutParams.width = (int) animatedValue;
            newActiveDot.setLayoutParams(activeLayoutParams);
        });

        ValueAnimator colorAnimatorActive = ValueAnimator.ofArgb(Color.parseColor("#A1C8FF"), Color.parseColor("#007BFF"));
        colorAnimatorActive.setDuration(150);
        colorAnimatorActive.addUpdateListener(animation -> {
            int color = (int) animation.getAnimatedValue();
            newActiveDot.setBackgroundTintMode(PorterDuff.Mode.SRC_ATOP);
            newActiveDot.setBackgroundTintList(ColorStateList.valueOf(color));
        });

        growAnimator.start();
        colorAnimatorActive.start();

        previousPosition = position;

        if(position == (dotsCount - 1)){
            changeTextViewText(next, "Bitir", 25);
        } else {
            changeTextViewText(next, "Sonraki", 25);
        }
    }

    private Handler handler = new Handler();

    private void changeTextViewText(TextView textView, String newText, int delay) {
        final String oldText = textView.getText().toString();
        final StringBuilder sb = new StringBuilder(oldText);

        final Runnable deleteRunnable = new Runnable() {
            @Override
            public void run() {
                if (sb.length() > 0) {
                    sb.deleteCharAt(sb.length() - 1);
                    textView.setText(sb.toString());
                    handler.postDelayed(this, delay);
                } else {
                    addNewText(textView, newText, delay);
                }
            }
        };

        handler.post(deleteRunnable);
    }

    private void addNewText(TextView textView, String newText, int delay) {
        final StringBuilder sb = new StringBuilder();

        final Runnable addRunnable = new Runnable() {
            @Override
            public void run() {
                if (sb.length() < newText.length()) {
                    sb.append(newText.charAt(sb.length()));
                    textView.setText(sb.toString());
                    handler.postDelayed(this, delay);
                }
            }
        };

        handler.post(addRunnable);
    }

    private void setupDotsIndicator(int position) {
        int dotsCount = sliderItems.size();
        dotsLayout.removeAllViews();

        for (int i = 0; i < dotsCount; i++) {
            ImageView dot = new ImageView(this);

            dot.setImageResource(R.drawable.inactive_dot);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    15,
                    20);
            params.setMargins(8, 0, 8, 0);
            dotsLayout.addView(dot, params);
        }

        if (position >= 0 && position < dotsCount) {
            ImageView activeDot = (ImageView) dotsLayout.getChildAt(position);
            activeDot.setImageResource(R.drawable.active_dot);
        }
    }

    class SliderAdapter extends RecyclerView.Adapter<SliderAdapter.SliderViewHolder> {

        private List<SliderItem> sliderItems;

        public SliderAdapter(List<SliderItem> sliderItems) {
            this.sliderItems = sliderItems;
        }

        @NonNull
        @Override
        public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.slider_item, parent, false);
            return new SliderViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
            SliderItem item = sliderItems.get(position);
            holder.titleTextView.setText(item.getTitle());
            holder.descriptionTextView.setText(item.getDescription());

            Typeface nunito = Typeface.createFromAsset(getAssets(), "nunito.ttf");
            Typeface nunitoBold = Typeface.createFromAsset(getAssets(), "Nunito-Bold.ttf");
            Typeface nunitoMedium = Typeface.createFromAsset(getAssets(), "Nunito-Medium.ttf");

            holder.titleTextView.setTypeface(nunitoBold);
            holder.descriptionTextView.setTypeface(nunitoBold);

            holder.image.setImageResource(item.resource);
        }

        @Override
        public int getItemCount() {
            return sliderItems.size();
        }

        public static class SliderViewHolder extends RecyclerView.ViewHolder {
            TextView titleTextView;
            TextView descriptionTextView;
            ImageView image;

            public SliderViewHolder(@NonNull View itemView) {
                super(itemView);
                image = itemView.findViewById(R.id.image);
                titleTextView = itemView.findViewById(R.id.titleTextView);
                descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            }
        }
    }

    class SliderItem {
        private String title;
        private String description;
        private int resource;

        public SliderItem(String title, String description, int resource) {
            this.title = title;
            this.description = description;
            this.resource = resource;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }
    }
}
