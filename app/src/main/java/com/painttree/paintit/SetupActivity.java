package com.painttree.paintit;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {
    private static final int GALLERY_REQUEST = 1;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;
    private StorageReference mImageStorage;
    private Uri mImageUri = null;
    private ProgressDialog mProgress;
    private ImageButton mSetupImageBtn;
    private EditText mSetupNameField;
    private Button mSubmitBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        mAuth = FirebaseAuth.getInstance();
        mImageStorage = FirebaseStorage.getInstance().getReference().child("Profile_images");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mProgress = new ProgressDialog(this);
        mSetupImageBtn = (ImageButton) findViewById(R.id.setupImageBtn);
        mSetupNameField = (EditText) findViewById(R.id.setupNameField);
        mSubmitBtn = (Button) findViewById(R.id.setupSubmitBtn);


        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strartSetupAccount();
            }
        });
        mSetupImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });
    }

    private void strartSetupAccount() {
        final String name = mSetupNameField.getText().toString().trim();
        final String user_id = mAuth.getCurrentUser().getUid();
        if (!TextUtils.isEmpty(name) && mImageUri != null) {
            mProgress.setMessage("finishing setup...");
            mProgress.show();
            final StorageReference filePath=mImageStorage.child(mImageUri.getLastPathSegment());
            filePath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    String downloadUri = filePath.getDownloadUrl().toString();
                    mDatabaseUsers.child(user_id).child("name").setValue(name);
                    mDatabaseUsers.child(user_id).child("image").setValue(downloadUri);
                    mProgress.dismiss();
                    Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);
                }
            });

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_REQUEST && resultCode==RESULT_OK){
            mImageUri=data.getData();
            CropImage.activity(mImageUri).setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1,1).start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode ==RESULT_OK) {
                mImageUri = result.getUri();
                mSetupImageBtn.setImageURI(mImageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                result.getError();
            }
        }
    }
}
