package com.ermayurmahajan.mcoestudentapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateUserDataActivity extends AppCompatActivity {
    boolean isStudentMobileVerify = false , isParentMobileVerify = false;
    private static final int PERMISSION_REQUEST_SEND_SMS = 1;
    private static final String PREFS_NAME = "UserPrefs";
    private static String ACADEMIC_YEAR, YEAR, ROLL_NO, BATCH, PARENT_MOBILE_NUMBER, STUDENT_MOBILE_NUMBER;
    private static String userID;
    private LottieAnimationView loading;
    private Spinner spnOldYear, spnNewYear;
    private TextView txtStudentFullName, txtParentFullName, txtParentMobileNumberOTP, txtStudentMobileNumberOTP;
    private EditText edtOldAcademicYear, edtNewAcademicYear, edtNewRollNo, edtBactch, edtNewStudentMobileNumber, edtNewPrentMobileNumber;
    private Button btnGetData, btnSetData;
    private CardView RcardView2, UcardView2;
    private FirebaseAuth authProfile;
    private FirebaseDatabase database;
    private FirebaseUser firebaseUser;
    private DatabaseReference studentsRef;
    private ReadWriteDetails readUserDetails;
    private boolean isFormatting;
    private String textOldStudentMobileNumber, textOldParentMobileNumber, textYearSelected, textOldAcademicYear, textNewAcademicYear, textNewRollNo, textNewBatch, textNewStudentMobileNumber = null, textNewParentMobileNumber = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_user_data_activity);
        getSupportActionBar().hide();

        spnOldYear = findViewById(R.id.spn_old_year);
        edtOldAcademicYear = findViewById(R.id.edt_old_academic_year);
        btnGetData = findViewById(R.id.btn_get_data);
        btnSetData = findViewById(R.id.btn_set_data);
        RcardView2 = findViewById(R.id.Rcard_view2);
        UcardView2 = findViewById(R.id.Ucard_view2);
        spnNewYear = findViewById(R.id.spn_new_year);
        edtNewAcademicYear = findViewById(R.id.edt_new_academic_year);
        txtStudentFullName = findViewById(R.id.txt_student_full_name);
        txtParentFullName = findViewById(R.id.txt_parent_full_name);
        edtNewRollNo = findViewById(R.id.edt_roll_no);
        edtBactch = findViewById(R.id.edt_batch);
        edtNewStudentMobileNumber = findViewById(R.id.edt_student_mobile_number);
        edtNewPrentMobileNumber = findViewById(R.id.edt_parent_mobile_number);
        loading = findViewById(R.id.loading);
        txtStudentMobileNumberOTP = findViewById(R.id.txt_student_mobile_number_OTP);
        txtParentMobileNumberOTP = findViewById(R.id.txt_parent_mobile_number_OTP);

        String mobileRegex = "[6-9][0-9]{9}"; //1st No. can be {6, 7, 8, 9} and rest 9 nos. can be any no.
        Pattern mobilePattern = Pattern.compile(mobileRegex);

        FirebaseApp.initializeApp(this);
        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        studentsRef = database.getReference().child("Registered Students");

        userID = firebaseUser.getUid();
        ACADEMIC_YEAR = userID + "academicYear";
        YEAR = userID + "year";
        ROLL_NO = userID + "rollNo";
        BATCH = userID + "batch";
        STUDENT_MOBILE_NUMBER = userID + "studentMobileNumber";
        PARENT_MOBILE_NUMBER = userID + "parentMobileNumber";

        spinnerAcademic(spnOldYear, edtOldAcademicYear);
        btnGetData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                textOldAcademicYear = edtOldAcademicYear.getText().toString();
                //Checking information is Not empty
                if (TextUtils.isEmpty(textOldAcademicYear) || textOldAcademicYear.length() != 7) {
                    edtOldAcademicYear.setError("Enter Old Academic year 202X-2X");
                    edtOldAcademicYear.requestFocus();
                }else if (Objects.equals(textYearSelected, "Year")) {
                    Toast.makeText(UpdateUserDataActivity.this, "Please select your old Year", Toast.LENGTH_SHORT).show();
                    spnOldYear.requestFocus();
                }else {
                    loading.setVisibility(view.VISIBLE);
                    getData(textOldAcademicYear, textYearSelected);
                }
            }
        });

        txtStudentMobileNumberOTP.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {
                textNewStudentMobileNumber = edtNewStudentMobileNumber.getText().toString();
                Matcher studentMobileMatcher;
                studentMobileMatcher = mobilePattern.matcher(textNewStudentMobileNumber);

                if (TextUtils.isEmpty(textNewStudentMobileNumber)) {
                    edtNewStudentMobileNumber.setError("Student Mobile Number is required");
                    edtNewStudentMobileNumber.requestFocus();
                } else if (!studentMobileMatcher.find() || textNewStudentMobileNumber.length() != 10) {
                    edtNewStudentMobileNumber.setError("Mobile No. is Invalid");
                    edtNewStudentMobileNumber.requestFocus();
                } else {
                    // Check for SMS permission
                    if (textOldStudentMobileNumber.equals(textNewStudentMobileNumber)) {
                        edtNewStudentMobileNumber.setEnabled(false);
                        edtNewStudentMobileNumber.setFocusable(false);
                        txtStudentMobileNumberOTP.setTextColor(R.color.gray);
                        txtStudentMobileNumberOTP.setEnabled(false);
                        isStudentMobileVerify = true;
                    } else if (checkPermission()) {
                        sendSMS(textNewStudentMobileNumber, 1);
                    } else {
                        requestPermission();
                    }
                }
            }
        });

        txtParentMobileNumberOTP.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {
                textNewParentMobileNumber = edtNewPrentMobileNumber.getText().toString();
                Matcher parentMobileMatcher;
                parentMobileMatcher = mobilePattern.matcher(textNewParentMobileNumber);
                if (TextUtils.isEmpty(textNewParentMobileNumber)) {
                    edtNewPrentMobileNumber.setError("Parent Mobile Number is required");
                    edtNewPrentMobileNumber.requestFocus();
                } else if (!parentMobileMatcher.find() || textNewParentMobileNumber.length() != 10) {
                    edtNewPrentMobileNumber.setError("Mobile No. is Invalid");
                    edtNewPrentMobileNumber.requestFocus();
                } else if (textNewStudentMobileNumber == null) {
                    edtNewStudentMobileNumber.setError("Enter Student Mobile Number");
                    edtNewStudentMobileNumber.requestFocus();
                } else if (textNewStudentMobileNumber.equals(textNewParentMobileNumber)) {
                    edtNewPrentMobileNumber.setError("Student & Parent Mobile Must Different");
                    edtNewPrentMobileNumber.requestFocus();
                } else {
                    // Check for SMS permission
                    if (textOldParentMobileNumber.equals(textNewParentMobileNumber)) {
                        edtNewPrentMobileNumber.setEnabled(false);
                        edtNewPrentMobileNumber.setFocusable(false);
                        txtParentMobileNumberOTP.setTextColor(R.color.gray);
                        txtParentMobileNumberOTP.setEnabled(false);
                        isParentMobileVerify = true;
                    } else if (checkPermission()) {
                        sendSMS(textNewParentMobileNumber, 2);
                    } else {
                        requestPermission();
                    }
                }
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_SEND_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                // Permission denied, show a message or handle accordingly
                Toast.makeText(UpdateUserDataActivity.this, "SMS permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void sendSMS(String phoneNumber, int type) {
        Random random = new Random();

        // Generate a random 4-digit number
        int OTP = 1000 + random.nextInt(9000);
        String smsOTPMessage = "This message is from Brilliant Science Institute. OTP is " + OTP;

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
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, PERMISSION_REQUEST_SEND_SMS);
    }

    private void studentOTP(int OTP, String phoneNumber, int type){
        String sOTP = String.valueOf(OTP);
        Dialog dialog = new Dialog(UpdateUserDataActivity.this);
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
                        Toast.makeText(UpdateUserDataActivity.this, "Verification Successful", Toast.LENGTH_SHORT).show();
                        if(type == 1){
                            edtNewStudentMobileNumber.setEnabled(false);
                            edtNewStudentMobileNumber.setFocusable(false);
                            txtStudentMobileNumberOTP.setTextColor(R.color.gray);
                            txtStudentMobileNumberOTP.setEnabled(false);
                            isStudentMobileVerify = true;
                        } else if (type == 2) {
                            edtNewPrentMobileNumber.setEnabled(false);
                            edtNewPrentMobileNumber.setFocusable(false);
                            txtParentMobileNumberOTP.setTextColor(R.color.gray);
                            txtParentMobileNumberOTP.setEnabled(false);
                            isParentMobileVerify = true;
                        }
                        dialog.dismiss();

                    } else {
                        Toast.makeText(UpdateUserDataActivity.this, "Verification Failed", Toast.LENGTH_SHORT).show();
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
    private static class DateFormatFilter implements InputFilter {
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
    private void getData(String textAcademicYear, String textYearSelected){
        DatabaseReference oldDataRef = studentsRef.child(textAcademicYear).child(textYearSelected).child(firebaseUser.getUid());
        oldDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    readUserDetails = snapshot.getValue(ReadWriteDetails.class);
                    if (readUserDetails != null) {
                        setData(readUserDetails, oldDataRef, textAcademicYear, textYearSelected);
                    }
                }else {
                    Toast.makeText(UpdateUserDataActivity.this, "Wrong Details", Toast.LENGTH_SHORT).show();
                }
                loading.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void spinnerAcademic(@NonNull Spinner spnYear, EditText edtAcademicYear){
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
                Toast.makeText(UpdateUserDataActivity.this, "Please select Year", Toast.LENGTH_SHORT).show();
            }
        });
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
    }
    private void setData(ReadWriteDetails readUserDetails, DatabaseReference oldDataRef, String oldAcademicYear, String oldYear) {
        RcardView2.setVisibility(View.INVISIBLE);
        btnGetData.setVisibility(View.INVISIBLE);
        UcardView2.setVisibility(View.VISIBLE);
        btnSetData.setVisibility(View.VISIBLE);

        txtStudentFullName.setText(readUserDetails.textStudentFullName);
        txtParentFullName.setText(readUserDetails.textParentFullName);
        textOldStudentMobileNumber = readUserDetails.textStudentMobileNumber;
        textOldParentMobileNumber = readUserDetails.textParentMobileNumber;

        spinnerAcademic(spnNewYear, edtNewAcademicYear);

        btnSetData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textNewAcademicYear = edtNewAcademicYear.getText().toString();
                textNewRollNo = edtNewRollNo.getText().toString();
                textNewBatch = edtBactch.getText().toString().toUpperCase();
                textNewStudentMobileNumber = edtNewStudentMobileNumber.getText().toString();
                textNewParentMobileNumber = edtNewPrentMobileNumber.getText().toString();

                //Checking information is Not empty
                if (TextUtils.isEmpty(textNewAcademicYear) || textNewAcademicYear.length() != 7) {
                    edtNewAcademicYear.setError("Enter New Academic Year 202X-2X");
                    edtNewAcademicYear.requestFocus();
                } else if (Objects.equals(textYearSelected, "Year")) {
                    Toast.makeText(UpdateUserDataActivity.this, "Please select your New Year", Toast.LENGTH_SHORT).show();
                    spnNewYear.requestFocus();
                } else if (TextUtils.isEmpty(textNewRollNo) || textNewRollNo.length() != 5) {
                    edtNewRollNo.setError("5 Digit Roll No is required");
                    edtNewRollNo.requestFocus();
                }else if (TextUtils.isEmpty(textNewBatch) || textNewBatch.length() != 2) {
                    edtBactch.setError("Enter Batch");
                    edtBactch.requestFocus();
                } else if (!isStudentMobileVerify) {
                    edtNewStudentMobileNumber.setError("Verify Student Mobile No.");
                    edtNewStudentMobileNumber.requestFocus();
                } else if (!isParentMobileVerify) {
                    edtNewPrentMobileNumber.setError("Verify Parent Mobile No.");
                    edtNewPrentMobileNumber.requestFocus();
                } else {
                    loading.setVisibility(View.VISIBLE);
                    setNewData(oldDataRef, readUserDetails, oldAcademicYear, oldYear, textNewAcademicYear, textYearSelected, textNewRollNo, textNewBatch, textNewStudentMobileNumber, textNewParentMobileNumber);
                }
            }
        });
    }
    private void setNewData(DatabaseReference oldDataRef, ReadWriteDetails readUserDetails, String oldAcademicYear, String oldYear, String textNewAcademicYear, String textYearSelected,  String textNewRollNo, String textNewBatch, String textNewStudentMobileNumber, String textNewParentMobileNumber){
        readUserDetails.textAcademicYear = textNewAcademicYear;
        readUserDetails.textYear= textYearSelected;
        readUserDetails.textRollNo = textNewRollNo;
        readUserDetails.textBatch = textNewBatch;
        readUserDetails.textStudentMobileNumber = textNewStudentMobileNumber;
        readUserDetails.textParentMobileNumber = textNewParentMobileNumber;

        DatabaseReference newDataRef = studentsRef.child(textNewAcademicYear).child(textYearSelected).child(firebaseUser.getUid());
        newDataRef.setValue(readUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    if (!(textNewAcademicYear.equals(oldAcademicYear)) || !(textYearSelected.equals(oldYear))) {
                        oldDataRef.removeValue(new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                saveCachedUserData();
                                loading.setVisibility(View.GONE);
                                Intent intent = new Intent(UpdateUserDataActivity.this, UserProfileActivity.class);
                                startActivity(intent);
                                Toast.makeText(UpdateUserDataActivity.this, "Your are upgrade to new Year", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
                    }else {
                        saveCachedUserData();
                        loading.setVisibility(View.GONE);
                        Intent intent = new Intent(UpdateUserDataActivity.this, UserProfileActivity.class);
                        startActivity(intent);
                        Toast.makeText(UpdateUserDataActivity.this, "Data update successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loading.setVisibility(View.GONE);
                Toast.makeText(UpdateUserDataActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void saveCachedUserData() {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(ACADEMIC_YEAR, textNewAcademicYear);
        editor.putString(YEAR, textYearSelected);
        editor.putString(ROLL_NO, textNewRollNo);
        editor.putString(BATCH, textNewBatch);
        editor.putString(STUDENT_MOBILE_NUMBER, textNewStudentMobileNumber);
        editor.putString(PARENT_MOBILE_NUMBER, textNewParentMobileNumber);

        editor.apply();
    }
}