package com.example.soundrack24;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.credentials.exceptions.NoCredentialException;

import com.google.android.gms.common.SignInButton;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private CredentialManager credentialManager;
    private Executor executor;
    private CancellationSignal cancellationSignal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        credentialManager = CredentialManager.create(this);
        executor = Executors.newSingleThreadExecutor();
        cancellationSignal = new CancellationSignal();

        SignInButton loginButton = findViewById(R.id.google_signin_button);
        loginButton.setSize(SignInButton.SIZE_WIDE);
        loginButton.setOnClickListener(v -> initiateSignIn());
    }

    private void initiateSignIn() {
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.default_web_client_id))
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(
                this,
                request,
                cancellationSignal,
                executor,
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        runOnUiThread(() -> {
                            try {
                                Credential credential = result.getCredential();
                                if (credential instanceof CustomCredential) {
                                    CustomCredential customCredential = (CustomCredential) credential;
                                    Bundle credentialData = customCredential.getData();
                                    GoogleIdTokenCredential googleCredential = GoogleIdTokenCredential.createFrom(credentialData);
                                    String idToken = googleCredential.getIdToken();

                                    if (idToken != null) {
                                        FirebaseAuth mAuth = FirebaseAuth.getInstance();
                                        AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);

                                        mAuth.signInWithCredential(firebaseCredential)
                                                .addOnCompleteListener(LoginActivity.this, task -> {
                                                    if (task.isSuccessful()) {
                                                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                                        finish();
                                                    } else {
                                                        Toast.makeText(LoginActivity.this, "Firebase Sign-in failed", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(LoginActivity.this, "No Google ID token", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(LoginActivity.this, "Credential is not of type Google ID!", Toast.LENGTH_SHORT).show();
                                    Log.w("LoginActivity", "Credential is not GoogleIdTokenCredential");
                                }
                            } catch (Exception e) {
                                Toast.makeText(LoginActivity.this, "Sign-in parsing failed", Toast.LENGTH_SHORT).show();
                                Log.e("LoginActivity", "Failed to parse credential", e);
                            }
                        });
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        if (e instanceof NoCredentialException) {
                            runOnUiThread(() -> Toast.makeText(LoginActivity.this, "No credentials available", Toast.LENGTH_SHORT).show());
                        } else {
                            runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Sign-in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                        Log.e("LoginActivity", "CredentialManager error", e);
                    }
                }
        );
    }

}
