package com.hospital.app;

import static android.content.Context.MODE_PRIVATE;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.fonts.FontFamily;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class CustomDatePickerView extends LinearLayout {

    private static final String TAG = "CustomDatePickerView";

    // UI Components
    private Button btnPrevYear, btnPrevMonth, btnNextMonth, btnNextYear;
    private TextView tvMonthYear;
    private GridLayout calendarGrid;
    private MaterialCardView headerCard;
    private MaterialCardView detailsPanel;
    private TextView tvSelectedDate, tvDayName;
    private LinearLayout appointmentsContainer;

    // Calendar instances
    private Calendar currentCalendar = Calendar.getInstance();
    private Calendar today = Calendar.getInstance();
    private Calendar selectedDay = null;

    // Data collections
    private Set<String> specialDaysPast = new HashSet<>();
    private Set<String> specialDaysFuture = new HashSet<>();
    private Map<String, List<Appointment>> appointmentMap = new HashMap<>();

    // Date formats
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("tr"));
    private SimpleDateFormat dayNameFormat = new SimpleDateFormat("EEEE", Locale.forLanguageTag("tr"));
    private SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.forLanguageTag("tr"));
    private SimpleDateFormat shortDayNameFormat = new SimpleDateFormat("EEE", Locale.forLanguageTag("tr"));
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    private final int DAYS_COUNT = 42;
    private int primaryColor;
    private int primaryDarkColor;
    private int accentColor;
    private int textColor;
    private int disabledTextColor;
    private int appointmentColor;
    private int cornerRadius;
    private int strokeWidth;
    private int dayCellSize;

    private Typeface font;

    // Database
    private Database db;

    // Interfaces
    public interface OnAddAppointmentListener {
        void onAddAppointment(String date);
    }

    public interface OnAppointmentClickListener {
        void onAppointmentClick(Appointment appointment);
    }

    private OnAddAppointmentListener addAppointmentListener;
    private OnAppointmentClickListener appointmentClickListener;

    public static class Appointment {
        private String id;
        private String doctorName;
        private String time;
        private String description;
        private String date;

        public Appointment(String id, String doctorName, String time, String description, String date) {
            this.id = id;
            this.doctorName = doctorName;
            this.time = time;
            this.description = description;
            this.date = date;
        }

        public String getId() { return id; }
        public String getDoctorName() { return doctorName; }
        public String getTime() { return time; }
        public String getDescription() { return description; }
        public String getDate() { return date; }

        @Override
        public String toString() {
            return doctorName + " - " + time + "\n" + description;
        }
    }

    // Constructors
    public CustomDatePickerView(Context context) {
        super(context);
        init(context, null);
    }

    public CustomDatePickerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CustomDatePickerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context ctx, AttributeSet attrs) {
        setOrientation(VERTICAL);
        LayoutInflater.from(ctx).inflate(R.layout.custom_datepicker, this, true);

        initializeFonts();
        initializeDimensions();
        initializeColors();
        initializeViews();
        setupDatabase();
        loadAppointmentsFromDatabase();
        styleComponents();
        setupListeners();
        updateCalendar();
    }

    private void initializeFonts() {
        font = Typeface.createFromAsset(getContext().getAssets(), "Nunito-Bold.ttf");
    }

    private void initializeDimensions() {
        cornerRadius = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());
        strokeWidth = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());
        dayCellSize = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 46, getResources().getDisplayMetrics());
    }

    private void initializeColors() {
        primaryColor = ContextCompat.getColor(getContext(), R.color.colorPrimary);
        primaryDarkColor = ContextCompat.getColor(getContext(), R.color.colorPrimaryDark);
        accentColor = ContextCompat.getColor(getContext(), R.color.colorAccent);
        textColor = Color.BLACK;
        disabledTextColor = Color.GRAY;
        appointmentColor = ContextCompat.getColor(getContext(), android.R.color.holo_green_light);
    }

    private void initializeViews() {
        btnPrevYear = findViewById(R.id.btnPrevYear);
        btnPrevMonth = findViewById(R.id.btnPrevMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);
        btnNextYear = findViewById(R.id.btnNextYear);
        tvMonthYear = findViewById(R.id.tvMonthYear);
        calendarGrid = findViewById(R.id.calendarGrid);
        headerCard = findViewById(R.id.headerCard);
        detailsPanel = findViewById(R.id.detailsPanel);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvDayName = findViewById(R.id.tvDayName);
        appointmentsContainer = findViewById(R.id.appointmentsContainer);
    }

    private void setupDatabase() {
        db = new Database(getContext(),
                getContext().getSharedPreferences("User", MODE_PRIVATE).getString("token", ""));
    }

    private void loadAppointmentsFromDatabase() {
        db.get("appointments", "user_id=t-o-k-e-n", new Database.OnCompleteListener() {
            @Override
            public void onSuccess(JSONArray message) {
                if (getContext() instanceof Activity) {
                    ((Activity) getContext()).runOnUiThread(() -> {
                        try {
                            if(message.toString().contains("appointment")) {
                                clearAllAppointments();
                                for (int i = 0; i < message.length(); i++) {
                                    JSONObject data = message.getJSONObject(i);
                                    parseAndAddAppointment(data);
                                }
                                post(() -> updateCalendar());
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing appointments JSON", e);
                        }
                    });
                }
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e(TAG, "Error loading appointments", throwable);
            }
        });
    }

    private void parseAndAddAppointment(JSONObject data) throws JSONException {
        if (getContext() instanceof Activity) {
            ((Activity) getContext()).runOnUiThread(() -> {
                try {
                    Log.d(TAG, "parseAndAddAppointment: " + data.toString());
                    String id = data.getString("id");
                    String time = data.getString("appointment_time");
                    String dateParts = data.getString("appointment_date");
                    String doctorId = data.getString("doctor_id");

                    String[] parts = dateParts.split("/");

                    db.get("users", "id=" + doctorId, new Database.OnCompleteListener() {
                        @Override
                        public void onSuccess(JSONArray message) throws JSONException, ParseException {
                            String date = parts[2] + "-" + parts[1] + "-" + parts[0];
                            addAppointment(new Appointment(id, message.getJSONObject(0).getString("name") + " " + message.getJSONObject(0).getString("surname"), time, "", date));
                        }

                        @Override
                        public void onError(Throwable throwable) {

                        }
                    });
                } catch (Exception ignored) {
                    Log.e("ERROR", ignored.getMessage());
                }
            });
        }
    }

    private void clearAllAppointments() {
        appointmentMap.clear();
    }

    private void styleComponents() {
        styleHeader();
        styleNavigationButtons();
        styleDetailsPanel();
        styleAddButton();
    }

    private void styleHeader() {
        headerCard.setRadius(cornerRadius);
        headerCard.setCardElevation(8);
        tvMonthYear.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        tvMonthYear.setTypeface(font);
    }

    private void styleNavigationButtons() {
        styleNavigationButton(btnPrevYear, "««", primaryDarkColor);
        styleNavigationButton(btnPrevMonth, "«", primaryColor);
        styleNavigationButton(btnNextMonth, "»", primaryColor);
        styleNavigationButton(btnNextYear, "»»", primaryDarkColor);
    }

    private void styleNavigationButton(Button button, String text, int bgColor) {
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setColor(bgColor);
        bg.setCornerRadius(cornerRadius);

        button.setBackground(bg);
        button.setTextColor(Color.WHITE);
        button.setText(text);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);

        TypedValue outValue = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true);
        ViewCompat.setBackground(button, ContextCompat.getDrawable(getContext(), outValue.resourceId));
    }

    private void styleDetailsPanel() {
        detailsPanel.setRadius(cornerRadius);
        detailsPanel.setCardElevation(4);
        detailsPanel.setCardBackgroundColor(Color.WHITE);
        detailsPanel.setStrokeWidth(strokeWidth);
        detailsPanel.setStrokeColor(primaryColor);

        tvSelectedDate.setTextColor(primaryDarkColor);
        tvSelectedDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

        tvDayName.setTextColor(accentColor);
        tvDayName.setTypeface(font);
        tvDayName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
    }

    private void styleAddButton() {

    }

    private void setupListeners() {
        setupNavigationListeners();
    }

    private void setupNavigationListeners() {
        btnPrevYear.setOnClickListener(v -> navigateYear(-1));
        btnNextYear.setOnClickListener(v -> navigateYear(1));
        btnPrevMonth.setOnClickListener(v -> navigateMonth(-1));
        btnNextMonth.setOnClickListener(v -> navigateMonth(1));
    }

    private void navigateYear(int direction) {
        animateButtonClick(direction > 0 ? btnNextYear : btnPrevYear);
        currentCalendar.add(Calendar.YEAR, direction);
        animateCalendarTransition(direction > 0);
    }

    private void navigateMonth(int direction) {
        animateButtonClick(direction > 0 ? btnNextMonth : btnPrevMonth);
        currentCalendar.add(Calendar.MONTH, direction);
        animateCalendarTransition(direction > 0);
    }

    private void updateCalendar() {
        tvMonthYear.setText(monthYearFormat.format(currentCalendar.getTime()).toUpperCase(Locale.forLanguageTag("tr")));

        Calendar tempCalendar = (Calendar) currentCalendar.clone();
        tempCalendar.set(Calendar.DAY_OF_MONTH, 1);
        tempCalendar.setFirstDayOfWeek(Calendar.MONDAY);

        int firstDayOfWeek = tempCalendar.get(Calendar.DAY_OF_WEEK);
        int offset = (firstDayOfWeek == Calendar.SUNDAY) ? 6 : firstDayOfWeek - Calendar.MONDAY;

        calendarGrid.removeAllViews();
        addDayNames();
        addCalendarDays(tempCalendar, offset);

        updateAddButtonVisibility();
    }

    private void addDayNames() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        for (int i = 0; i < 7; i++) {
            MaterialCardView dayCard = createDayCard();
            TextView dayName = new TextView(getContext());

            dayName.setTypeface(font);

            dayName.setText(shortDayNameFormat.format(cal.getTime()));
            dayName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            dayName.setTextColor(primaryDarkColor);
            dayName.setGravity(Gravity.CENTER);

            dayCard.setCardBackgroundColor(Color.TRANSPARENT);
            dayCard.addView(dayName);
            calendarGrid.addView(dayCard);

            cal.add(Calendar.DAY_OF_WEEK, 1);
        }
    }

    private void addCalendarDays(Calendar tempCalendar, int offset) {
        for (int i = 0; i < DAYS_COUNT; i++) {
            int dayNumber = i - offset + 1;
            Calendar dayCalendar = (Calendar) tempCalendar.clone();

            MaterialCardView dayCard = createDayCard();
            LinearLayout dayLayout = createDayLayout();
            TextView dayNumberView = createDayNumberView();
            TextView dayNameView = createDayNameView();

            if (dayNumber < 1 || dayNumber > tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                setupEmptyDay(dayCard);
            } else {
                dayCalendar.set(Calendar.DAY_OF_MONTH, dayNumber);
                setupValidDay(dayCard, dayLayout, dayNumberView, dayNameView, dayCalendar);
            }
            calendarGrid.addView(dayCard);
        }
    }

    private MaterialCardView createDayCard() {
        MaterialCardView dayCard = new MaterialCardView(getContext());
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = dayCellSize;
        params.height = dayCellSize;
        params.setMargins(5, 5, 5, 5);
        dayCard.setLayoutParams(params);
        dayCard.setRadius(cornerRadius);
        dayCard.setCardElevation(2);
        return dayCard;
    }

    private LinearLayout createDayLayout() {
        LinearLayout dayLayout = new LinearLayout(getContext());
        dayLayout.setOrientation(LinearLayout.VERTICAL);
        dayLayout.setGravity(Gravity.CENTER);
        dayLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        return dayLayout;
    }

    private TextView createDayNumberView() {
        TextView dayNumberView = new TextView(getContext());
        dayNumberView.setTypeface(font);
        dayNumberView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        dayNumberView.setGravity(Gravity.CENTER);
        dayNumberView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        dayNumberView.setPadding(0, 0, 0, 0);
        return dayNumberView;
    }

    private TextView createDayNameView() {
        TextView dayNameView = new TextView(getContext());
        dayNameView.setTypeface(font);
        dayNameView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        dayNameView.setGravity(Gravity.CENTER);
        dayNameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        dayNameView.setPadding(0, 0, 0, 0);
        return dayNameView;
    }

    private void setupEmptyDay(MaterialCardView dayCard) {
        dayCard.setCardBackgroundColor(Color.TRANSPARENT);
        dayCard.setStrokeWidth(0);
        dayCard.setVisibility(View.INVISIBLE);
    }

    private void setupValidDay(MaterialCardView dayCard, LinearLayout dayLayout,
                               TextView dayNumberView, TextView dayNameView,
                               Calendar calendar) {
        int dayNumber = calendar.get(Calendar.DAY_OF_MONTH);
        dayNumberView.setText(String.valueOf(dayNumber));
        dayNameView.setText(shortDayNameFormat.format(calendar.getTime()));

        String dateStr = sdf.format(calendar.getTime());
        boolean isPast = calendar.before(today) && !isSameDay(calendar, today);
        boolean isToday = isSameDay(calendar, today);
        boolean isFuture = calendar.after(today);
        boolean hasAppointment = appointmentMap.containsKey(dateStr) && !appointmentMap.get(dateStr).isEmpty();
        boolean isSelected = selectedDay != null && isSameDay(calendar, selectedDay);

        DayAppearance appearance = calculateDayAppearance(isSelected, isToday, isPast, isFuture, hasAppointment);

        applyDayAppearance(dayCard, dayNumberView, dayNameView, appearance);

        final Calendar dayCalendar = (Calendar) calendar.clone();
        dayCard.setOnClickListener(v -> handleDayClick(dayCalendar, dayCard));

        dayLayout.addView(dayNumberView);
        dayLayout.addView(dayNameView);
        dayCard.addView(dayLayout);
        dayCard.setVisibility(View.VISIBLE);
    }

    private DayAppearance calculateDayAppearance(boolean isSelected, boolean isToday,
                                                 boolean isPast, boolean isFuture,
                                                 boolean hasAppointment) {
        DayAppearance appearance = new DayAppearance();

        if (isSelected) {
            appearance.bgColor = accentColor;
            appearance.textColor = Color.WHITE;
            appearance.dayNameColor = Color.WHITE;
            appearance.strokeColor = accentColor;
            appearance.hasStroke = true;
        } else if (isToday) {
            appearance.bgColor = Color.argb(50, 144, 202, 249);
            appearance.strokeColor = Color.parseColor("#90CAF9");
            appearance.dayNameColor = Color.parseColor("#0D47A1");
            appearance.hasStroke = true;
        } else if (isPast) {
            if (hasAppointment) {
                appearance.bgColor = Color.argb(50, 255, 138, 128);
                appearance.strokeColor = Color.parseColor("#FF8A80");
                appearance.dayNameColor = Color.parseColor("#D32F2F");
                appearance.hasStroke = true;
            } else {
                appearance.textColor = disabledTextColor;
            }
        } else if (isFuture && hasAppointment) {
            appearance.bgColor = Color.argb(50, 76, 175, 80);
            appearance.strokeColor = appointmentColor;
            appearance.dayNameColor = Color.parseColor("#2E7D32");
            appearance.hasStroke = true;
        }

        return appearance;
    }

    private void applyDayAppearance(MaterialCardView dayCard, TextView dayNumberView,
                                    TextView dayNameView, DayAppearance appearance) {
        dayCard.setCardBackgroundColor(appearance.bgColor);
        dayCard.setStrokeColor(appearance.strokeColor);
        dayCard.setStrokeWidth(appearance.hasStroke ? strokeWidth : 0);
        dayNumberView.setTextColor(appearance.textColor);
        dayNameView.setTextColor(appearance.dayNameColor);
    }

    private static class DayAppearance {
        int bgColor = Color.TRANSPARENT;
        int textColor = Color.BLACK;
        int dayNameColor = Color.GRAY;
        int strokeColor = Color.TRANSPARENT;
        boolean hasStroke = false;
    }

    private void handleDayClick(Calendar dayCalendar, MaterialCardView dayCard) {
        if (selectedDay != null && isSameDay(dayCalendar, selectedDay)) {
            selectedDay = null;
            detailsPanel.setVisibility(View.GONE);
        } else {
            selectedDay = (Calendar) dayCalendar.clone();
            showDayDetails(dayCalendar);
        }
        updateCalendar();
        animateDaySelection(dayCard);
    }

    private void showDayDetails(Calendar day) {
        String dateStr = sdf.format(day.getTime());
        String displayDate = displayDateFormat.format(day.getTime());
        String dayName = dayNameFormat.format(day.getTime());

        tvSelectedDate.setText(displayDate);
        tvSelectedDate.setTypeface(font);
        tvDayName.setText(dayName);

        appointmentsContainer.removeAllViews();

        if (appointmentMap.containsKey(dateStr)) {
            displayAppointments(dateStr);
        } else {
            displayNoAppointments();
        }

        updateAddButtonVisibility();
        animateDetailsPanelIfNeeded();
    }

    private void displayAppointments(String dateStr) {
        List<Appointment> appointments = appointmentMap.get(dateStr);
        if (appointments != null && !appointments.isEmpty()) {
            for (Appointment appointment : appointments) {
                addAppointmentToDetailsPanel(appointment);
            }
        } else {
            displayNoAppointments();
        }
    }

    private void displayNoAppointments() {
        TextView noAppointmentText = new TextView(getContext());
        noAppointmentText.setTypeface(font);
        noAppointmentText.setText("Randevu yok");
        noAppointmentText.setTextColor(disabledTextColor);
        noAppointmentText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        appointmentsContainer.addView(noAppointmentText);
    }

    private void updateAddButtonVisibility() {

    }

    private void animateDetailsPanelIfNeeded() {
        if (detailsPanel.getVisibility() != View.VISIBLE) {
            animateDetailsPanel();
        }
    }

    private void addAppointmentToDetailsPanel(Appointment appointment) {
        MaterialCardView appointmentCard = new MaterialCardView(getContext());
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, 8);
        appointmentCard.setLayoutParams(cardParams);
        appointmentCard.setRadius(cornerRadius);
        appointmentCard.setCardBackgroundColor(Color.WHITE);
        appointmentCard.setStrokeWidth(strokeWidth);
        appointmentCard.setStrokeColor(appointmentColor);

        TextView appointmentText = new TextView(getContext());
        appointmentText.setTypeface(font);
        appointmentText.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        appointmentText.setText(appointment.toString());
        appointmentText.setTextColor(textColor);
        appointmentText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        appointmentText.setPadding(16, 16, 16, 16);

        appointmentCard.addView(appointmentText);
        appointmentCard.setOnClickListener(v -> {
            if (appointmentClickListener != null) {
                appointmentClickListener.onAppointmentClick(appointment);
            }
        });

        appointmentsContainer.addView(appointmentCard);
    }

    private void animateDetailsPanel() {
        detailsPanel.setVisibility(View.VISIBLE);
        detailsPanel.setAlpha(0f);
        detailsPanel.setTranslationY(20);

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(detailsPanel, "alpha", 0f, 1f);
        ObjectAnimator slideUp = ObjectAnimator.ofFloat(detailsPanel, "translationY", 20, 0);

        fadeIn.setDuration(300);
        slideUp.setDuration(300);
        slideUp.setInterpolator(new OvershootInterpolator(0.7f));

        fadeIn.start();
        slideUp.start();
    }

    private void animateButtonClick(View view) {
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 0.9f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 0.9f);
        scaleDownX.setDuration(100);
        scaleDownY.setDuration(100);

        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 1.1f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 1.1f);
        scaleUpX.setDuration(150);
        scaleUpY.setDuration(150);

        ObjectAnimator scaleNormalX = ObjectAnimator.ofFloat(view, "scaleX", 1f);
        ObjectAnimator scaleNormalY = ObjectAnimator.ofFloat(view, "scaleY", 1f);
        scaleNormalX.setDuration(100);
        scaleNormalY.setDuration(100);

        scaleDownX.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                scaleUpX.start();
                scaleUpY.start();
            }
        });

        scaleUpX.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                scaleNormalX.start();
                scaleNormalY.start();
            }
        });

        scaleDownX.start();
        scaleDownY.start();
    }

    private void animateCalendarTransition(boolean forward) {
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(calendarGrid, "alpha", 1f, 0f);
        ObjectAnimator slideOut = ObjectAnimator.ofFloat(calendarGrid, "translationX",
                forward ? 0 : -50f, forward ? 50f : 0);
        fadeOut.setDuration(200);
        slideOut.setDuration(200);

        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                updateCalendar();

                calendarGrid.setTranslationX(forward ? -50f : 50f);
                calendarGrid.setAlpha(0f);

                ObjectAnimator fadeIn = ObjectAnimator.ofFloat(calendarGrid, "alpha", 0f, 1f);
                ObjectAnimator slideIn = ObjectAnimator.ofFloat(calendarGrid, "translationX",
                        forward ? -50f : 50f, 0);

                fadeIn.setDuration(200);
                slideIn.setDuration(200);
                fadeIn.setInterpolator(new AccelerateDecelerateInterpolator());
                slideIn.setInterpolator(new OvershootInterpolator(0.5f));

                fadeIn.start();
                slideIn.start();
            }
        });

        fadeOut.start();
        slideOut.start();
    }

    private void animateDaySelection(View dayView) {
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(dayView, "scaleX", 0.9f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(dayView, "scaleY", 0.9f);
        scaleDownX.setDuration(100);
        scaleDownY.setDuration(100);

        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(dayView, "scaleX", 1.1f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(dayView, "scaleY", 1.1f);
        scaleUpX.setDuration(150);
        scaleUpY.setDuration(150);

        ObjectAnimator scaleNormalX = ObjectAnimator.ofFloat(dayView, "scaleX", 1f);
        ObjectAnimator scaleNormalY = ObjectAnimator.ofFloat(dayView, "scaleY", 1f);
        scaleNormalX.setDuration(100);
        scaleNormalY.setDuration(100);

        scaleDownX.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                scaleUpX.start();
                scaleUpY.start();
            }
        });

        scaleUpX.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                scaleNormalX.start();
                scaleNormalY.start();
            }
        });

        scaleDownX.start();
        scaleDownY.start();
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    public void setOnAddAppointmentListener(OnAddAppointmentListener listener) {
        this.addAppointmentListener = listener;
    }

    public void setOnAppointmentClickListener(OnAppointmentClickListener listener) {
        this.appointmentClickListener = listener;
    }

    public void addAppointment(Appointment appointment) {
        String date = appointment.getDate();
        if (!appointmentMap.containsKey(date)) {
            appointmentMap.put(date, new ArrayList<>());
        }
        appointmentMap.get(date).add(appointment);
        post(() -> {
            updateCalendar();
            if (selectedDay != null && sdf.format(selectedDay.getTime()).equals(date)) {
                showDayDetails(selectedDay);
            }
        });
    }

    public void removeAppointment(Appointment appointment) {
        String date = appointment.getDate();
        if (appointmentMap.containsKey(date)) {
            appointmentMap.get(date).remove(appointment);
            if (appointmentMap.get(date).isEmpty()) {
                appointmentMap.remove(date);
            }
            post(() -> {
                updateCalendar();
                if (selectedDay != null && sdf.format(selectedDay.getTime()).equals(date)) {
                    showDayDetails(selectedDay);
                }
            });
        }
    }

    public void refreshAppointments() {
        loadAppointmentsFromDatabase();
    }

    public String getSelectedDate() {
        return selectedDay != null ? sdf.format(selectedDay.getTime()) : "";
    }

    public void setSelectedDate(int year, int month, int day) {
        currentCalendar.set(year, month, day);
        selectedDay = (Calendar) currentCalendar.clone();
        updateCalendar();
        showDayDetails(selectedDay);
    }

    public void clearSelection() {
        selectedDay = null;
        updateCalendar();
        detailsPanel.setVisibility(View.GONE);
    }

    public List<Appointment> getAppointmentsForDate(String date) {
        return appointmentMap.getOrDefault(date, new ArrayList<>());
    }
}