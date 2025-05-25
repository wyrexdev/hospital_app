package com.hospital.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;

public class Login extends AppCompatActivity {

    TextView hhgy, th, vkgy, bhy, register;
    EditText password, email;
    Button login;
    LinearLayout google, facebook;

    ProgressDialog coreprog;

    BaseHttpClient client;

    SharedPreferences sharedPreferences;

    GoogleSignInClient mGoogleSignInClient;
    ActivityResultLauncher<Intent> googleSignInLauncher;


    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        hideSystemUI();

        client = new BaseHttpClient();

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        sharedPreferences = getSharedPreferences("User", Context.MODE_PRIVATE);

        hhgy = findViewById(R.id.hhgy);
        th = findViewById(R.id.th);
        vkgy = findViewById(R.id.vkgy);
        bhy = findViewById(R.id.bhy);
        register = findViewById(R.id.register);

        password = findViewById(R.id.password);
        email = findViewById(R.id.email);

        login = findViewById(R.id.login);

        google = findViewById(R.id.google);
        facebook = findViewById(R.id.facebook);

        Typeface nunitoBold = Typeface.createFromAsset(getAssets(), "Nunito-Bold.ttf");

        hhgy.setTypeface(nunitoBold);
        th.setTypeface(nunitoBold);
        vkgy.setTypeface(nunitoBold);
        bhy.setTypeface(nunitoBold);
        register.setTypeface(nunitoBold);

        email.setTypeface(nunitoBold);
        password.setTypeface(nunitoBold);

        login.setTypeface(nunitoBold);

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Log.e("GOOGLE", result.toString());
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        handleGoogleSignInResult(task);
                    }
                }
        );

        ViewUtils.rippleRoundStroke(google, "#ffffff", "#f2f2f2", 25, 0, "#000000");
        ViewUtils.rippleRoundStroke(facebook, "#ffffff", "#f2f2f2", 25, 0, "#000000");

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLoader(true);

                String emailV = email.getText().toString();
                String passwordV = password.getText().toString();

                if(emailV.isBlank() || emailV.isEmpty() || emailV.toString().equals("") ||
                        passwordV.isBlank() || passwordV.isEmpty() || passwordV.toString().equals("")) {
                    openLoader(false);
                    ModernToast.showCustomToast(Login.this, "Lütfen tüm bilgileri doldurduğunuzdan emin olun!");
                } else {
                    JSONObject jsonBody = new JSONObject();
                    try {
                        jsonBody.put("email", emailV);
                        jsonBody.put("password", passwordV);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    client.post("/api/v1/login", jsonBody, new BaseHttpClient.JsonResponseCallback() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            try {
                                int status = response.getInt("status");

                                if(status == 429) {
                                    ModernToast.showCustomToast(Login.this, "Çok fazla başarısız giriş denemesi. Lütfen 30 dakika içerisinde tekrar deneyiniz.");
                                    openLoader(false);
                                } else if(status == 200) {
                                    String token = response.getJSONObject("user").getString("token");

                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("token", token);
                                    editor.apply();

                                    boolean allCompleted = response.has("allCompleted") && response.getBoolean("allCompleted");
                                    boolean isVerify = response.has("verify") && response.getBoolean("verify");

                                    if(isVerify) {
                                        if(allCompleted) {
                                            Intent intent = new Intent();
                                            intent.setClass(getApplicationContext(), Dashboard.class);
                                            startActivity(intent);
                                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                            finish();
                                        } else {
                                            Intent intent = new Intent();
                                            intent.setClass(getApplicationContext(), PersonalSetup.class);
                                            startActivity(intent);
                                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                            finish();
                                        }
                                    } else {
                                        Intent intent = new Intent();
                                        intent.setClass(getApplicationContext(), VerifyMail.class);
                                        startActivity(intent);
                                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                        finish();
                                    }

                                    openLoader(false);
                                } else {
                                    ModernToast.showCustomToast(Login.this, "Lütfen bilgilerinizi kontrol edin ve tekrar deneyin.");
                                    openLoader(false);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.e("API_RESPONSE", "JSON parsing error: " + e.getMessage());
                                openLoader(false);
                            }
                        }

                        @Override
                        public void onSuccess(JSONArray arrayResponse) {

                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("API_POST_ERROR", e.getMessage());
                        }
                    });
                }
            }
        });

        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestIdToken("646014604044-mljhroi6vqgivg4eafkqt9c0sp9f1lfl.apps.googleusercontent.com")
                        .build();

                GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(Login.this, gso);

                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                googleSignInLauncher.launch(signInIntent);
            }
        });

        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logInWithReadPermissions(Login.this, Arrays.asList("email", "public_profile"));
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setClass(getApplicationContext(), Register.class);
                startActivity(i);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        });

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken accessToken = loginResult.getAccessToken();
                String token = accessToken.getToken();
            }

            @Override
            public void onCancel() {
                Log.d("Facebook", "Facebook Login Canceled");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("Facebook", "Facebook Login Error: " + error.getMessage());
            }
        });

        // ViewUtils.rippleRoundStroke(facebook, "#232323", "#333232", 25, 0, "#000000");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
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

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            openLoader(true);

            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            String email = account.getEmail();
            String idToken = account.getIdToken();

            JSONObject data = new JSONObject();

            try {
                data.put("email", email);
                data.put("token", idToken);
            } catch (Exception e) {
                Log.e("JSONObject Error", e.getMessage());
            }

            client.post("/api/v1/platform/0", data, new BaseHttpClient.JsonResponseCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    try {
                        int status = response.getInt("status");

                        if(status == 200) {
                            String token = response.getJSONObject("user").getString("token");

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("token", token);
                            editor.apply();

                            boolean allCompleted = response.has("allCompleted") && response.getBoolean("allCompleted");

                            if(allCompleted) {
                                Intent intent = new Intent();
                                intent.setClass(getApplicationContext(), Dashboard.class);
                                startActivity(intent);
                                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                finish();
                            } else {
                                Intent intent = new Intent();
                                intent.setClass(getApplicationContext(), PersonalSetup.class);
                                startActivity(intent);
                                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                finish();
                            }

                            openLoader(false);
                        } else if (status == 429) {
                            ModernToast.showCustomToast(Login.this, "Çok fazla giriş denemesi. Lütfen 30 dakika içerisinde tekrar deneyiniz.");
                            openLoader(false);
                        } else if(status == 2002) {
                            ModernToast.showCustomToast(Login.this, "Bu Google hesabına bağlı herhangi bir hesap bulunamadı.");
                            openLoader(false);
                        } else if(status == 2001) {
                            ModernToast.showCustomToast(Login.this, "Geçersiz Google girişi.");
                            openLoader(false);
                        } else {
                            ModernToast.showCustomToast(Login.this, "Görünüşe göre bizden kaynaklı bir sorun var. Lütfen daha sonra tekrar deneyiniz. Düzelmediği takdirde bizimle iletişime geçmeyi deneyebilirsiniz.");
                            openLoader(false);
                        }
                    } catch (Exception e) {
                        Log.e("API Success Error: ", e.getMessage());
                        ModernToast.showCustomToast(Login.this, "Görünüşe göre bizden kaynaklı bir sorun var. Lütfen daha sonra tekrar deneyiniz. Düzelmediği takdirde bizimle iletişime geçmeyi deneyebilirsiniz.");
                        openLoader(false);
                    }
                }

                @Override
                public void onSuccess(JSONArray arrayResponse) {

                }

                @Override
                public void onError(Exception e) {
                    Log.e("API Error:", e.getMessage());
                    ModernToast.showCustomToast(Login.this, "Görünüşe göre bizden kaynaklı bir sorun var. Lütfen daha sonra tekrar deneyiniz. Düzelmediği takdirde bizimle iletişime geçmeyi deneyebilirsiniz.");
                    openLoader(false);
                }
            });
        } catch (ApiException e) {
            Log.w("GOOGLE", "signInResult:failed code=" + e.getStatusCode());
        }
    }
}