package com.example.journalapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;

import model.Journal;
import util.JournalApi;

public class PostJournalActivity extends AppCompatActivity implements View.OnClickListener {
    private static  final int GALLERY_CODE=1;

    private Button saveButton;
    private EditText titleEditText,thoughtEditText;
    private ImageView addPhotoButton,imageView;
    private ProgressBar progressBar;
    private TextView currentUserTextView;

    private String currentUserId;
    private String currrentUsername;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    private FirebaseUser user;


    private FirebaseFirestore db= FirebaseFirestore.getInstance();

    private StorageReference storageReference;

    private CollectionReference collectionReference = db.collection("Journal");
    private Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_journal);

        storageReference= FirebaseStorage.getInstance().getReference();


        firebaseAuth=FirebaseAuth.getInstance();
        progressBar=findViewById(R.id.postJournalProgressBar);
        saveButton=findViewById(R.id.postJournalsavebutton);
        titleEditText=findViewById(R.id.postJournal_title_edittext);
        thoughtEditText=findViewById(R.id.postJournal_thought_editText);
        currentUserTextView=findViewById(R.id.postJournal_username_textview);
        addPhotoButton=findViewById(R.id.postJournalCameraButton);
        imageView=findViewById(R.id.postJournalImageView);
        progressBar.setVisibility(View.INVISIBLE);

        addPhotoButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);

        if(JournalApi.getInstance()!=null)
        {
            currentUserId=JournalApi.getInstance().getUserId();
            currrentUsername=JournalApi.getInstance().getUsername();

            currentUserTextView.setText(currrentUsername);
        }

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user=firebaseAuth.getCurrentUser();
                if(user!=null)
                {

                }
                else{

                }
            }
        };







    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.postJournalsavebutton:
                //save data in firestore
                saveJournal();
                
                
                break;

            case R.id.postJournalCameraButton:
                //addphotofrom gallery or memory

                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERY_CODE);
                
                
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + view.getId());
        }

    }

    private void saveJournal() {
        String title= titleEditText.getText().toString().trim();
        String thoughts = thoughtEditText.getText().toString().trim();
        progressBar.setVisibility(View.VISIBLE);

        if(!TextUtils.isEmpty(title)&&
        !TextUtils.isEmpty(thoughts)&&
        imageUri!=null)
        {
            StorageReference filepath = storageReference
                    .child("journal_image")
                    .child("my_image"+ Timestamp.now().getSeconds());

            filepath.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                            filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    String imageUrl= uri.toString();

                                    Journal journal= new Journal();
                                    journal.setTitle(title);
                                    journal.setThought(thoughts);
                                    journal.setImageUri(imageUrl);
                                    journal.setTimeAdded(new Timestamp(new Date()));
                                    journal.setUserName(currrentUsername);
                                    journal.setUserId(currentUserId);


                                    collectionReference.add(journal).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            progressBar.setVisibility(View.INVISIBLE);
                                            startActivity(new Intent(PostJournalActivity.this,JournalListActivity.class));
                                            finish();

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                        }
                                    });

                                }
                            });

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("postError", "onFailure: "+e.getMessage());


                        }
                    });

        }
        else{
            progressBar.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_CODE&&resultCode==RESULT_OK)
        {
            if(data!=null)
            {
                imageUri=data.getData();
                imageView.setImageURI(imageUri);
                Log.d("activity", "onActivityResult: "+resultCode);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        user= firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(firebaseAuth!=null)
        {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}