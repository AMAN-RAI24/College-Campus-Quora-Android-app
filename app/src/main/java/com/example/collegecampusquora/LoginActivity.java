package com.example.collegecampusquora;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private Button button_signIn;
    private EditText email;
    private EditText password;
    private TextView forgotpass;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        email = ((TextInputLayout)findViewById(R.id.login_email_input)).getEditText();
        password = ((TextInputLayout)findViewById(R.id.login_password_input)).getEditText();
        forgotpass = findViewById(R.id.forgot_password);
        forgotpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               String emailAddress = email.getText().toString();
               sendPasswordResetEmail(emailAddress);
            }
        });

        button_signIn = findViewById(R.id.button_signIn);
        button_signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                  loginUser();
            }
        });
    }

    private void sendPasswordResetEmail(String emailAddress) {
        if(TextUtils.isEmpty(emailAddress)){
            Toast.makeText(getApplicationContext(), "Email Field Empty. Try Again", Toast.LENGTH_SHORT).show();
            return;
        }
         mAuth.sendPasswordResetEmail(emailAddress).addOnCompleteListener(new OnCompleteListener<Void>() {
             @Override
             public void onComplete(@NonNull Task<Void> task) {
                 if(task.isSuccessful()) {
                     Toast.makeText(getApplicationContext(), "Reset Link Sent", Toast.LENGTH_SHORT).show();
                 } else {
                     String[] messages = task.getException().getMessage().split(":");
                     Toast.makeText(getApplicationContext(), messages[messages.length-1], Toast.LENGTH_SHORT).show();
                 }
             }
         });
    }

    private void loginUser() {
        String Email = email.getText().toString().trim();
        String Password = password.getText().toString().trim();

        mAuth.signInWithEmailAndPassword(Email, Password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("tag", "signInWithEmail:success");
                            finish();
                            startActivity(new Intent(getApplicationContext(),MainActivity.class));
//                            FirebaseUser user = mAuth.getCurrentUser();
//                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("tag", "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                            // ...
                        }

                        // ...
                    }
                });
    }
}