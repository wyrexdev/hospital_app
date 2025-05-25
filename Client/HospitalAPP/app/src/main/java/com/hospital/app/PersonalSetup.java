package com.hospital.app;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class PersonalSetup extends AppCompatActivity {

    public static TextView prek, gender, ss;
    LinearLayout save, selectPP;
    EditText name, surname, tc, birthdate, allergy, chronic, disability;
    ImageView pp;

    String[] genderOptions = {"Erkek", "Kadın", "Diğer"};

    String nameS, surnameS, tcS, birthdateS, allergyS, chronicS, disabilityS;
    public static int genderS = 9, ssS = 9;

    BaseHttpClient client;

    ProgressDialog coreprog;

    Uri selectedImageUri;
    File imageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_personal_setup);

        hideSystemUI();

        client = new BaseHttpClient();

        prek = findViewById(R.id.prek);
        gender = findViewById(R.id.gender);
        ss = findViewById(R.id.ss);
        save = findViewById(R.id.save);

        name = findViewById(R.id.name);
        surname = findViewById(R.id.surname);
        tc = findViewById(R.id.tc);
        birthdate = findViewById(R.id.birthdate);
        allergy = findViewById(R.id.allergy);
        chronic = findViewById(R.id.chronic);
        disability = findViewById(R.id.disability);

        pp = findViewById(R.id.pp);
        selectPP = findViewById(R.id.selectPp);

        selectPP.setOnClickListener(v -> openFileChooser());

        Typeface nunitoBold = Typeface.createFromAsset(getAssets(), "Nunito-Bold.ttf");

        prek.setTypeface(nunitoBold);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLoader(true);

                nameS = name.getText().toString();
                surnameS = surname.getText().toString();
                tcS = tc.getText().toString();
                birthdateS = birthdate.getText().toString();
                allergyS = allergy.getText().toString().isEmpty() ? "" : allergy.getText().toString();
                chronicS = chronic.getText().toString().isEmpty() ? "" : chronic.getText().toString();
                disabilityS = disability.getText().toString().isEmpty() ? "" : disability.getText().toString();

                boolean isName = !nameS.isBlank();
                boolean isSurname = !surnameS.isBlank();
                boolean isTC = !tcS.isBlank();
                boolean isBirthdate = !birthdateS.isBlank();
                boolean isAllergy = !allergyS.isBlank();
                boolean isChronic = !chronicS.isBlank();
                boolean isDisability = !disabilityS.isBlank();

                boolean isGender = (genderS == 0 || genderS == 1 || genderS == 2);
                boolean isSS = (ssS == 0 || ssS == 1);

                if (isName && isSurname && isTC && isBirthdate && isGender && isSS) {
                    JSONObject data = new JSONObject();

                    try {
                        String id = getSharedPreferences("User", Context.MODE_PRIVATE).getString("token", "");

                        data.put("id", id);
                        data.put("name", nameS);
                        data.put("surname", surnameS);
                        data.put("tc", tcS);
                        data.put("birthDate", birthdateS);
                        data.put("allergies", allergyS);
                        data.put("chronic_diseases", chronicS);
                        data.put("disability_status", disabilityS);
                        data.put("gender", genderS);
                        data.put("insurance_type", ssS);
                    } catch (Exception e) {
                        openLoader(false);
                        Log.e("JSON Error: ", e.getMessage().toString());
                    }

                    NetworkUtils.uploadJsonAndFileAsync("https://kisetsuna.com/api/v1/update", data, imageFile, "pp", new NetworkUtils.OnSuccessCallback() {
                                @Override
                                public void onSuccess(JSONObject response) {
                                    System.out.println("Başarılı! JSON response: " + response.toString());

                                    try {
                                        int status = response.getInt("status");

                                        if(status == 200) {
                                            boolean allCompleted = response.getBoolean("allCompleted");

                                            if(allCompleted) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Intent i = new Intent();
                                                        i.setClass(getApplicationContext(), Dashboard.class);
                                                        startActivity(i);
                                                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                                        finish();
                                                    }
                                                });
                                            } else {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        ModernToast.showCustomToast(PersonalSetup.this, "Tüm bilgileri doldurduğunuzdan emin olun! Bir hata olduğunu düşünüyorsanız bizimle iletişime geçin.");
                                                    }
                                                });
                                            }

                                            openLoader(false);
                                        } else if(status == 9001) {
                                            openLoader(false);
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    ModernToast.showCustomToast(PersonalSetup.this, "TC Kimlik numaranız girdiğiniz bilgiler ile eşleşmiyor!");
                                                }
                                            });
                                        } else if(status == 9002) {
                                            openLoader(false);
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    ModernToast.showCustomToast(PersonalSetup.this, "TC Kimlik numaranız girdiğiniz bilgiler ile eşleşmiyor! Bir hata olduğunu düşünüyorsanız bizimle iletişime geçiniz.");
                                                }
                                            });
                                        } else {
                                            openLoader(false);
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    ModernToast.showCustomToast(PersonalSetup.this, "Bir hata meydana geldi.");
                                                }
                                            });
                                        }
                                    } catch (Exception e) {
                                        openLoader(false);
                                        Log.e("API Error: ", e.getMessage().toString());
                                    }
                                }
                            },
                            new NetworkUtils.OnErrorCallback() {
                                @Override
                                public void onError(String error) {
                                    openLoader(false);
                                    System.err.println("API Error: " + error);
                                }
                            });
                } else {
                    openLoader(false);
                    ModernToast.showCustomToast(PersonalSetup.this, "Lütfen gerekli alanları doldurduğunuzdan emin olun.");
                }
            }
        });

        ViewUtils.rippleRoundStroke(save, "#ffffff", "#f2f2f2", 100, 0, "#000000");

        gender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheetGender();
            }
        });

        ss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheetSS();
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

    private void showBottomSheetSS() {
        BottomSheetFragmentSS bottomSheetFragment = new BottomSheetFragmentSS();
        bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
    }

    private void showBottomSheetGender() {
        BottomSheetFragment bottomSheetFragment = new BottomSheetFragment();
        bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
    }

    public static class BottomSheetFragment extends BottomSheetDialogFragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.bottom_sheet_gender, container, false);

            TextView man = view.findViewById(R.id.man);
            TextView woman = view.findViewById(R.id.woman);
            TextView other = view.findViewById(R.id.other);

            ViewUtils.rippleRoundStroke(man, "#ffffff", "#f2f2f2", 100, 0, "#000000");
            ViewUtils.rippleRoundStroke(woman, "#ffffff", "#f2f2f2", 100, 0, "#000000");
            ViewUtils.rippleRoundStroke(other, "#ffffff", "#f2f2f2", 100, 0, "#000000");

            man.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gender.setText("Erkek");
                    genderS = 0;
                    dismiss();
                }
            });

            woman.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gender.setText("Kadın");
                    genderS = 1;
                    dismiss();
                }
            });

            other.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gender.setText("Diğer");
                    genderS = 2;
                    dismiss();
                }
            });

            return view;
        }


        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

            dialog.setOnShowListener(dialogInterface -> {
                BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
                FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);

                if (bottomSheet != null) {
                    bottomSheet.setBackground(new ColorDrawable(Color.TRANSPARENT));
                }

                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.getWindow().setDimAmount(0f);
                }
            });

            return dialog;
        }
    }

    public static class BottomSheetFragmentSS extends BottomSheetDialogFragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.bottom_sheet_ss, container, false);

            TextView sgk = view.findViewById(R.id.sgk);
            TextView priv = view.findViewById(R.id.priv);

            ViewUtils.rippleRoundStroke(sgk, "#ffffff", "#f2f2f2", 100, 0, "#000000");
            ViewUtils.rippleRoundStroke(priv, "#ffffff", "#f2f2f2", 100, 0, "#000000");

            sgk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ss.setText("SGK");
                    ssS = 0;
                    dismiss();
                }
            });

            priv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ss.setText("Özel");
                    ssS = 1;
                    dismiss();
                }
            });

            return view;
        }


        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

            dialog.setOnShowListener(dialogInterface -> {
                BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
                FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);

                if (bottomSheet != null) {
                    bottomSheet.setBackground(new ColorDrawable(Color.TRANSPARENT));
                }

                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.getWindow().setDimAmount(0f);
                }
            });

            return dialog;
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

    private static final int PICK_IMAGE_REQUEST = 1;

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Resim Seç"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();

            pp.setImageURI(selectedImageUri);

            imageFile = getFileFromUri(selectedImageUri);
        }
    }

    private File getFileFromUri(Uri uri) {
        File file = null;
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            String fileName = "selected_image_" + System.currentTimeMillis() + ".jpg";

            file = new File(getCacheDir(), fileName);
            FileOutputStream outputStream = new FileOutputStream(file);

            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
            }

            outputStream.close();
            inputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return file;
    }
}
