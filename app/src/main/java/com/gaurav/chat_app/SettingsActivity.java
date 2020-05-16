package com.gaurav.chat_app;

import android.app.ProgressDialog;
import android.arch.core.executor.ArchTaskExecutor;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    private Button UpdateAccountSettings;
    private EditText username, userstatus;
    private CircleImageView userprofileimage;
    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference rootref;
    private static final int gallerypic = 1;
    private StorageReference userprofileimageref;
    private ProgressDialog loadingBar;

    private String retrieveprofileimage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        rootref = FirebaseDatabase.getInstance().getReference();
        userprofileimageref = FirebaseStorage.getInstance().getReference().child("Profile Images");

        initalisefields();
        UpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSettings();
            }
        });

        RetriveUserInfo();

        userprofileimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, gallerypic);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == gallerypic && resultCode==RESULT_OK && data!=null){
            Uri imageuri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK){
                loadingBar.setTitle("Set profile image");
                loadingBar.setMessage("Please Wait");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                Uri resulturi = result.getUri();
                final StorageReference filePath = userprofileimageref.child(currentUserID + ".jpg");
                filePath.putFile(resulturi).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String uri1 = uri.toString();
                                rootref.child("Users").child(currentUserID).child("image").setValue(uri1);
                                loadingBar.dismiss();
                            }
                        });
                    }
                });
            }
        }
    }

    private void RetriveUserInfo() {
        rootref.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))
                                && (dataSnapshot.hasChild("image"))){
                            String retrieveusername = dataSnapshot.child("name").getValue().toString();
                            String retrieveuserstatus = dataSnapshot.child("status").getValue().toString();
                            retrieveprofileimage = dataSnapshot.child("image").getValue().toString();
                            Picasso.get().load(retrieveprofileimage).into(userprofileimage);

                            username.setText(retrieveusername);
                            userstatus.setText(retrieveuserstatus);

                        }
                        else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))){
                            String retrieveusername = dataSnapshot.child("name").getValue().toString();
                            String retrieveuserstatus = dataSnapshot.child("status").getValue().toString();
                            
                            username.setText(retrieveusername);
                            userstatus.setText(retrieveuserstatus);
                        }
                        else{
                            Toast.makeText(SettingsActivity.this, "Please set & update profile information", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
    private void UpdateSettings() {
        String setusername = username.getText().toString().trim();
        String setuserstatus = userstatus.getText().toString().trim();
        if(TextUtils.isEmpty(setusername)){
            Toast.makeText(this, "Please enter name", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(setuserstatus)){
            Toast.makeText(this, "Please enter any status", Toast.LENGTH_SHORT).show();
        }
        else{
            HashMap<String, String> profilemap = new HashMap<>();
            profilemap.put("uid", currentUserID);
            profilemap.put("name", setusername);
            profilemap.put("status", setuserstatus);
            profilemap.put("image", retrieveprofileimage);
            rootref.child("Users").child(currentUserID).setValue(profilemap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                SendUserToMainActivity();
                                Toast.makeText(SettingsActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                String msg = task.getException().toString();
                                Toast.makeText(SettingsActivity.this, "msg", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
    private void initalisefields() {
        UpdateAccountSettings = (Button) findViewById(R.id.update_settings_button);
        username = (EditText) findViewById(R.id.set_user_name);
        userstatus = (EditText) findViewById(R.id.set_profile_status);
        userprofileimage = (CircleImageView) findViewById(R.id.set_profile_image);
        loadingBar = new ProgressDialog(this);
    }
}
