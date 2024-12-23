package com.example.footfitstore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.footfitstore.R;
import com.example.footfitstore.Utils.CustomDialog;
import com.example.footfitstore.activity.Admin.MainActivity_Admin;
import com.example.footfitstore.activity.User.MainActivity;
import com.example.footfitstore.activity.User.ResetPasswordActivity;
import com.example.footfitstore.model.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 123;
    private EditText emailLogin, passwordLogin;
    private Button btnLogin;
    private TextView tvRegister,tvRecovery;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        emailLogin = findViewById(R.id.emailLogin);
        passwordLogin = findViewById(R.id.passwordLogin);
        btnLogin = findViewById(R.id.btnLogin);
        LinearLayout btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        tvRegister = findViewById(R.id.tvRegister);
        tvRecovery = findViewById(R.id.tvRecovery);

        btnLogin.setOnClickListener(v -> loginUser());
        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
        tvRecovery.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
        });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                new CustomDialog(LoginActivity.this)
                        .setTitle("Login Failed")
                        .setMessage("Google sign in failed. Please try again.")
                        .setIcon(R.drawable.error)
                        .setPositiveButton("OK", null)
                        .hideNegativeButton()
                        .show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        String uid = user != null ? user.getUid() : null;
                        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Users");
                        databaseReference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (!snapshot.exists())
                                {
                                    addUser(user,uid,databaseReference);
                                }
                                else
                                {
                                    if (Objects.equals(snapshot.child("status").getValue(String.class), "active"))
                                    {
                                        loginSuccess(uid);
                                    }
                                    else {
                                        new CustomDialog(LoginActivity.this)
                                                .setTitle("Login Failed")
                                                .setMessage("Your account has been banned.")
                                                .setIcon(R.drawable.error)
                                                .setPositiveButton("OK", null)
                                                .show();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    } else {
                        new CustomDialog(LoginActivity.this)
                                .setTitle("Login Failed")
                                .setMessage("Authentication Failed")
                                .setIcon(R.drawable.error)
                                .setPositiveButton("OK", null)
                                .hideNegativeButton()
                                .show();
                    }
                });
    }

    private void loginUser() {
        String email = emailLogin.getText().toString().trim();
        String password = passwordLogin.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            new CustomDialog(LoginActivity.this)
                    .setTitle("Login Failed")
                    .setMessage("Please fill all fields")
                    .setIcon(R.drawable.error)
                    .hideNegativeButton()
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        String uid = user != null ? user.getUid() : null;
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
                        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists())
                                {
                                    if (Objects.equals(snapshot.child("status").getValue(String.class), "active"))
                                    {
                                        loginSuccess(uid);
                                    }
                                    else {
                                        new CustomDialog(LoginActivity.this)
                                                .setTitle("Login Failed")
                                                .setMessage("Your account has been banned.")
                                                .setIcon(R.drawable.error)
                                                .setPositiveButton("OK", null)
                                                .show();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    } else {
                        new CustomDialog(LoginActivity.this)
                                .setTitle("Login Failed")
                                .setMessage("Email Address or Password incorrect.")
                                .setIcon(R.drawable.error)
                                .setPositiveButton("OK", null)
                                .hideNegativeButton()
                                .show();
                    }
                });
    }
    private void loginSuccess(String uid)
    {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("role");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    if (Objects.equals(snapshot.getValue(String.class), "admin"))
                    {
                        startActivity(new Intent(LoginActivity.this, MainActivity_Admin.class));
                        finish();
                    }
                    else
                    {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void addUser(FirebaseUser user, String uid, DatabaseReference databaseReference)
    {
        User user1 = new User(user.getEmail());
        databaseReference.child(uid).setValue(user1);
        databaseReference.child(uid).child("role").setValue("user");
    }
}
