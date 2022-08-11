package com.example.journalapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import model.Journal;
import util.JournalApi;

public class LoginActivity extends AppCompatActivity {
    private Button loginButton,createAccountButton;

    private AutoCompleteTextView emailAddress;
    private EditText password;
    private ProgressBar mProgressBar;
    private FirebaseAuth firebaseAuth;
    private  FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mProgressBar= findViewById(R.id.Login_progress);

        firebaseAuth=FirebaseAuth.getInstance();
        emailAddress=findViewById(R.id.login_email);
        password=findViewById(R.id.login_password);

        loginButton=findViewById(R.id.login_email_sign_button);
        createAccountButton=findViewById(R.id.login_email_create_new_account_button);

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,CreateAccountActivity.class));
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginEmailPasswordUser(emailAddress.getText().toString().trim(),password.getText().toString().trim());

            }
        });
    }

    private void loginEmailPasswordUser(String email, String pwd) {

        mProgressBar.setVisibility(View.VISIBLE);

        if(!TextUtils.isEmpty(email)&&
        !TextUtils.isEmpty(pwd))
        {
            firebaseAuth.signInWithEmailAndPassword(email,pwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    FirebaseUser user= firebaseAuth.getCurrentUser();
                    assert user != null;
                    String currentUserId=user.getUid();

                    collectionReference.whereEqualTo("userId",currentUserId)
                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                    if(error!=null)
                                    {

                                    }
                                    assert value != null;
                                    if(!value.isEmpty())
                                    {mProgressBar.setVisibility(View.INVISIBLE);
                                        for(QueryDocumentSnapshot snapshot:value)
                                        {
                                            JournalApi journalApi = JournalApi.getInstance();
                                            journalApi.setUsername(snapshot.getString("username"));
                                            journalApi.setUserId(snapshot.getString("userId"));

                                            startActivity(new Intent(LoginActivity.this,PostJournalActivity.class));
                                        }
                                    }

                                }
                            });



                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    mProgressBar.setVisibility(View.INVISIBLE);

                }
            });

        }
        else{
            mProgressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(LoginActivity.this, "Enter email and password", Toast.LENGTH_SHORT).show();
        }

    }
}