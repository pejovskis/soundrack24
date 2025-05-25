package com.example.soundrack24;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

import Model.DatabaseHelper;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mAuth.getCurrentUser() == null) {
            // User not signed in, redirect to LoginActivity
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Proceed to load layout only if user is signed in
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(this);
        databaseHelper.populateBaseTables();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main, new MainLayout())
                    .commit();
        }
    }

}