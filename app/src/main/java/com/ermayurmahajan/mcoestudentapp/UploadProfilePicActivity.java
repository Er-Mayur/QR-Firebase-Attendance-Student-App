package com.ermayurmahajan.mcoestudentapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class UploadProfilePicActivity extends AppCompatActivity {
    private LottieAnimationView loading;
    private ImageView imgSelectedProfilePicture;
    private static int PICK_IMAGE_REQUEST = 1;
    private  Uri uriImage;
    private FirebaseAuth authProfile;
    private StorageReference storageReference;
    private FirebaseUser firebaseUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_profile_pic_activity);
        getSupportActionBar().setTitle("Upload Profile Picture");

        Button btnChoosePicture = findViewById(R.id.btn_choose_picture);
        Button btnUploadProfilePicture = findViewById(R.id.btn_upload_profile_picture);
        loading = findViewById(R.id.loading);
        imgSelectedProfilePicture = findViewById(R.id.img_selected_profile_picture);

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference("Profile Picture");

        try {
            Uri uri = firebaseUser.getPhotoUrl();
            //Set User's current DP in ImageView
            Glide.with(UploadProfilePicActivity.this).load(Uri.parse(uri.toString())).placeholder(R.drawable.profile_loading).diskCacheStrategy(DiskCacheStrategy.ALL).into(imgSelectedProfilePicture);

        }catch (Exception e){
            Toast.makeText(this, "Select Profile Picture", Toast.LENGTH_SHORT).show();
        }

        btnChoosePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileManager();
            }
        });
        btnUploadProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loading.setVisibility(View.VISIBLE);
                userUploadPicture();
            }
        });

    }

    private void openFileManager() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            uriImage = data.getData();
            imgSelectedProfilePicture.setImageURI(uriImage);
        }
    }

    private void userUploadPicture(){
        if (uriImage != null){
            //save image to firebase
            StorageReference fileReference = storageReference.child(firebaseUser.getUid()).child(authProfile.getCurrentUser().getUid() + "." + getFileExtension(uriImage));

            //Upload picture to firebase
            fileReference.putFile(uriImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uri downloadUri = uri;
                            firebaseUser = authProfile.getCurrentUser();

                            // Finally set the display image of the after upload
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setPhotoUri(downloadUri).build();
                            firebaseUser.updateProfile(profileUpdates);
                            String newProfilePictureUrl = downloadUri.toString();
                            FirebaseHelper.updateProfilePicture(newProfilePictureUrl);


                            // Send back the updated profile picture status
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("profilePictureUpdated", true);
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        }
                    });
                    loading.setVisibility(View.GONE);
                    Toast.makeText(UploadProfilePicActivity.this, "Profile uploaded Successfully wait for change", Toast.LENGTH_LONG).show();

                    // After setting the display image of the user
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("profilePictureUpdated", true);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    loading.setVisibility(View.GONE);
                    Toast.makeText(UploadProfilePicActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }else {
            loading.setVisibility(View.GONE);
            Toast.makeText(this, "No file is Selected!", Toast.LENGTH_SHORT).show();
        }
    }
    private String getFileExtension(Uri uriImage){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uriImage));

    }
}