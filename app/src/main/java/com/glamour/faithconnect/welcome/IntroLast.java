package com.glamour.faithconnect.welcome;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.glamour.faithconnect.R;
import com.glamour.faithconnect.emailAuth.LoginActivity;
import com.glamour.faithconnect.menu.PrivacyActivity;
import com.glamour.faithconnect.menu.TermsActivity;
import com.glamour.faithconnect.phoneAuth.GenerateOTPActivity;

@SuppressWarnings("ALL")
public class IntroLast extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_last);

        Button email = findViewById(R.id.next);
        Button phone = findViewById(R.id.phone);

        email.setOnClickListener(v -> startActivity( new Intent(getApplicationContext(), LoginActivity.class )));

        phone.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), GenerateOTPActivity.class )));

        TextView terms = findViewById(R.id.terms);
        TextView privacy = findViewById(R.id.privacy);

        privacy.setOnClickListener(v -> {
            startActivity(new Intent(IntroLast.this, PrivacyActivity.class));
        });

        terms.setOnClickListener(v -> {
            startActivity(new Intent(IntroLast.this, TermsActivity.class));
        });

    }
}