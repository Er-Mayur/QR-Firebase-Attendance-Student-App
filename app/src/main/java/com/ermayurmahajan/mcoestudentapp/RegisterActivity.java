package com.ermayurmahajan.mcoestudentapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity{
    boolean isStudentMobileVerify = false , isParentMobileVerify = false;
    private static final int PERMISSION_REQUEST_SEND_SMS = 1;
    ReadWriteDetails writeDetails;
    DatabaseReference userRef;
    Spinner spnYear;
    FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    private DatabaseReference studentsRef;
    private ImageView imgRegisterShowHidePassword;
    private  static final String TAG = "RegisterActivity";
    private TextView txtParentMobileNumberOTP, txtStudentMobileNumberOTP;
    private EditText edtStudentFullName, edtDOB, edtAcademicYear, edtRollNo, edtBatch, edtStudentMobileNumber, edtEmail, edtParentFullName;
    private EditText edtParentMobileNumber, edtPassword, edtConfirmPassword;
    private boolean isFormatting;
    private LottieAnimationView loading;
    private DatePickerDialog picker;
    RadioGroup radBtnGender;
    RadioButton radBtnGenderSelected;
    String textGenderSelected ,textStudentFullName, textDOB, textYearSelected, textAcademicYear, textRollNo, textBatch, textStudentMobileNumber = null, textEmail, textParentFullName, textParentMobileNumber = null, textPassword;
    Button btnRegister;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);
        getSupportActionBar().hide();
        spnYear = findViewById(R.id.spn_year);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.Year_list, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spnYear.setAdapter(adapter);
        spnYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                textYearSelected = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Toast.makeText(RegisterActivity.this, "Please select your Year", Toast.LENGTH_SHORT).show();
            }
        });
        Toast.makeText(this, "Register Now", Toast.LENGTH_SHORT).show();
        loading = findViewById(R.id.loading);

        String mobileRegex = "[6-9][0-9]{9}"; //1st No. can be {6, 7, 8, 9} and rest 9 nos. can be any no.
        Pattern mobilePattern = Pattern.compile(mobileRegex);

        //Edit View ID
        edtStudentFullName = findViewById(R.id.edt_student_full_name);
        edtDOB = findViewById(R.id.edt_DOB);
        edtAcademicYear = findViewById(R.id.edt_academic_year);
        edtRollNo = findViewById(R.id.edt_roll_no);
        edtBatch = findViewById(R.id.edt_batch);
        edtStudentMobileNumber = findViewById(R.id.edt_student_mobile_number);
        edtEmail = findViewById(R.id.edt_email);
        edtParentFullName = findViewById(R.id.edt_parent_full_name);
        edtParentMobileNumber = findViewById(R.id.edt_parent_mobile_number);
        edtPassword = findViewById(R.id.edt_password);
        edtConfirmPassword = findViewById(R.id.edt_confirm_password);
        imgRegisterShowHidePassword = findViewById(R.id.img_register_show_hide_password);
        txtStudentMobileNumberOTP = findViewById(R.id.txt_student_mobile_number_OTP);
        txtParentMobileNumberOTP = findViewById(R.id.txt_parent_mobile_number_OTP);

        edtAcademicYear.setFilters(new InputFilter[]{new DateFormatFilter()});
        edtAcademicYear.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed before text changes
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No action needed during text changes
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!isFormatting) {
                    String inputText = s.toString();
                    String formattedText = formatDateText(inputText);

                    if (!inputText.equals(formattedText)) {
                        isFormatting = true;
                        s.replace(0, s.length(), formattedText);
                        isFormatting = false;
                    }
                }
            }
        });

        FirebaseApp.initializeApp(this);
        database = FirebaseDatabase.getInstance();
        studentsRef = database.getReference("Registered Students");

        //Radio Button Id
        radBtnGender = findViewById(R.id.rad_btn_gender);

        radBtnGender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                ((RadioButton) findViewById(R.id.rad_btn_male)).setError(null);
                ((RadioButton) findViewById(R.id.rad_btn_female)).setError(null);
            }
        });

        btnRegister = findViewById(R.id.btn_register);

        //Set Image of Hidden Password
        imgRegisterShowHidePassword.setImageResource(R.drawable.outline_visibility_off_24);
        imgRegisterShowHidePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edtPassword.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())) {
                    edtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    //Change Icon
                    imgRegisterShowHidePassword.setImageResource(R.drawable.outline_visibility_off_24);
                } else {
                    edtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    imgRegisterShowHidePassword.setImageResource(R.drawable.outline_visibility_24);
                }
            }
        });

        //Setting Up DatePicker on edtDOB
        edtDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edtDOB.setError(null);
                final Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);

                //Date Picker
                picker = new DatePickerDialog(RegisterActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                        edtDOB.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                    }
                }, year, month, day);
                picker.show();
            }
        });

        txtStudentMobileNumberOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textStudentMobileNumber = edtStudentMobileNumber.getText().toString();
                Matcher studentMobileMatcher;
                studentMobileMatcher = mobilePattern.matcher(textStudentMobileNumber);

                if (TextUtils.isEmpty(textStudentMobileNumber)) {
                    edtStudentMobileNumber.setError("Student Mobile Number is required");
                    edtStudentMobileNumber.requestFocus();
                } else if (!studentMobileMatcher.find() || textStudentMobileNumber.length() != 10) {
                    edtStudentMobileNumber.setError("Mobile No. is Invalid");
                    edtStudentMobileNumber.requestFocus();
                } else {
                    // Check for SMS permission
                    if (checkPermission()) {
                        sendSMS(textStudentMobileNumber, 1);
                    } else {
                        requestPermission();
                    }
                }
            }
        });

        txtParentMobileNumberOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textParentMobileNumber = edtParentMobileNumber.getText().toString();
                Matcher parentMobileMatcher;
                parentMobileMatcher = mobilePattern.matcher(textParentMobileNumber);
                if (TextUtils.isEmpty(textParentMobileNumber)) {
                    edtParentMobileNumber.setError("Parent Mobile Number is required");
                    edtParentMobileNumber.requestFocus();
                } else if (!parentMobileMatcher.find() || textParentMobileNumber.length() != 10) {
                    edtParentMobileNumber.setError("Mobile No. is Invalid");
                    edtParentMobileNumber.requestFocus();
                } else if (textStudentMobileNumber == null) {
                    edtStudentMobileNumber.setError("Enter Student Mobile Number");
                    edtStudentMobileNumber.requestFocus();
                } else if (textStudentMobileNumber.equals(textParentMobileNumber)) {
                    edtParentMobileNumber.setError("Student & Parent Mobile Must Different");
                    edtParentMobileNumber.requestFocus();
                } else {
                    // Check for SMS permission
                    if (checkPermission()) {
                        sendSMS(textParentMobileNumber, 2);
                    } else {
                        requestPermission();
                    }
                }
            }
        });
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selectedGenderId = radBtnGender.getCheckedRadioButtonId();
                radBtnGenderSelected = findViewById(selectedGenderId);

                //Obtain the student entered data
                textStudentFullName = edtStudentFullName.getText().toString().toUpperCase();
                textDOB = edtDOB.getText().toString();
                textAcademicYear = edtAcademicYear.getText().toString();
                textRollNo = edtRollNo.getText().toString();
                textBatch = edtBatch.getText().toString().toUpperCase();
                textStudentMobileNumber = edtStudentMobileNumber.getText().toString();
                textEmail = edtEmail.getText().toString();
                textParentFullName = edtParentFullName.getText().toString().toUpperCase();
                textParentMobileNumber = edtParentMobileNumber.getText().toString();
                textPassword = edtPassword.getText().toString();
                String textConfirmPassword = edtConfirmPassword.getText().toString();

                //Checking information is Not empty
                if (TextUtils.isEmpty(textStudentFullName)) {
                    edtStudentFullName.setError("Full Name is required");
                    edtStudentFullName.requestFocus();
                } else if (TextUtils.isEmpty(textDOB)) {
                    edtDOB.setError("Enter DOB");
                    edtDOB.requestFocus();
                } else if (Objects.equals(textYearSelected, "Year")) {
                    Toast.makeText(RegisterActivity.this, "Please select Year", Toast.LENGTH_SHORT).show();
                    spnYear.requestFocus();
                } else if (radBtnGender.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(RegisterActivity.this, "Please select your gender", Toast.LENGTH_SHORT).show();
                    ((RadioButton) findViewById(R.id.rad_btn_male)).setError(" ");
                    ((RadioButton) findViewById(R.id.rad_btn_female)).setError(" ");
                    radBtnGender.requestFocus();
                } else if ((TextUtils.isEmpty(textAcademicYear)) || textAcademicYear.length() != 7) {
                    edtAcademicYear.setError("Enter Academic Year 202X-2X");
                    edtAcademicYear.requestFocus();
                } else if (TextUtils.isEmpty(textRollNo) || textRollNo.length() != 5) {
                    edtRollNo.setError("5 Digit Roll No is required");
                    edtRollNo.requestFocus();
                } else if (TextUtils.isEmpty(textBatch) || textBatch.length() != 2) {
                    edtBatch.setError("Batch is required");
                    edtBatch.requestFocus();
                } else if (!isStudentMobileVerify) {
                    edtStudentMobileNumber.setError("Verify Student Mobile No.");
                    edtStudentMobileNumber.requestFocus();
                } else if (TextUtils.isEmpty(textParentFullName)) {
                    edtParentFullName.setError("Parent Full Name is required");
                    edtParentFullName.requestFocus();
                } else if (!isParentMobileVerify) {
                    edtParentMobileNumber.setError("Verify Parent Mobile No.");
                    edtParentMobileNumber.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()) {
                    edtEmail.setError("Enter Valid Email");
                    edtEmail.requestFocus();
                } else if (TextUtils.isEmpty(textPassword)) {
                    edtPassword.setError("Password is required");
                    edtPassword.requestFocus();
                } else if (textPassword.length() < 6) {
                    edtPassword.setError("Password is too weak at least 6 digit");
                    edtPassword.requestFocus();
                } else if (TextUtils.isEmpty(textConfirmPassword)) {
                    edtConfirmPassword.setError("Password Confirmation is required");
                    edtConfirmPassword.requestFocus();
                } else if (!textPassword.equals(textConfirmPassword)) {
                    edtConfirmPassword.setError("Password doses not match");
                    edtConfirmPassword.requestFocus();
                    //Clear the confirm password
                } else {
                    textGenderSelected = radBtnGenderSelected.getText().toString();
                    loading.setVisibility(view.VISIBLE);
                    disableEditView();
                    registerNewStudent(textEmail, textPassword);
                }
            }
        });

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_SEND_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendSMS(textStudentMobileNumber, 1);
            } else {
                // Permission denied, show a message or handle accordingly
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void sendSMS(String phoneNumber, int type) {
        Random random = new Random();

        // Generate a random 4-digit number
        int OTP = 1000 + random.nextInt(9000);
        String smsOTPMessage = "This message is from PES Modern College. OTP is " + OTP;

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage("+91" + phoneNumber, null, smsOTPMessage, null, null);
            studentOTP(OTP, phoneNumber , type);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, PERMISSION_REQUEST_SEND_SMS);
    }

    private void studentOTP(int OTP, String phoneNumber, int type){
        String sOTP = String.valueOf(OTP);
        Dialog dialog = new Dialog(RegisterActivity.this);
        dialog.setContentView(R.layout.otp_dialog_layout);
        dialog.setCancelable(false);
        TextView txtDialogBoxTitle = dialog.findViewById(R.id.txt_dialog_box_title);
        TextView txtReSendOTP = dialog.findViewById(R.id.txt_resend_OTP);
        EditText edtPhoneOTP = dialog.findViewById(R.id.edt_phone_OTP);
        Button btnDialogBack = dialog.findViewById(R.id.btn_dialog_back);
        Button btnDialogSubmit = dialog.findViewById(R.id.btn_dialog_submit);

        //set Size
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());

        txtDialogBoxTitle.setText("+91 " + phoneNumber);
        btnDialogBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();

        btnDialogSubmit.setOnClickListener(new View.OnClickListener() {

            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {
                String txtUserEnterOTP = edtPhoneOTP.getText().toString();
                if (TextUtils.isEmpty(txtUserEnterOTP)) {
                    edtPhoneOTP.setError("Enter OTP");
                    edtPhoneOTP.requestFocus();
                } else {
                    if (sOTP.equals(txtUserEnterOTP)) {
                        Toast.makeText(RegisterActivity.this, "Verification Successful", Toast.LENGTH_SHORT).show();
                        if(type == 1){
                            edtStudentMobileNumber.setEnabled(false);
                            edtStudentMobileNumber.setFocusable(false);
                            txtStudentMobileNumberOTP.setTextColor(R.color.gray);
                            txtStudentMobileNumberOTP.setEnabled(false);
                            isStudentMobileVerify = true;
                        } else if (type == 2) {
                            edtParentMobileNumber.setEnabled(false);
                            edtParentMobileNumber.setFocusable(false);
                            txtParentMobileNumberOTP.setTextColor(R.color.gray);
                            txtParentMobileNumberOTP.setEnabled(false);
                            isParentMobileVerify = true;
                        }
                        dialog.dismiss();

                    } else {
                        Toast.makeText(RegisterActivity.this, "Verification Failed", Toast.LENGTH_SHORT).show();
                        edtPhoneOTP.setError("Wrong OTP");
                        edtPhoneOTP.requestFocus();
                    }
                }
            }
        });
        txtReSendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendSMS(phoneNumber, type);
                dialog.dismiss();
            }
        });
    }
    @NonNull
    private String formatDateText(String text) {
        // Remove all non-numeric characters
        String numbersOnly = text.replaceAll("[^0-9]", "");

        if (numbersOnly.length() > 6) {
            // Truncate the input to a maximum of 7 characters
            numbersOnly = numbersOnly.substring(0, 6);
        }

        if (numbersOnly.length() >= 5) {
            // Format the date as "yyyy-yy"
            String formattedText = numbersOnly.substring(0, 4) + "-" + numbersOnly.substring(4);

            return formattedText;
        }

        return numbersOnly;
    }

    private class DateFormatFilter implements InputFilter {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            StringBuilder filteredBuilder = new StringBuilder(end - start);
            for (int i = start; i < end; i++) {
                char currentChar = source.charAt(i);
                if (Character.isDigit(currentChar) || currentChar == '-') {
                    filteredBuilder.append(currentChar);
                }
            }
            return filteredBuilder.toString();
        }
    }

    private void registerNewStudent(String textEmail, String textPassword){
        //Creating User Profile
        auth.createUserWithEmailAndPassword(textEmail, textPassword).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    //Enter User Data into the Firebase Realtime Database
                    writeDetails = new ReadWriteDetails(textStudentFullName, textDOB, textGenderSelected, textYearSelected, textAcademicYear, textRollNo, textBatch, textStudentMobileNumber, textEmail, textParentFullName, textParentMobileNumber);
                    firebaseUser = auth.getCurrentUser();
                    userRef = studentsRef.child(textAcademicYear).child(textYearSelected);

                    userRef.child(firebaseUser.getUid()).setValue(writeDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                loading.setVisibility(View.GONE);
                                Toast.makeText(RegisterActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                                //Open user profile after successful registration
                                Intent intent = new Intent(RegisterActivity.this, UserProfileActivity.class);

                                //To Prevent user from returning back to register Activity on pressing back button after registration
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish(); // to close Register Activity
                            } else {
                                loading.setVisibility(View.GONE);
                                Toast.makeText(RegisterActivity.this, "Registration Failed. Please try again", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }else {
                    try {
                        throw task.getException();
                    }catch (FirebaseAuthWeakPasswordException e){
                        edtPassword.setError("Your password is too weak. Kindly use a mix of alphabet, number & spacial characters");
                        edtPassword.requestFocus();
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        edtEmail.setError("Your email is invalid or already in use. Kindly re-enter");
                        edtEmail.requestFocus();
                    }catch (FirebaseAuthUserCollisionException e){
                        edtEmail.setError("User is already register with this email. Use another email");
                        edtEmail.requestFocus();
                    }catch (Exception e){
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }finally {
                        loading.setVisibility(View.GONE);
                    }
                }
            }
        });
    }
    private void disableEditView(){
        edtStudentFullName.setEnabled(false);
        edtStudentFullName.setFocusable(false);
        edtDOB.setEnabled(false);
        edtDOB.setFocusable(false);
        edtAcademicYear.setEnabled(false);
        edtAcademicYear.setFocusable(false);
        edtRollNo.setEnabled(false);
        edtRollNo.setFocusable(false);
        edtBatch.setEnabled(false);
        edtBatch.setFocusable(false);
        edtEmail.setEnabled(false);
        edtEmail.setFocusable(false);
        edtParentFullName.setEnabled(false);
        edtParentFullName.setFocusable(false);
        edtPassword.setEnabled(false);
        edtPassword.setFocusable(false);
        edtConfirmPassword.setEnabled(false);
        edtConfirmPassword.setFocusable(false);
        (findViewById(R.id.rad_btn_male)).setEnabled(false);
        (findViewById(R.id.rad_btn_male)).setFocusable(false);
        (findViewById(R.id.rad_btn_female)).setEnabled(false);
        (findViewById(R.id.rad_btn_female)).setFocusable(false);
        spnYear.setEnabled(false);
        spnYear.setFocusable(false);
    }

    @Override
    protected void onResume(){
        super.onResume();
        if (firebaseUser != null){
            firebaseUser.reload();
        }
    }
}