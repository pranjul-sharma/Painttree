package com.painttree.paintit;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.net.URI;

public class PostActivity extends AppCompatActivity {


    private static final int GALLERY_REQUEST = 1;
   // private AnimatedCircleLoadingView animatedCircleLoadingView;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseUser;
    private Uri mImageUri=null;
    private EditText mPostDesc;
    private EditText mPostTitle;
    private ImageButton mSelectImage;
    private ImageButton mProfileImage;
    private StorageReference mStorage;
    private Button mSubmitBtn;
    ProgressDialog mProgress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        mAuth=FirebaseAuth.getInstance();
        mCurrentUser=mAuth.getCurrentUser();
        mStorage= FirebaseStorage.getInstance().getReference();
        mDatabaseUser=FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());
        mDatabase= FirebaseDatabase.getInstance().getReference().child("Blog");
        mSelectImage = (ImageButton) findViewById(R.id.imageSelect);
        mProfileImage=(ImageButton)findViewById(R.id.setupImageBtn);
        mPostTitle = (EditText) findViewById(R.id.titleField);
        mPostDesc = (EditText) findViewById(R.id.descField);
        mSubmitBtn = (Button) findViewById(R.id.submitBtn);

        mProgress=new ProgressDialog(this);
        mSelectImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });
        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPosting();
            }
        });
    }

    private void startPosting() {
        mProgress.setMessage("Posting to Blog...");
        mProgress.show();
        final String title_val = mPostTitle.getText().toString().trim();
        final String desc_val = mPostDesc.getText().toString().trim();
        if (!TextUtils.isEmpty(title_val) && !TextUtils.isEmpty(desc_val) && mImageUri != null) {
            final StorageReference filepath=mStorage.child("Blog_Images").child(mImageUri.getLastPathSegment());
            filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                   final String downloadUrl=filepath.getDownloadUrl().getResult().toString();
                   final DatabaseReference newPost=mDatabase.push();


                    mDatabaseUser.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            newPost.child("title").setValue(title_val);
                            newPost.child("desc").setValue(desc_val);
                            newPost.child("image").setValue(downloadUrl);
                            newPost.child("uid").setValue(mCurrentUser.getUid());
                            newPost.child("time").setValue(System.currentTimeMillis());
                            newPost.child("profileImage").setValue(dataSnapshot.child("image").getValue());
                            newPost.child("username").setValue(dataSnapshot.child("name").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()){
                                        startActivity(new Intent(PostActivity.this, MainActivity.class));
                                    }

                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    mProgress.dismiss();

                }
            });
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            mImageUri = data.getData();
            mSelectImage.setImageURI(mImageUri);
        }
    }
}
