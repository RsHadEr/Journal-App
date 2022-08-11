package com.example.journalapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import util.JournalApi;

public class CreateAccountActivity extends AppCompatActivity {

    private EditText userNameEditText,emailEditText,passwordEditText;
    private Button createAccountButton;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currUser;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionRefernce= db.collection("Users");




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        createAccountButton = findViewById(R.id.create_account_create_button);
        userNameEditText=findViewById(R.id.create_account_username);
        emailEditText=findViewById(R.id.create_account_emailid);
        passwordEditText = findViewById(R.id.create_account_password);
        progressBar=findViewById(R.id.create_account_progress_bar);
        firebaseAuth= FirebaseAuth.getInstance();

        authStateListener= new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currUser=firebaseAuth.getCurrentUser();
                if(currUser!=null)
                {
                    //user already logged in
                }
                else{

                }
            }
        };

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!TextUtils.isEmpty(userNameEditText.getText().toString())&&
                        !TextUtils.isEmpty(emailEditText.getText().toString())&&
                        !TextUtils.isEmpty(passwordEditText.getText().toString())) {
                    String email=emailEditText.getText().toString().trim();
                    String password=passwordEditText.getText().toString().trim();
                    String username=userNameEditText.getText().toString().trim();

                    createUserEmailAccount(email, password, username);

                }
                else{
                    Toast.makeText(CreateAccountActivity.this, "Empty fields", Toast.LENGTH_SHORT).show();
                }
            }
        });



    }


    private void createUserEmailAccount(String email, String password,String username)
    {
        if(!TextUtils.isEmpty(email)
        &&!TextUtils.isEmpty(password)
        &&!TextUtils.isEmpty(username))
        {
            progressBar.setVisibility(View.VISIBLE);

            firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful())
                    {
                        currUser=firebaseAuth.getCurrentUser();
                        String currentUserId=currUser.getUid();

                        Map<String,String>userObj= new HashMap<>();
                        userObj.put("userId",currentUserId);
                        userObj.put("username",username);

                        collectionRefernce.add(userObj).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(Objects.requireNonNull(task.getResult().exists()))
                                        {
                                            progressBar.setVisibility(View.INVISIBLE);
                                            String name=task.getResult().getString(username);
                                            Log.d("TAG", "onComplete: reached ");

                                            JournalApi journalApi= JournalApi.getInstance();
                                            journalApi.setUserId(currentUserId);
                                            journalApi.setUsername(username);

                                            Intent intent = new Intent(CreateAccountActivity.this,PostJournalActivity.class);
                                            startActivity(intent);

                                        }
                                        else{
                                            progressBar.setVisibility(View.INVISIBLE);
                                        }

                                    }
                                });


                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });

                    }
                    else{

                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("error", "onFailure: "+e.getMessage());

                }
            });


        }
        else{

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        currUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }
}