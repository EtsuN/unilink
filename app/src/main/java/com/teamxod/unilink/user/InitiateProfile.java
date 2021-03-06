package com.teamxod.unilink.user;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.teamxod.unilink.MainActivity;
import com.teamxod.unilink.R;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

import java.util.ArrayList;

public class InitiateProfile extends AppCompatActivity implements IPickResult {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private final Uri MALE_PROFILE_PIC = Uri.parse("https://firebasestorage.googleapis.com/v0/b/fir-project-7cabd.appspot.com/o/male.png?alt=media&token=02a80321-a6ae-4194-af4d-bd658de9348f");
    private final Uri FEMALE_PROFILE_PIC = Uri.parse("https://firebasestorage.googleapis.com/v0/b/fir-project-7cabd.appspot.com/o/female.png?alt=media&token=69a0c9c9-eda5-481d-9043-b718d899121b");
    private final String NAME_INVALID = "Please enter a valid name!";
    private final int PICK_IMAGE_REQUEST = 71;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;
    private String uid;
    private String name;
    private Uri picture;
    private String gender;
    private String yearGraduate;
    private String description;
    private ImageView mProfilePic;
    private EditText mEditName;
    private Spinner mGenderSpinner;
    private Spinner mYearSpinner;
    private EditText mDescription;
    private PickImageDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initiate_profile);

        mProfilePic = findViewById(R.id.profile_pic);
        mEditName = findViewById(R.id.edit_name);
        TextView mEmail = findViewById(R.id.email);
        mGenderSpinner = findViewById(R.id.gender_spinner);
        mYearSpinner = findViewById(R.id.year_spinner);
        mDescription = findViewById(R.id.description);
        Button mSave = findViewById(R.id.save);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        if (mAuth != null && mAuth.getCurrentUser() != null) {
            uid = mAuth.getCurrentUser().getUid();

            // using google login
            if (getLoginmethod()) {
                mEditName.setText(mAuth.getCurrentUser().getDisplayName());
                Uri mPhoto = mAuth.getCurrentUser().getPhotoUrl();
                if (mPhoto != null) {
                    Glide.with(this)
                            .load(mPhoto)
                            .apply(RequestOptions.circleCropTransform())
                            .into(mProfilePic);
                    picture = mPhoto;
                }
            }
            mEmail.setText(mAuth.getCurrentUser().getEmail());
        }
        //choose picture from camera or gallery
        mProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = PickImageDialog.build(new PickSetup()).show(InitiateProfile.this);
                hideKeyBoard(v);

            }
        });


        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyBoard(v);
                name = mEditName.getText().toString();
                if (name.equals("")) {
                    Toast.makeText(InitiateProfile.this, NAME_INVALID, Toast.LENGTH_SHORT).show();
                    return;
                }
                gender = mGenderSpinner.getSelectedItem().toString();
                if (picture == null) {
                    if (gender.equals("Female")) {
                        picture = FEMALE_PROFILE_PIC;
                    } else {
                        picture = MALE_PROFILE_PIC;
                    }
                }
                yearGraduate = mYearSpinner.getSelectedItem().toString();
                description = mDescription.getText().toString();
                writeNewUser(name, picture.toString(), gender, yearGraduate, description);
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .setPhotoUri(picture)
                        .build();
                mAuth.getCurrentUser().updateProfile(profileUpdates);
                uploadToFirebase(picture);
                Intent mainIntent = new Intent(InitiateProfile.this, MainActivity.class);
                startActivity(mainIntent);
                finish();
            }
        });

        // hide kb for gender spinner
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                hideKeyBoard(view);
                String selectedItem = parent.getItemAtPosition(position).toString();
                if (selectedItem.equals("Add new category")) {
                    // do your stuff
                }
            } // to close the onItemSelected

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        mYearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                hideKeyBoard(view);
                String selectedItem = parent.getItemAtPosition(position).toString();
                if (selectedItem.equals("Add new category")) {
                    // do your stuff
                }
            } // to close the onItemSelected

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


    }

    //PickImage Plug-in
    //choose picture from camera or gallery
    @Override
    public void onPickResult(PickResult r) {
        if (r.getError() == null) {

            //Mandatory to refresh image from Uri.
            // upload to firebase storage

            picture = r.getUri();
            Glide.with(InitiateProfile.this)
                    .load(picture)
                    .apply(RequestOptions.circleCropTransform())
                    .into(mProfilePic);
        } else {
            Toast.makeText(this, r.getError().getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    private void writeNewUser(String name, String picture, String gender, String yearGraduate,
                              String description) {
        User user = new User(name, picture, gender, yearGraduate, description, new ArrayList<User>(), new ArrayList<String>(0), new ArrayList<String>(0));

        mDatabase.child("Users").child(uid).setValue(user);
    }


    private boolean getLoginmethod() {
        if (mAuth != null && mAuth.getCurrentUser() != null) {
            for (UserInfo user : mAuth.getCurrentUser().getProviderData()) {
                Log.d("TAG", user.getProviderId());
                if (user.getProviderId().equals("google.com")) { // google login
                    return true;
                }
            }
            return false;
        } else {
            return true; // not login, no profile photo
        }
    }

    // hide keyboard
    // This method is used to hide the keyboard
    // If want to use, simply copy the whole method to your file
    // Then call hideKeyBoard(v) in the onclick method
    private void hideKeyBoard(View view) {
        View vv = findViewById(android.R.id.content);
        if (vv != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void uploadToFirebase(Uri uri) {
        final StorageReference profile_images = mStorageRef.child("Profile_Images").child(mAuth.getCurrentUser().getUid());

        try {


            profile_images.putFile(uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get a URL to the uploaded content
                            profile_images.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    picture = uri;
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle any errors
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            // ...
                        }
                    });
        } catch (Exception e) {
            Log.d("InitiateProfile", e.getMessage());
        }

    }

}
