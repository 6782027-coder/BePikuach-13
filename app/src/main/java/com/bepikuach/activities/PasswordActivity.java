package com.bepikuach.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bepikuach.R;
import com.bepikuach.utils.PrefManager;
import com.google.android.material.textfield.TextInputEditText;

public class PasswordActivity extends AppCompatActivity {

    private PrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        prefManager = new PrefManager(this);

        TextInputEditText passwordInput = findViewById(R.id.passwordInput);
        TextView errorText = findViewById(R.id.errorText);
        Button confirmBtn = findViewById(R.id.confirmBtn);
        Button cancelBtn = findViewById(R.id.cancelBtn);
        String target = getIntent().getStringExtra("target");

        confirmBtn.setOnClickListener(v -> {
            String entered = passwordInput.getText() != null
                    ? passwordInput.getText().toString() : "";
            if (prefManager.checkPassword(entered)) {
                errorText.setVisibility(android.view.View.GONE);
                navigateToTarget(target);
            } else {
                errorText.setVisibility(android.view.View.VISIBLE);
                passwordInput.setText("");
            }
        });

        passwordInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                confirmBtn.performClick();
                return true;
            }
            return false;
        });

        cancelBtn.setOnClickListener(v -> finish());
    }

    private void navigateToTarget(String target) {
        if ("admin".equals(target)) {
            startActivity(new Intent(this, AdminActivity.class));
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
