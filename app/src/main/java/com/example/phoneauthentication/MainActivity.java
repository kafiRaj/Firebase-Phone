package com.example.phoneauthentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.chaos.view.PinView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private EditText phoneText, passText;
    private Button signUpBtn, verifyBtn;
    private LinearLayout registyerLay, otpLay;

    private PinView otpView;

    private String phoneNumber;
    private String mVerificationId;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        phoneText = findViewById(R.id.phoneId);
        passText = findViewById(R.id.passId);
        signUpBtn = findViewById(R.id.btnSignUpId);
        verifyBtn = findViewById(R.id.btnVerifyId);
        registyerLay = findViewById(R.id.lay1);
        otpLay = findViewById(R.id.lay2);
        otpView = findViewById(R.id.pinviewId);

        mAuth = FirebaseAuth.getInstance();

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!phoneText.getText().toString().isEmpty() && !passText.getText().toString().isEmpty()){

                    phoneNumber = "+88"+phoneText.getText().toString();

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            MainActivity.this,               // Activity (for callback binding)
                            mCallbacks);        // OnVerificationStateChangedCallbacks

                    registyerLay.setVisibility(View.GONE);
                    otpLay.setVisibility(View.VISIBLE);

                }
                else {
                    Toast.makeText(MainActivity.this, "invalid", Toast.LENGTH_SHORT).show();
                }
            }
        });

        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String verifyCode = otpView.getText().toString();
                if (verifyCode.isEmpty()){
                    Toast.makeText(MainActivity.this, "Enter Verify Code", Toast.LENGTH_SHORT).show();
                }
                else {

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId,verifyCode);
                    signInWithPhoneAuthCredential(credential, phoneNumber, passText.getText().toString());

                }
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {

                String passwordText = passText.getText().toString();

                signInWithPhoneAuthCredential(credential, phoneNumber, passwordText);
                otpView.setText(mVerificationId);

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                mVerificationId = verificationId;
                mResendToken = token;

                // ...
            }
        };


    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential, final String phone, final String pass) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d(TAG, "signInWithCredential:success");

                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            String uId = currentUser.getUid();

                            mDatabase = FirebaseDatabase.getInstance().getReference().child("User").child(uId);

                            HashMap<String, String> userMap = new HashMap<>();
                            userMap.put("Phone Number", phone);
                            userMap.put("password", pass);

                            mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){

                                        Toast.makeText(MainActivity.this, "save success", Toast.LENGTH_SHORT).show();

                                        Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            });


                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI
                            //Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }
}
