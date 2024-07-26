package com.ermayurmahajan.mcoestudentapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
public class UserProfileActivity extends AppCompatActivity  {
    private static final String PREFS_NAME = "UserPrefs";
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private String folderName;
    private ImageView imgUserProfilePic;
    Bitmap qrCodeWithText;
    private TextView txtWelcomeUser, txtDOB, txtAcademicYear, txtYear,txtRollNo, txtBatch, txtStudentMobileNumber, txtParentName, txtParentMobileNumber;
    private LottieAnimationView loading;
    private String textStudentFullName, textAcademicYear, textDOB, textYear, textRollNo, textBatch, textStudentMobileNumber, textParentName, textParentMobileNumber;
    private FirebaseAuth authProfile;
    private FirebaseUser firebaseUser;
    private ReadWriteDetails readUserDetails;
    private static String userID;
    private static String STUDENT_FULL_NAME, ACADEMIC_YEAR, YEAR, ROLL_NO, BATCH, DOB, STUDENT_MOBILE_NUMBER, PARENT_NAME, PARENT_MOBILE_NUMBER;
    private DatabaseReference studentsRef, yearRef;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private String newProfilePictureUrl;
    private boolean userFound = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile_activity);
        getSupportActionBar().setTitle("Profile");

        loading = findViewById(R.id.loading);
        txtWelcomeUser = findViewById(R.id.Utxt_welcome_user);
        imgUserProfilePic = findViewById(R.id.img_user_profile_pic);

        txtDOB = findViewById(R.id.txt_DOB);
        txtAcademicYear = findViewById(R.id.txt_academic_year);
        txtYear = findViewById(R.id.txt_year);
        txtRollNo = findViewById(R.id.txt_roll_no);
        txtBatch = findViewById(R.id.txt_batch);
        txtStudentMobileNumber = findViewById(R.id.txt_student_mobile_number);
        txtParentName = findViewById(R.id.txt_parent_full_name);
        txtParentMobileNumber = findViewById(R.id.txt_parent_mobile_number);

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();
        userID = firebaseUser.getUid();

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // Replace "folderName" with the name of the folder you want to delete
        folderName = "Profile Picture/" + userID;

        STUDENT_FULL_NAME = userID + "userFullName";
        ACADEMIC_YEAR = userID + "academicYear";
        YEAR = userID + "year";
        ROLL_NO = userID + "rollNo";
        BATCH = userID + "batch";
        DOB = userID + "dob";
        STUDENT_MOBILE_NUMBER = userID + "studentMobileNumber";
        PARENT_NAME = userID + "parentName";
        PARENT_MOBILE_NUMBER = userID + "parentMobileNumber";

        FirebaseApp.initializeApp(this);
        studentsRef = FirebaseDatabase.getInstance().getReference().child("Registered Students");

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        textStudentFullName = prefs.getString(STUDENT_FULL_NAME, null);
        textAcademicYear = prefs.getString(ACADEMIC_YEAR, null);
        textYear = prefs.getString(YEAR, null);
        textRollNo = prefs.getString(ROLL_NO, null);
        textBatch = prefs.getString(BATCH, null);
        textDOB = prefs.getString(DOB, null);
        textStudentMobileNumber = prefs.getString(STUDENT_MOBILE_NUMBER, null);
        textParentName = prefs.getString(PARENT_NAME, null);
        textParentMobileNumber = prefs.getString(PARENT_MOBILE_NUMBER, null);

        if (textStudentFullName != null && !textStudentFullName.isEmpty() ) {
            setData(textStudentFullName, textDOB, textAcademicYear, textYear, textRollNo, textBatch, textStudentMobileNumber, textParentName, textParentMobileNumber);

        } else if (firebaseUser != null) {
            // Fetch user data from the database
            showUserProfile(firebaseUser);
        }
        if (firebaseUser != null) {
            try {
                Uri uri = firebaseUser.getPhotoUrl();

                //Set User's current DP in ImageView
                Glide.with(UserProfileActivity.this).load(Uri.parse(uri.toString())).placeholder(R.drawable.profile_loading).diskCacheStrategy(DiskCacheStrategy.ALL).into(imgUserProfilePic);

            }catch (Exception e){
                Toast.makeText(this, "Upload Profile Picture", Toast.LENGTH_SHORT).show();
            }
            DatabaseReference userRef = FirebaseHelper.getUserRef(firebaseUser.getUid());
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        newProfilePictureUrl = dataSnapshot.child("profilePictureUrl").getValue(String.class);
                        if (newProfilePictureUrl != null) {
                            Glide.with(UserProfileActivity.this).load(Uri.parse(newProfilePictureUrl)).placeholder(R.drawable.profile_loading).diskCacheStrategy(DiskCacheStrategy.ALL).into(imgUserProfilePic);
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else {
            Toast.makeText(UserProfileActivity.this, "Something went wrong! User details are not available at this moment", Toast.LENGTH_SHORT).show();
            loading.setVisibility(View.GONE);
        }

        imgUserProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserProfileActivity.this, UploadProfilePicActivity.class);
                startActivity(intent);
            }
        });
    }

    private void showUserProfile(FirebaseUser firebaseUser) {


        studentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // This method will be called whenever the data at the root reference changes

                // Loop through all academic years
                for (DataSnapshot academicYearSnapshot : dataSnapshot.getChildren()) {
                    if (userFound) {
                        break; // Exit loop if user is found
                    }

                    String academicYear = academicYearSnapshot.getKey();
                    yearRef = FirebaseDatabase.getInstance().getReference().child("Registered Students").child(academicYear);

                    yearRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (userFound) {
                                return; // Exit listener if user is found
                            }

                            for (DataSnapshot classSnapshot : snapshot.getChildren()) {
                                String selectedYear= classSnapshot.getKey();

                                studentsRef.child(academicYear).child(selectedYear).child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        readUserDetails = snapshot.getValue(ReadWriteDetails.class);
                                        if (readUserDetails != null) {
                                            userFound = true; // Set flag to true
                                            // Set data
                                            textStudentFullName = readUserDetails.textStudentFullName;
                                            textAcademicYear = readUserDetails.textAcademicYear;
                                            textYear = readUserDetails.textYear;
                                            textRollNo = readUserDetails.textRollNo;
                                            textBatch = readUserDetails.textBatch;
                                            textDOB = readUserDetails.textDOB;
                                            textStudentMobileNumber = readUserDetails.textStudentMobileNumber;
                                            textParentName = readUserDetails.textParentFullName;
                                            textParentMobileNumber = readUserDetails.textParentMobileNumber;
                                            setData(textStudentFullName, textDOB, textAcademicYear, textYear, textRollNo, textBatch, textStudentMobileNumber, textParentName, textParentMobileNumber);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        // Handle onCancelled
                                    }
                                });

                                if (userFound) {
                                    break; // Exit loop if user is found
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Handle onCancelled
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled
            }
        });
    }
    private void setData(String textStudentFullName, String textDOB, String textAcademicYear, String textYear , String textRollNo, String textBatch, String textStudentMobileNumber, String textParentName, String textParentMobileNumber){
        txtWelcomeUser.setText("Welcome\n" + textStudentFullName);
        txtDOB.setText(textDOB);
        txtAcademicYear.setText(textAcademicYear);
        txtYear.setText(textYear);
        txtRollNo.setText(textRollNo);
        txtBatch.setText(textBatch);
        txtStudentMobileNumber.setText(textStudentMobileNumber);
        txtParentName.setText(textParentName);
        txtParentMobileNumber.setText(textParentMobileNumber);

        loading.setVisibility(View.GONE);
        generateQRCode();
    }
    private void generateQRCode() {
        String text = userID.trim();
        if (!text.isEmpty()) {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            try {
                // Adjust QR code size based on device screen width
                int screenWidth = getScreenWidth();
                int qrCodeSize = (int) (screenWidth * 0.5);

                // Generate the QR code as a Bitmap
                Bitmap bitmap = barcodeEncoder.encodeBitmap(text, BarcodeFormat.QR_CODE, qrCodeSize, (qrCodeSize + 200));

                // Add text to the QR code image
                String textToAdd = textStudentFullName + "\n" + textYear + " - " + textAcademicYear + "\n" + textRollNo;

                // Calculate the dimensions of the combined image (QR code + text)
                int combinedImageWidth = Math.max(bitmap.getWidth(), getTextWidth(textToAdd, 40));
                int combinedImageHeight = bitmap.getHeight() + getTextHeight(textToAdd, 40) ; // Add 50 for extra space

                // Create a new bitmap with the combined dimensions
                Bitmap combinedBitmap = Bitmap.createBitmap(combinedImageWidth, combinedImageHeight, Bitmap.Config.ARGB_8888);

                // Create a Canvas to draw on the new bitmap
                Canvas canvas = new Canvas(combinedBitmap);

                // Calculate the position to center the QR code
                int qrCodeX = (combinedImageWidth - bitmap.getWidth()) / 2;
                int qrCodeY = 0; // QR code is at the top

                // Calculate the position to center the text below the QR code
                int textX = (combinedImageWidth - getTextWidth(textToAdd, 40)) / 2;
                int textY = qrCodeY + bitmap.getHeight() - 40; // Adjust for spacing

                // Draw the QR code on canvas
                canvas.drawBitmap(bitmap, qrCodeX, qrCodeY, null);

                // Draw the text below the QR code on canvas
                drawTextOnCanvas(canvas, textToAdd, textX, textY, 40);

                // Now 'combinedBitmap' contains the centered QR code and text
                // You can use 'combinedBitmap' as needed
                qrCodeWithText = combinedBitmap;

            } catch (WriterException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Failed to generate QR code", Toast.LENGTH_SHORT).show();
        }
    }
    private void drawTextOnCanvas(Canvas canvas, String text, int x, int y, float textSize) {
        // Create a Paint Object for styling
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(textSize);

        // Split the text into lines
        String[] lines = text.split("\n");

        // Calculate total height needed for text
        float totalTextHeight = lines.length * (paint.descent() - paint.ascent() + 5);

        // Calculate starting y-coordinate to center the text
        float startY = y - totalTextHeight / 2;

        // Draw each line of text on canvas
        for (String line : lines) {
            canvas.drawText(line, x, startY, paint);
            // Adjust the y-coordinate for the next line
            startY += paint.descent() - paint.ascent() + 5; // Add 5 for spacing
        }
    }
    private int getTextWidth(String text, float textSize) {
        Paint paint = new Paint();
        paint.setTextSize(textSize);
        String[] lines = text.split("\n");
        int maxWidth = 0;
        for (String line : lines) {
            int width = (int) paint.measureText(line);
            if (width > maxWidth) {
                maxWidth = width;
            }
        }
        return maxWidth;
    }

    private int getTextHeight(String text, float textSize) {
        Paint paint = new Paint();
        paint.setTextSize(textSize);
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        return bounds.height();
    }

    private void requestStoragePermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_WRITE_EXTERNAL_STORAGE);
            }  else {
                saveQRCodeImage(qrCodeWithText);
            }
        }else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
            }  else {
                saveQRCodeImage(qrCodeWithText);
            }
        }
    }

    private void saveQRCodeImage(Bitmap qrCodeWithText) {
        String fileName = "QR_" + userID + ".png";
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), fileName);

        if (file.exists()) {
            Toast.makeText(this, "First Delete Old QR in Pictures Folder", Toast.LENGTH_SHORT).show();
            return; // Do not proceed with saving if the file already exists
        }


        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            qrCodeWithText.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            Toast.makeText(this, "QR code is saved to: Pictures folder", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save QR code " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, generate and save the QR code
                saveQRCodeImage(qrCodeWithText);
            } else {
                // Permission denied, handle accordingly (e.g., show a message)
                Toast.makeText(this, "QR code does not save until you give storage permission", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private int getScreenWidth() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        return displayMetrics.widthPixels;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveCachedUserData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveCachedUserData();
    }
    private void saveCachedUserData() {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(STUDENT_FULL_NAME, textStudentFullName);
        editor.putString(ACADEMIC_YEAR, textAcademicYear);
        editor.putString(YEAR, textYear);
        editor.putString(ROLL_NO, textRollNo);
        editor.putString(BATCH, textBatch);
        editor.putString(DOB, textDOB);
        editor.putString(STUDENT_MOBILE_NUMBER, textStudentMobileNumber);
        editor.putString(PARENT_NAME, textParentName);
        editor.putString(PARENT_MOBILE_NUMBER, textParentMobileNumber);

        editor.apply();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.navigation_drawer_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.download_qr_menu:
                requestStoragePermission();
                return true;
            case R.id.change_password_menu:
                Intent intent = new Intent(getApplicationContext(), ForgetPasswordActivity.class);
                startActivity(intent);
                return true;
            case R.id.logout_menu:
                loading.setVisibility(View.GONE);
                authProfile.signOut();
                Toast.makeText(UserProfileActivity.this, "Logout Successful", Toast.LENGTH_SHORT).show();
                Intent intent2 = new Intent(UserProfileActivity.this, MainActivity.class);
                intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent2);
                finish(); // to close UserProfile Activity Activity
                return true;
            case R.id.update_std_menu:
                Intent intent3 = new Intent(UserProfileActivity.this, UpdateUserDataActivity.class);
                startActivity(intent3);
                finish();
                return true;
            case R.id.delete_account_menu:
                sendVerificationEmailDialogBox();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void sendVerificationEmailDialogBox(){
        Dialog dialog = new Dialog(UserProfileActivity.this);
        dialog.setContentView(R.layout.custom_dialog_layout);

        Button btnDialogContinue = dialog.findViewById(R.id.btn_dialog_continue);
        ImageView imgDialog = dialog.findViewById(R.id.img_dilog);
        TextView txtDialogBoxTitle = dialog.findViewById(R.id.txt_dialog_box_title);
        TextView txtDialogBoxContext = dialog.findViewById(R.id.txt_dialog_box_context);

        imgDialog.setImageResource(R.drawable.baseline_delete_white);
        txtDialogBoxTitle.setText("Delete Account!");
        txtDialogBoxContext.setText("You want to delete your account?");
        btnDialogContinue.setText("Yes");
        //set Size
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());

        btnDialogContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteAccount();
                loading.setVisibility(View.VISIBLE);
                dialog.dismiss();
            }
        });
        dialog.show();
    }
    private void deleteAccount() {
        loading.setVisibility(View.VISIBLE);
        // Get a reference to the folder you want to delete
        StorageReference folderRef = storageRef.child(folderName);
        folderRef.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        List<StorageReference> items = listResult.getItems();
                        for (StorageReference item : items) {
                            item.delete().addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(UserProfileActivity.this, "Error deleting item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // An error occurred while listing folder contents
                        Toast.makeText(UserProfileActivity.this, "Error listing folder contents: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        loading.setVisibility(View.VISIBLE);
        DatabaseReference deleteAccount = studentsRef.child(textAcademicYear).child(textYear).child(userID);
        deleteAccount.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    firebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                loading.setVisibility(View.GONE);
                                Toast.makeText(UserProfileActivity.this, "Account Deleted", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(UserProfileActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }else {
                                loading.setVisibility(View.GONE);
                                Toast.makeText(UserProfileActivity.this, "Something went wrong1"+ task.getException(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else {
                    loading.setVisibility(View.GONE);
                    Toast.makeText(UserProfileActivity.this, "Something went wrong2"+ task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}