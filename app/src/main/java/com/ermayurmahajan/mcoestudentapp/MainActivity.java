package com.ermayurmahajan.mcoestudentapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.ermayurmahajan.mcoestudentapp.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth authProfile;
    private ActivityMainBinding binding;
    private Animation fadeIn;
    private Animation bottomDown;
    private  EditText edtLoginEmail, edtLoginPassword;
    private LottieAnimationView loading;
    private ImageView imgShowHidePassword;
    private static final String TAG = "MainActivity";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        bottomDown = AnimationUtils.loadAnimation(this, R.anim.bottom_down);
        binding.topLinearLayout.setAnimation(bottomDown);
        binding.txtPesModernCollagePune.setAnimation(bottomDown);
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                binding.cardView1.setAnimation(fadeIn);
                binding.cardView2.setAnimation(fadeIn);
                binding.registerLayout.setAnimation(fadeIn);
            }
        };
        handler.postDelayed(runnable, 1000);

        edtLoginEmail = findViewById(R.id.edt_email);
        edtLoginPassword = findViewById(R.id.edt_password);
        loading = findViewById(R.id.loading);
        imgShowHidePassword = findViewById(R.id.img_login_show_hide_password);

        //Set Image of Hidden Password
        imgShowHidePassword.setImageResource(R.drawable.outline_visibility_off_24);
        imgShowHidePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edtLoginPassword.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())){
                    edtLoginPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    //Change Icon
                    imgShowHidePassword.setImageResource(R.drawable.outline_visibility_off_24);
                }else{
                    edtLoginPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    imgShowHidePassword.setImageResource(R.drawable.outline_visibility_24);
                }
            }
        });

        authProfile = FirebaseAuth.getInstance();

    }
    //If user is already login
    @Override
    protected void onStart(){
        super.onStart();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();
        if (firebaseUser != null) {
            if (authProfile.getCurrentUser() != null) {

                //Open user profile after successful registration
                Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);

                //To Prevent user from returning back to register Activity on pressing back button after registration
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish(); // to close Register Activity
            }
        }
    }
    public void loginActivity(View view) {
        String textEmail = edtLoginEmail.getText().toString();
        String textPassword = edtLoginPassword.getText().toString();

        if (TextUtils.isEmpty(textEmail)){
            edtLoginEmail.setError("Enter Your Email");
            edtLoginEmail.requestFocus();
        }else if (!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()) {
            edtLoginEmail.setError("Enter Valid Email");
            edtLoginEmail.requestFocus();
        }else if (TextUtils.isEmpty(textPassword)) {
            edtLoginPassword.setError("Enter Your Password");
            edtLoginPassword.requestFocus();
        }else {
            loading.setVisibility(View.VISIBLE);
            loginUser(textEmail, textPassword);
        }
    }

    private void loginUser(String email, String password) {
        authProfile.signInWithEmailAndPassword(email, password).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                    //Open user profile after successful registration
                    Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
                    //To Prevent user from returning back to register Activity on pressing back button after registration
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish(); // to close Register Activity
                } else {
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidUserException e) {
                        // This exception is thrown if the user does not exist or is no longer valid
                        edtLoginEmail.setError("User does not exist or is no longer valid. Please register first");
                        edtLoginEmail.requestFocus();
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        // This exception is thrown if the password is incorrect
                        edtLoginPassword.setError("Wrong Password");
                        edtLoginPassword.requestFocus();
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(MainActivity.this, "Authentication failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                loading.setVisibility(View.GONE);
            }
        });
    }
    public void registerActivity(View view) {
        Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
        startActivity(intent);
    }

    public void forgetPasswordActivity(View view) {
        Intent intent = new Intent(getApplicationContext(), ForgetPasswordActivity.class);
        startActivity(intent);
    }
}