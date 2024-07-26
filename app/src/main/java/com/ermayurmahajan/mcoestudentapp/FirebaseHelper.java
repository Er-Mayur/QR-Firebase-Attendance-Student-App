package com.ermayurmahajan.mcoestudentapp;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseHelper {

    @NonNull
    public static DatabaseReference getUserRef(String userId) {
        return FirebaseDatabase.getInstance().getReference("users").child(userId);
    }

    public static void updateProfilePicture(String newProfilePictureUrl) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();

            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(Uri.parse(newProfilePictureUrl))
                    .build();

            firebaseUser.updateProfile(profileUpdates);

            DatabaseReference userRef = getUserRef(userId);
            userRef.child("profilePictureUrl").setValue(newProfilePictureUrl);
        }
    }
}
