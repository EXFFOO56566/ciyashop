package com.example.ciyashop.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.drawable.DrawableCompat;

import com.bumptech.glide.Glide;
import com.ciyashop.library.apicall.PostApi;
import com.ciyashop.library.apicall.URLS;
import com.ciyashop.library.apicall.interfaces.OnResponseListner;
import com.example.ciyashop.R;
import com.example.ciyashop.customview.edittext.EditTextMedium;
import com.example.ciyashop.customview.edittext.EditTextRegular;
import com.example.ciyashop.databinding.ActivityLogInBinding;
import com.example.ciyashop.model.LogIn;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;
import com.example.ciyashop.utils.Utils;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LogInActivity extends BaseActivity implements OnResponseListner {

    private static final String TAG = LogInActivity.class.getSimpleName();

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    AlertDialog alertDialog;
    AlertDialog alertDialog1;

    CallbackManager callbackManager;
    private ActivityLogInBinding binding;

    /* private GoogleApiClient mGoogleApiClient;*/

    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private String pin, email, password;
    private String faceBookImageUrl;
    private JSONObject fbJsonObject;
    private Bundle bundle;
    private boolean isSplash = false;

    //ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLogInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();
        setCLickEvent();
        setScreenLayoutDirection();
        loginWithFB();
        setColor();
        getIntentData();

        if (Constant.APPLOGO != null && !Constant.APPLOGO.equals("")) {
            Glide.with(this).load(Constant.APPLOGO).error(R.drawable.logo).into(binding.ivLogo);
        }

        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        binding.ivBlackBackButton.setOnClickListener(v -> finish());
    }

    public void getIntentData() {
        bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey("is_splash")) {
            isSplash = bundle.getBoolean("is_splash");
        }
    }

    public void setColor() {
        binding.tvNewUser.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        Drawable unwrappedDrawable = binding.tvSignIn.getBackground();
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, (Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR))));
        setTextViewDrawableColor(binding.etEmail, (Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR))));
        setTextViewDrawableColor(binding.etPass, (Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR))));
        binding.tvForgetPass.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.ivBlackBackButton.setColorFilter((Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR))));
    }

    public void setCLickEvent() {
        binding.tvSignIn.setOnClickListener(v -> {
            if (binding.etEmail.getText().toString().isEmpty()) {
                Toast.makeText(this, R.string.enter_email_address, Toast.LENGTH_SHORT).show();
            } else {
                if (Utils.isValidEmail(binding.etEmail.getText().toString())) {
                    if (binding.etPass.getText().toString().isEmpty()) {
                        Toast.makeText(this, R.string.enter_password, Toast.LENGTH_SHORT).show();
                    } else {
                        userLogin();
                    }
                } else {
                    Toast.makeText(this, R.string.enter_valid_email_address, Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.ivLoginWithGoogle.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            signIn();
        });

        binding.ivLoginWithFacebook.setOnClickListener(v -> {
            LoginManager.getInstance().logOut();

            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList(RequestParamUtils.email, RequestParamUtils.publicProfile));
        });

        binding.tvNewUser.setOnClickListener(v -> {
            Intent intent = new Intent(LogInActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        binding.tvForgetPass.setOnClickListener(v ->
                showForgetPassDialog()
        );
    }

    // User Login
    public void userLogin() {
        if (Utils.isInternetConnected(this)) {
            showProgress("");
            PostApi postApi = new PostApi(LogInActivity.this, RequestParamUtils.login, this, getlanuage());
            JSONObject object = new JSONObject();
            try {
                object.put(RequestParamUtils.email, binding.etEmail.getText().toString());
                object.put(RequestParamUtils.PASSWORD, binding.etPass.getText().toString());
                object.put(RequestParamUtils.deviceType, "2");
                String token = getPreferences().getString(RequestParamUtils.NOTIFICATION_TOKEN, "");
                object.put(RequestParamUtils.deviceToken, token);
                postApi.callPostApi(new URLS().LOGIN, object.toString());
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        GoogleLoginActivityResult.launch(signInIntent);
    }

    private void handleSignInResult(GoogleSignInAccount result) {
        AuthCredential credential = GoogleAuthProvider.getCredential(result.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
//                        FirebaseUser user = mAuth.getCurrentUser();
                        String personName = result.getDisplayName();
                        String email = result.getEmail();
                        Log.e("hi", "Name: " + personName + ", email: " + email);
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put(RequestParamUtils.socialId, result.getId());
                            jsonObject.put(RequestParamUtils.email, result.getEmail());
                            jsonObject.put(RequestParamUtils.firstName, result.getGivenName());
                            jsonObject.put(RequestParamUtils.lastName, result.getFamilyName());
                            socialLogin(jsonObject);
                        } catch (Exception e) {
                            Log.e("error", e.getMessage());
                        }
                    } else {
                        Log.e(TAG, "signInWithCredential:failure", task.getException());
                    }
                });
    }

    public void loginWithFB() {
        callbackManager = CallbackManager.Factory.create();

        // If you are using in a fragment, call loginButton.setFragment(this);

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
                        String accessToken = loginResult.getAccessToken()
                                .getToken();
                        Log.e("accessToken", accessToken);

                        GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                                (object, response) -> {
                                    Log.e("LoginActivity", response.toString());
                                    try {
                                        String id = object.getString("id");
                                        faceBookImageUrl = "https://graph.facebook.com/" + id + "/picture?type=large";
                                        fbJsonObject = object;
                                        getBitmapInAsync();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id, name, email, gender, first_name, last_name, picture.type(large)");
                        request.setParameters(parameters);
                        request.executeAsync();
                    }

                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                        Log.e(TAG, "Facebook on Error: " + exception.toString());
                    }
                });
    }

    private final Executor executor = Executors.newSingleThreadExecutor(); // change according to your requirements
    private final Handler handler = new Handler(Looper.getMainLooper());

    private void getBitmapInAsync() {
        executor.execute(() -> {
            String encodedBitmap = getBitmap();

            handler.post(() -> {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(RequestParamUtils.data, encodedBitmap);
                    jsonObject.put(RequestParamUtils.name, "image.jpg");

                    if (fbJsonObject.has(RequestParamUtils.gender)) {
                        Log.e("gender", fbJsonObject.getString(RequestParamUtils.gender));
                    }

                    fbJsonObject.put(RequestParamUtils.userImage, jsonObject);
                    fbJsonObject.put(RequestParamUtils.socialId, fbJsonObject.getString("id"));
                    socialLogin(fbJsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    public String getBitmap() {
        try {
            URL url = new URL(faceBookImageUrl);
            try {
                Bitmap mIcon = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                mIcon.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
                byte[] b = baos.toByteArray();
                return Base64.encodeToString(b, Base64.DEFAULT);
            } catch (IOException e) {
                Log.e("IOException ", e.getMessage());
            } catch (Exception e) {
                Log.e("Exception ", e.getMessage());
            }
            Log.e("Done", "Done");
        } catch (IOException e) {
            Log.e("Exception Url ", e.getMessage());
        }
        return null;
    }

    // Social Login API
    public void socialLogin(final JSONObject object) {
        if (Utils.isInternetConnected(LogInActivity.this)) {
            showProgress("");
            final PostApi postApi = new PostApi(LogInActivity.this, RequestParamUtils.socialLogin, LogInActivity.this, getlanuage());
            try {
                object.put(RequestParamUtils.deviceType, Constant.DEVICE_TYPE);
                String token = getPreferences().getString(RequestParamUtils.NOTIFICATION_TOKEN, "");
                object.put(RequestParamUtils.deviceToken, token);
                postApi.callPostApi(new URLS().SOCIAL_LOGIN, object.toString());
            } catch (Exception e) {
                Log.e("error", e.getMessage());
            }
        } else {
            Toast.makeText(LogInActivity.this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
        }
    }

    // this part was missing thanks to wesely

    ActivityResultLauncher<Intent> GoogleLoginActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        handleSignInResult(account);
                        Log.e(TAG, "firebaseAuthWithGoogle:" + account.getId());
                    } catch (ApiException e) {
                        Log.e(TAG, "Google sign in failed", e);
                    }
                }
            });

   /* @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                handleSignInResult(account);
                Log.e(TAG, "firebaseAuthWithGoogle:" + account.getId());
            } catch (ApiException e) {
                Log.e(TAG, "Google sign in failed", e);
            }
            *//*GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);*//*
        }
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (!getPreferences().getString(RequestParamUtils.ID, "").equals("")) {
            if (isSplash) {
                Intent intent = new Intent(LogInActivity.this, HomeActivity.class);
                startActivity(intent);
            }
            finish();
        }
    }

    public void showForgetPassDialog() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_forget_password, null);
        dialogBuilder.setView(dialogView);
        TextView tvRequestPasswordReset = dialogView.findViewById(R.id.tvRequestPasswordReset);

        //  tvRequestPasswordReset.setBackgroundColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));

        Drawable unwrappedDrawable = tvRequestPasswordReset.getBackground();
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, (Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR))));
        final EditTextRegular etForgetPassEmail = dialogView.findViewById(R.id.etForgetPassEmail);

        alertDialog1 = dialogBuilder.create();
        alertDialog1.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog1.show();

        tvRequestPasswordReset.setOnClickListener(view -> {
            if (etForgetPassEmail.getText().toString().isEmpty()) {
                Toast.makeText(LogInActivity.this, R.string.enter_email_address, Toast.LENGTH_SHORT).show();
            } else {
                if (Utils.isValidEmail(etForgetPassEmail.getText().toString())) {
                    email = etForgetPassEmail.getText().toString();
                    forgetPassword();
                } else {
                    Toast.makeText(LogInActivity.this, R.string.enter_valid_email_address, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void forgetPassword() {
        if (Utils.isInternetConnected(this)) {
            showProgress("");
            PostApi postApi = new PostApi(this, RequestParamUtils.forgotPassword, this, getlanuage());
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(RequestParamUtils.email, email);
                postApi.callPostApi(new URLS().FORGET_PASSWORD, jsonObject.toString());
            } catch (Exception e) {
                Log.e("error", e.getMessage());
            }
        } else {
            Toast.makeText(this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
        }
    }

    public void showSetPassDialog() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_forget_password_pin, null);
        dialogBuilder.setView(dialogView);

        final TextView tvSetNewPass = dialogView.findViewById(R.id.tvSetNewPass);
        //tvSetNewPass.setBackgroundColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        Drawable unwrappedDrawable = tvSetNewPass.getBackground();
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, (Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR))));

        final TextView tvNowEnterPass = dialogView.findViewById(R.id.tvNowEnterPass);
        final EditTextMedium etPin = dialogView.findViewById(R.id.etPin);
        final EditTextMedium etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        final EditTextMedium etConfirmNewPassword = dialogView.findViewById(R.id.etConfirrmNewPassword);

        alertDialog = dialogBuilder.create();

        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        alertDialog.show();

        etPin.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (etPin.getText().toString().equals(pin)) {
                    tvNowEnterPass.setVisibility(View.VISIBLE);
                    etNewPassword.setVisibility(View.VISIBLE);
                    etConfirmNewPassword.setVisibility(View.VISIBLE);
                }
            }
        });

        tvSetNewPass.setOnClickListener(view -> {
            if (etPin.getText().toString().isEmpty()) {
                Toast.makeText(LogInActivity.this, R.string.enter_pin, Toast.LENGTH_SHORT).show();
            } else {
                if (etPin.getText().toString().equals(pin)) {
                    if (etNewPassword.getText().toString().isEmpty()) {
                        Toast.makeText(LogInActivity.this, R.string.enter_new_password, Toast.LENGTH_SHORT).show();
                    } else {
                        if (etConfirmNewPassword.getText().toString().isEmpty()) {
                            Toast.makeText(LogInActivity.this, R.string.enter_confirm_password, Toast.LENGTH_SHORT).show();
                        } else {
                            if (etNewPassword.getText().toString().equals(etConfirmNewPassword.getText().toString())) {
                                //apicalls
                                password = etNewPassword.getText().toString();
                                updatePassword();
                            } else {
                                Toast.makeText(LogInActivity.this, R.string.password_and_confirm_password_not_matched, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                } else {
                    Toast.makeText(LogInActivity.this, R.string.enter_proper_detail, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void updatePassword() {
        if (Utils.isInternetConnected(this)) {
            showProgress("");
            PostApi postApi = new PostApi(this, RequestParamUtils.updatePassword, this, getlanuage());
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(RequestParamUtils.email, email);
                jsonObject.put(RequestParamUtils.PASSWORD, password);
                jsonObject.put(RequestParamUtils.key, pin);
                postApi.callPostApi(new URLS().UPDATE_PASSWORD, jsonObject.toString());
            } catch (Exception e) {
                Log.e("error", e.getMessage());
            }
        } else {
            Toast.makeText(this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResponse(final String response, final String methodName) {
        switch (methodName) {
            case RequestParamUtils.login:
            case RequestParamUtils.socialLogin:
                dismissProgress();
                if (response != null && response.length() > 0) {
                    try {
                        JSONObject jsonObj = new JSONObject(response);
                        String status = jsonObj.getString("status");
                        if (status.equals("success")) {
                            final LogIn loginRider = new Gson().fromJson(
                                    response, new TypeToken<LogIn>() {
                                    }.getType());

                            runOnUiThread(() -> {
                                //set call here
                                if (loginRider.status.equals("success")) {
                                    SharedPreferences.Editor pre = getPreferences().edit();
                                    pre.putString(RequestParamUtils.CUSTOMER, "");
                                    pre.putString(RequestParamUtils.ID, loginRider.user.id + "");
                                    pre.putString(RequestParamUtils.PASSWORD, binding.etPass.getText().toString());
                                    if (methodName.equals(RequestParamUtils.socialLogin)) {
                                        pre.putString(RequestParamUtils.SOCIAL_SIGNIN, "1");
                                    }
                                    pre.apply();
                                    dismissProgress();

                                    if (isSplash) {
                                        finish();
                                        Intent intent = new Intent(LogInActivity.this, HomeActivity.class);
                                        startActivity(intent);
                                    } else {
                                        finish();
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), R.string.enter_proper_detail, Toast.LENGTH_SHORT).show(); //display in long period of time
                                }
                            });
                        } else {
                            String msg = jsonObj.getString("message");
                            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(methodName + "Gson Exception is ", e.getMessage());
                        Toast.makeText(getApplicationContext(), R.string.something_went_wrong, Toast.LENGTH_SHORT).show(); //display in long period of time
                    }
                }
                break;
            case RequestParamUtils.forgotPassword:
                dismissProgress();
                if (response != null && response.length() > 0) {
                    try {
                        JSONObject jsonObj = new JSONObject(response);
                        String status = jsonObj.getString("status");
                        if (status.equals("success")) {
                            Toast.makeText(this, jsonObj.getString("message"), Toast.LENGTH_SHORT).show();
                            alertDialog1.dismiss();
                            pin = jsonObj.getString("key");
                            showSetPassDialog();
                        } else {
                            Toast.makeText(this, jsonObj.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(methodName + "Gson Exception is ", e.getMessage());
                        Toast.makeText(getApplicationContext(), R.string.something_went_wrong, Toast.LENGTH_SHORT).show(); //display in long period of time
                    }
                }
                break;
            case RequestParamUtils.updatePassword:
                dismissProgress();
                if (response != null && response.length() > 0) {
                    try {
                        JSONObject jsonObj = new JSONObject(response);
                        String status = jsonObj.getString("status");
                        if (status.equals("success")) {
                            alertDialog.dismiss();
                            Toast.makeText(this, jsonObj.getString("message"), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, jsonObj.getString("error"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(methodName + "Gson Exception is ", e.getMessage());
                        Toast.makeText(getApplicationContext(), R.string.something_went_wrong, Toast.LENGTH_SHORT).show(); //display in long period of time
                    }
                }
                break;
        }
    }


}