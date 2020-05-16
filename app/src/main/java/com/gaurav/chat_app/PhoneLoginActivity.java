package com.gaurav.chat_app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {
    private Button sendverificationbutton, verifybutton;
    private EditText inputphonenumber, inputverificationcode;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);
        mAuth = FirebaseAuth.getInstance();
        initialise();
        loadingBar = new ProgressDialog(this);
        sendverificationbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = inputphonenumber.getText().toString();
                if(TextUtils.isEmpty(phoneNumber)){
                    Toast.makeText(PhoneLoginActivity.this, "Enter valid number", Toast.LENGTH_SHORT).show();
                }
                else {
                    loadingBar.setTitle("Phone verification");
                    loadingBar.setMessage("Please wait while we authenticate your number");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            PhoneLoginActivity.this,               // Activity (for callback binding)
                            callbacks);        // OnVerificationStateChangedCallbacks
                }
            }
        });
        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Invalid, please enter phone number with country code", Toast.LENGTH_SHORT).show();
                sendverificationbutton.setVisibility(View.VISIBLE);
                inputphonenumber.setVisibility(View.VISIBLE);
                verifybutton.setVisibility(View.INVISIBLE);
                inputverificationcode.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                mVerificationId = verificationId;
                mResendToken = token;

                loadingBar.dismiss();
                sendverificationbutton.setVisibility(View.INVISIBLE);
                inputphonenumber.setVisibility(View.INVISIBLE);
                verifybutton.setVisibility(View.VISIBLE);
                inputverificationcode.setVisibility(View.VISIBLE);
            }
        };

        verifybutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendverificationbutton.setVisibility(View.INVISIBLE);
                inputphonenumber.setVisibility(View.INVISIBLE);

                String verificationcode = inputverificationcode.getText().toString();
                if(TextUtils.isEmpty(verificationcode)){
                    Toast.makeText(PhoneLoginActivity.this, "Enter code first", Toast.LENGTH_SHORT).show();
                }
                else {
                    loadingBar.setTitle("verification code");
                    loadingBar.setMessage("Please wait while we verify your code");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationcode);
                    signInWithPhoneAuthCredential(credential);
                }

            }
        });
    }

    private void initialise() {
        sendverificationbutton = (Button) findViewById(R.id.send_ver_code_button);
        verifybutton = (Button) findViewById(R.id.verify_button);
        inputphonenumber = (EditText) findViewById(R.id.phone_number_input);
        inputverificationcode = (EditText) findViewById(R.id.verification_code_input);
    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            loadingBar.dismiss();
                            Toast.makeText(PhoneLoginActivity.this, "logged in success", Toast.LENGTH_SHORT).show();
                            SendUserToMMainActivity();
                        }
                        else
                        {
                            String msg = task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void SendUserToMMainActivity() {
        Intent mainactivity = new Intent(PhoneLoginActivity.this, MainActivity.class);
        startActivity(mainactivity);
        finish();
    }
}
