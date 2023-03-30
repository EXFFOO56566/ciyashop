package com.example.ciyashop.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.bumptech.glide.Glide;
import com.ciyashop.library.apicall.PostApi;
import com.ciyashop.library.apicall.URLS;
import com.ciyashop.library.apicall.interfaces.OnResponseListner;
import com.example.ciyashop.R;
import com.example.ciyashop.customview.edittext.EditTextRegular;
import com.example.ciyashop.customview.textview.TextViewRegular;
import com.example.ciyashop.databinding.ActivitySignUpsBinding;
import com.example.ciyashop.javaclasses.SyncWishList;
import com.example.ciyashop.model.LogIn;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Config;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.CustomToast;
import com.example.ciyashop.utils.RequestParamUtils;
import com.example.ciyashop.utils.Utils;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

public class SignUpActivity extends BaseActivity implements OnResponseListner {

    private static final String TAG = SignUpActivity.class.getSimpleName();

    AlertDialog alertDialog;
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private String mVerificationId;
    private CustomToast toast;

    private ActivitySignUpsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySignUpsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setClickEvent();

        toast = new CustomToast(this);
        mAuth = FirebaseAuth.getInstance();
        mAuth.useAppLanguage();
        setScreenLayoutDirection();
        setThemeColor();

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                Log.e(TAG, "onVerificationCompleted:" + credential);
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.e(TAG, "onVerificationFailed " + e.getMessage());

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // [START_EXCLUDE]
                    if (alertDialog != null) {
                        alertDialog.dismiss();
                    }
                    Toast.makeText(SignUpActivity.this, getString(R.string.invalid_phone_number), Toast.LENGTH_SHORT).show();
                    // [END_EXCLUDE]
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // [START_EXCLUDE]
                    Toast.makeText(SignUpActivity.this, getString(R.string.quoto_exceeded), Toast.LENGTH_SHORT).show();
                    // [END_EXCLUDE]
                }

               /* // Show a message and update the UI
                // [START_EXCLUDE]
                updateUI(STATE_VERIFY_FAILED);
                // [END_EXCLUDE]*/
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.e(TAG, "onCodeSent:" + verificationId);
                if (alertDialog != null) {
                    alertDialog.show();
                    dismissProgress();
                }
                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
            }
        };



    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.e(TAG, "signInWithCredential:success");
//                        FirebaseUser user = task.getResult().getUser();

                        if (alertDialog != null) {
                            alertDialog.dismiss();
                        }
                        registerUser();
                    } else {
                        // Sign in failed, display a message and update the UI
                        Log.e(TAG, "signInWithCredential:failure", task.getException());
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            // The verification code entered was invalid
                            // [START_EXCLUDE silent]
                            Toast.makeText(SignUpActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            // [END_EXCLUDE]
                        }
                       /* // [START_EXCLUDE silent]
                        // Update UI
                        updateUI(STATE_SIGNIN_FAILED);
                        // [END_EXCLUDE]*/
                    }
                });
    }

    public void setClickEvent() {
        binding.tvSignInNow.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, LogInActivity.class);
            startActivity(intent);
            finish();
        });

        binding.ivBlackBackButton.setOnClickListener(v -> onBackPressed());

        binding.tvSignUp.setOnClickListener(v -> {
            if (binding.etUsername.getText().toString().length() == 0) {
                Toast.makeText(this, R.string.enter_username, Toast.LENGTH_SHORT).show();
            } else if (binding.etEmail.getText().toString().length() == 0) {
                Toast.makeText(this, R.string.enter_email_address, Toast.LENGTH_SHORT).show();
            } else if (!Utils.isValidEmail(binding.etEmail.getText().toString())) {
                Toast.makeText(this, R.string.enter_valid_email_address, Toast.LENGTH_SHORT).show();
            } else if (binding.etContact.getText().toString().isEmpty()) {
                Toast.makeText(this, R.string.enter_contact_number, Toast.LENGTH_SHORT).show();
            } else if (binding.etPass.getText().toString().isEmpty()) {
                Toast.makeText(this, R.string.enter_password, Toast.LENGTH_SHORT).show();
            } else if (binding.etConfirmPass.getText().toString().isEmpty()) {
                Toast.makeText(this, R.string.enter_confirm_password, Toast.LENGTH_SHORT).show();
            } else if (binding.etPass.getText().toString().equals(binding.etConfirmPass.getText().toString())) {
                if (Config.OTPVerification) {
                    String number = binding.ccp.getSelectedCountryCodeWithPlus() + binding.etContact.getText().toString().trim();
                    Log.e("Otp :-", number);
                    ShowDialogForOTP(number);
                } else {
                    registerUser();
                }
            } else {
                Toast.makeText(this, R.string.password_and_confirm_password_not_matched, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setThemeColor() {
        if (Constant.APPLOGO != null && !Constant.APPLOGO.equals("")) {
            Glide.with(this).load(Constant.APPLOGO).error(R.drawable.logo).into(binding.ivLogo);
        }
        Drawable mDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.login, null);
        if (mDrawable != null) {
            mDrawable.setColorFilter(new
                    PorterDuffColorFilter(Color.parseColor(getPreferences().getString(Constant.APP_COLOR, Constant.PRIMARY_COLOR)), PorterDuff.Mode.OVERLAY));
        }
        binding.tvSignInNow.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        Drawable unwrappedDrawable = binding.tvSignUp.getBackground();
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, (Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR))));

        setTextViewDrawableColor(binding.etUsername, (Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR))));
        setTextViewDrawableColor(binding.etEmail, (Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR))));
        setTextViewDrawableColor(binding.etConfirmPass, (Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR))));
        setTextViewDrawableColor(binding.etPass, (Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR))));
        binding.ivBlackBackButton.setColorFilter((Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR))));
    }

    private void ShowDialogForOTP(final String number) {
        showProgress("");
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(number)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SignUpActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_otp_verification, null);
        dialogBuilder.setView(dialogView);

        final EditTextRegular etOTP = dialogView.findViewById(R.id.etOTP);
        TextViewRegular tvVerificationText = dialogView.findViewById(R.id.tvVerificationText);

        TextViewRegular tvDone = dialogView.findViewById(R.id.tvDone);
        TextViewRegular tvResend = dialogView.findViewById(R.id.tvResend);

        String verificationText = getResources().getString(R.string.please_type_verification_code_sent_to_in) + " " + binding.etContact.getText().toString();
        tvVerificationText.setText(verificationText);
        alertDialog = dialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;



        Drawable unwrappedDrawable = tvResend.getBackground();
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, (Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR))));

        Drawable unwrappedDrawable1 = tvDone.getBackground();
        Drawable wrappedDrawable1 = DrawableCompat.wrap(unwrappedDrawable1);
        DrawableCompat.setTint(wrappedDrawable1, (Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR))));

        tvDone.setOnClickListener(v -> {
            String code = etOTP.getText().toString();
            if (etOTP.getText().toString().length() == 0) {
                toast.showToast(getString(R.string.enter_verificiation_code));
                toast.showBlackBg();
//                    etOTP.setError("Enter Verification Code");
                return;
            }
            verifyPhoneNumberWithCode(mVerificationId, code);
        });

        tvResend.setOnClickListener(v -> {
            Toast.makeText(SignUpActivity.this,
                    getString(R.string.otp_sent_again) + " " + number, Toast.LENGTH_SHORT).show();

            resendVerificationCode(number, mResendToken);
        });

    /*    rvProductVariation = (RecyclerView) dialogView.findViewById(R.id.rvProductVariation);
        TextViewRegular tvDone = (TextViewRegular) dialogView.findViewById(R.id.tvDone);
        TextViewRegular tvCancel = (TextViewRegular) dialogView.findViewById(R.id.tvCancel);*/
    }

    private void verifyPhoneNumberWithCode(String mVerificationId, String code) {
        // [START verify_with_code]
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        // [END verify_with_code]
        signInWithPhoneAuthCredential(credential);
    }

    private void resendVerificationCode(String number, PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(number)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    public void registerUser() {
        if (Utils.isInternetConnected(this)) {
            showProgress("");
            PostApi postApi = new PostApi(this, "create_customer", this, getlanuage());
            JSONObject object = new JSONObject();
            try {
                object.put(RequestParamUtils.email, binding.etEmail.getText().toString());
                object.put(RequestParamUtils.username, binding.etUsername.getText().toString());
                object.put(RequestParamUtils.mobile, binding.ccp.getSelectedCountryCodeWithPlus() + binding.etContact.getText().toString().trim());
                object.put(RequestParamUtils.PASSWORD, binding.etPass.getText().toString());
                object.put(RequestParamUtils.deviceType, Constant.DEVICE_TYPE);

                String token = getPreferences().getString(RequestParamUtils.NOTIFICATION_TOKEN, "");
                object.put(RequestParamUtils.deviceToken, token);

                postApi.callPostApi(new URLS().CREATE_CUSTOMER, object.toString());
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Toast.makeText(this, R.string.something_went_wrong_try_after_somtime, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResponse(final String response, String methodName) {
        if (methodName.equals(RequestParamUtils.createCustomer)) {
            if (response != null && response.length() > 0) {
                try {
                    final LogIn loginRider = new Gson().fromJson(
                            response, new TypeToken<LogIn>() {
                            }.getType());

                    JSONObject jsonObj = new JSONObject(response);
                    String status = jsonObj.getString("status");

                    if (status.equals("error")) {
                        Toast.makeText(getApplicationContext(), jsonObj.getString("message"), Toast.LENGTH_SHORT).show(); //display in long period of time
                    } else {
                        runOnUiThread(() -> {
                            //set call here
                            if (loginRider.status.equals("success")) {
                                SharedPreferences.Editor pre = getPreferences().edit();
                                pre.putString(RequestParamUtils.CUSTOMER, "");
                                pre.putString(RequestParamUtils.ID, loginRider.user.id + "");
                                pre.putString(RequestParamUtils.PASSWORD, binding.etPass.getText().toString());
                                pre.apply();

                                new SyncWishList(SignUpActivity.this).syncWishList(getPreferences().getString(RequestParamUtils.ID, ""), false);
//                                    Intent intent = new Intent(SignUpActivity.this, AccountActivity.class);
//                                    startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), R.string.enter_proper_detail, Toast.LENGTH_SHORT).show(); //display in long period of time
                            }
                        });
                    }
                    dismissProgress();
                } catch (Exception e) {
                    Log.e(methodName + "Gson Exception is ", e.getMessage());
                    Toast.makeText(getApplicationContext(), R.string.something_went_wrong, Toast.LENGTH_SHORT).show(); //display in long period of time
                }
            }
        }
    }
}
