package com.example.whatsappclone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.whatsappclone.databinding.ActivityPhoneNumberBinding;

public class PhoneNumberActivity extends AppCompatActivity {

    private ActivityPhoneNumberBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPhoneNumberBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        binding.continueBtn.setOnClickListener(view -> {
            Intent intent = new Intent(PhoneNumberActivity.this, OTPActivity.class);
            intent.putExtra("phoneNumber", binding.phoneBox.getText().toString());
            startActivity(intent);
        });
    }
}