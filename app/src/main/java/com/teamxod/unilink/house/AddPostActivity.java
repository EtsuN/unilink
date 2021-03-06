package com.teamxod.unilink.house;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.teamxod.unilink.R;
import com.teamxod.unilink.roommate.Room;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.UUID;

/* TODO @ Etsu:
    First, try to read through the whole file. I did lots of comments. Just make sure u know what i did.
    Then:
        (1) use the values got from the UI fields (store in Java fields) to create a house obj and push to firebase.
            All the values from XML are passed into java fields(If i didn't make mistake), except the date. (search "INFO FOR DATE").
            Problems: inconsistence between the values we got from xml and the fields in House class. (talk to neal see if we can change it).
        (2) Grid of image, then pass the images to House obj.
        (3) Upload imgs from local.
        (4) U can do a check to see if edit text all have input (not necessary i can do this)
        (5) Do what else we may need.
*/
public class AddPostActivity extends AppCompatActivity implements IPickResult, DatePickerDialog.OnDateSetListener {


    // Database field
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;
    private DatabaseReference post;
    // Java fields (Used to create House obj and push to firebase)
    // TODO @ etsu  (bs. we use start date + lease length.  no end date)
    private String _posterId;
    private String _postId;
    private String _houseType;
    private String _bedroom_number;
    private String _bathroom_number;
    private String _title;
    private String _location;
    private String _description;
    private String _startDate;
    private String _leaseLength;
    private String _tv;
    private String _ac;
    private String _bus;
    private String _parking;
    private String _videoGame;

    //  ------------- UI fields ---------------- //
    private String _gym;
    private String _laundry;
    private String _pet;
    private GridLayout photoGrid;
    private ArrayList<LinearLayout> photoBoxList;
    private PickImageDialog dialog;
    private ArrayList<Uri> pictureList;
    private LinearLayout roomContainer;
    private ArrayList<LinearLayout> roomBoxList;
    private ArrayList<Room> roomList;
    // edit text
    private EditText title;
    private EditText street;
    private EditText city;
    private EditText start_date;
    private EditText description;

    // Facilities
    private CheckedTextView ac;
    private CheckedTextView allow_pet;
    private CheckedTextView parking;
    private CheckedTextView tv;
    private CheckedTextView video_game;
    private CheckedTextView gym;
    private CheckedTextView laundry;
    private CheckedTextView bus;

    // for validation
    private TextWatcher tw;
    private boolean filledIn;


    // --------------- UI fields above --------------- //

    // helper method for ChekedTextView in XML
    private static void setCheckedTextView(final CheckedTextView ctv) {

        ctv.setCheckMarkDrawable(R.drawable.unchecked);
        ctv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!ctv.isChecked()) {
                    ctv.setChecked(true);
                    ctv.setCheckMarkDrawable(R.drawable.checked);
                } else {
                    ctv.setChecked(false);
                    ctv.setCheckMarkDrawable(R.drawable.unchecked);
                }

            }
        });
    }

    // Get content of check text view
    private static String isChecked(final CheckedTextView ctv) {
        if (ctv.isChecked())
            return "1";
        else
            return "0";
    }

    // On create
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);


        // variables
        filledIn = false;
        _houseType = "";
        _bedroom_number = "";
        _bathroom_number = "";
        _leaseLength = "";

        // Data base part
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        // get UI in gridlayout
        ImageView addpic = findViewById(R.id.addpic_btn);
        photoGrid = findViewById(R.id.gridlayout);
        photoBoxList = new ArrayList<>();
        pictureList = new ArrayList<>();
        addpic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // pickImage
                dialog = PickImageDialog.build(new PickSetup()).show(AddPostActivity.this);
                hideKeyBoard(v);
            }
        });

        // get UI in gridlayout
        Button addroom = findViewById(R.id.addroom_btn);
        roomContainer = findViewById(R.id.linearlayout_room);
        roomBoxList = new ArrayList<>();
        roomList = new ArrayList<>();
        addroom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // pickImage
                addRoom();
                hideKeyBoard(v);
            }
        });

        title = findViewById(R.id.title);
        street = findViewById(R.id.street);
        city = findViewById(R.id.city);
        RadioButton apartment = findViewById(R.id.apartment);
        RadioButton house = findViewById(R.id.house);
        RadioButton town_house = findViewById(R.id.town_house);
        RadioButton bed_zero = findViewById(R.id.bed_zero);
        RadioButton bed_one = findViewById(R.id.bed_one);
        RadioButton bed_two = findViewById(R.id.bed_two);
        RadioButton bed_three = findViewById(R.id.bed_three);
        RadioButton bed_four_plus = findViewById(R.id.bed_four_plus);
        RadioButton bath_zero = findViewById(R.id.bath_zero);
        RadioButton bath_one = findViewById(R.id.bath_one);
        RadioButton bath_two = findViewById(R.id.bath_two);
        RadioButton bath_three = findViewById(R.id.bath_three);
        RadioButton bath_four_plus = findViewById(R.id.bath_four_plus);
        RadioButton annual = findViewById(R.id.annual);
        RadioButton quarterly = findViewById(R.id.quarterly);
        RadioButton short_term = findViewById(R.id.short_term);
        start_date = findViewById(R.id.start_date);
        ac = findViewById(R.id.ac);
        allow_pet = findViewById(R.id.allow_pet);
        parking = findViewById(R.id.parking);
        tv = findViewById(R.id.tv);
        video_game = findViewById(R.id.video_game);
        gym = findViewById(R.id.gym);
        laundry = findViewById(R.id.laundry);
        bus = findViewById(R.id.bus);
        description = findViewById(R.id.description);

        Button submit = findViewById(R.id.submit);

        // for validation
        tw = new MyTextWatcher();
        title.addTextChangedListener(tw);
        street.addTextChangedListener(tw);
        city.addTextChangedListener(tw);
        description.addTextChangedListener(tw);

        // set check text view, using helper method
        setCheckedTextView(ac);
        setCheckedTextView(allow_pet);
        setCheckedTextView(parking);
        setCheckedTextView(tv);
        setCheckedTextView(video_game);
        setCheckedTextView(gym);
        setCheckedTextView(laundry);
        setCheckedTextView(bus);

        //  -------------- set listeners ---(pass values in xml to java)---------- //
        // date picker
        start_date.setInputType(InputType.TYPE_NULL);
        start_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment datePicker = new DatePickerFragment();
                datePicker.show(getSupportFragmentManager(), "date");
                hideKeyBoard(v);
            }
        });
        // House type
        apartment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _houseType = "Apartment";
                hideKeyBoard(v);
            }
        });
        town_house.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _houseType = "Town House";hideKeyBoard(v);
            }
        });
        house.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _houseType = "House";
                hideKeyBoard(v);
            }
        });

        // Bedroom number
        bed_zero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _bedroom_number = "0";
                hideKeyBoard(v);
            }
        });
        bed_one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _bedroom_number = "1";
                hideKeyBoard(v);
            }
        });
        bed_two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _bedroom_number = "2";
                hideKeyBoard(v);
            }
        });
        bed_three.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _bedroom_number = "3";
                hideKeyBoard(v);
            }
        });
        bed_four_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _bedroom_number = "4+";
                hideKeyBoard(v);
            }
        });

        // Bathroom number
        bath_zero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _bathroom_number = "0";
                hideKeyBoard(v);
            }
        });
        bath_one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _bathroom_number = "1";
                hideKeyBoard(v);
            }
        });
        bath_two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _bathroom_number = "2";
                hideKeyBoard(v);
            }
        });
        bath_three.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _bathroom_number = "3";
                hideKeyBoard(v);
            }
        });
        bath_four_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _bathroom_number = "4+";
                hideKeyBoard(v);
            }
        });

        // Lease length
        annual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _leaseLength = "Annually";
                hideKeyBoard(v);
            }
        });
        quarterly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _leaseLength = "Quarterly";
                hideKeyBoard(v);
            }
        });
        short_term.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _leaseLength = "Monthly";
                hideKeyBoard(v);
            }
        });

        // SUBMIT button !!!
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // validation
                if (validation())
                    return;

                _posterId = mAuth.getCurrentUser().getUid();
                // TODO use Java fields values to create house obj

                // get edit text content
                _title = title.getText().toString();

                for (int i = 0; i < roomList.size(); i++)
                    roomList.get(i).setPrice(Integer.parseInt(((EditText) roomBoxList.get(i).findViewById(R.id.price)).getText().toString()));
                _location = street.getText().toString() + ", " + city.getText().toString();
                _startDate = start_date.getText().toString();
                _description = description.getText().toString();

                // Facilities check box
                _ac = isChecked(ac);
                _pet = isChecked(allow_pet);
                _parking = isChecked(parking);
                _tv = isChecked(tv);
                _videoGame = isChecked(video_game);
                _gym = isChecked(gym);
                _laundry = isChecked(laundry);
                _bus = isChecked(bus);

                uploadToFirebase(pictureList);
                finish();
            }
        });

        ImageView backBtn = findViewById(R.id.back_button);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }// on create

    // helper for date
    private void setDate(final Calendar calendar) {
        final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
        start_date.setText(dateFormat.format(calendar.getTime()));
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar cal = new GregorianCalendar(year, month, dayOfMonth);
        setDate(cal);
    }

    // helper validation
    private boolean validation() {
        if (!filledIn || start_date.getText().toString().length() == 0) {
            String INVALID_FORM_DUE_TO_FILLING = "Please fill in all the information!";
            Toast.makeText(AddPostActivity.this, INVALID_FORM_DUE_TO_FILLING, Toast.LENGTH_LONG).show();
            return true;
        }
        if (roomBoxList.isEmpty() || photoBoxList.isEmpty()) {
            String INVALID_FORM_DUE_TO_ADDING = "Please add at least one picture and one room information!";
            Toast.makeText(AddPostActivity.this, INVALID_FORM_DUE_TO_ADDING, Toast.LENGTH_LONG).show();
            return true;
        }
        String INVALID_FORM_DUE_TO_SELECTING = "Please select all the choices!";
        if (_houseType.length() == 0 || _bedroom_number.length() == 0
                || _bathroom_number.length() == 0 || _leaseLength.length() == 0) {
            Toast.makeText(AddPostActivity.this, INVALID_FORM_DUE_TO_SELECTING, Toast.LENGTH_LONG).show();
            return true;
        }
        for (Room r : roomList) {
            if (r.getRoomType().length() == 0) {
                Toast.makeText(AddPostActivity.this, INVALID_FORM_DUE_TO_SELECTING, Toast.LENGTH_LONG).show();
                return true;
            }
        }
        return false;
    }

    // add room
    private void addRoom() {
        final LinearLayout roomBox = (LinearLayout) View.inflate(this,
                R.layout.add_room, null);
        final Room room = new Room("", 0);
        roomBox.findViewById(R.id.deleteroom_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyBoard(v);
                int index = roomBoxList.indexOf(roomBox);
                roomBoxList.remove(index);
                roomList.remove(index);
                roomContainer.removeView(roomBox);
            }
        });

        // Room type
        roomBox.findViewById(R.id.master_bedroom).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: change variable
                room.setRoomType("Master Bedroom");
                hideKeyBoard(v);
            }
        });
        roomBox.findViewById(R.id.living_room).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                room.setRoomType("Living Room");
                hideKeyBoard(v);
            }
        });
        roomBox.findViewById(R.id.loft).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyBoard(v);
                room.setRoomType("Loft / Den");
            }
        });

        ((EditText) roomBox.findViewById(R.id.price)).addTextChangedListener(tw);
        roomBoxList.add(roomBox);
        roomList.add(room);
        roomContainer.addView(roomBox);
    }

    //PickImage Plug-in
    //choose picture from camera or gallery
    @Override
    public void onPickResult(PickResult r) {
        if (r.getError() == null) {
            // create photo box
            final LinearLayout photoBox = (LinearLayout) View.inflate(this,
                    R.layout.add_photo, null);
            photoBox.findViewById(R.id.deletepic_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideKeyBoard(v);
                    int index = photoBoxList.indexOf(photoBox);
                    photoBoxList.remove(index);
                    pictureList.remove(index);
                    photoGrid.removeView(photoBox);
                }
            });
            photoBoxList.add(photoBox);

            //Mandatory to refresh image from Uri.
            // upload to firebase storage
            Uri picture = r.getUri();
            Glide.with(AddPostActivity.this)
                    .load(picture)
                    .apply(RequestOptions.centerCropTransform())
                    .into((ImageView) photoBoxList.get(photoBoxList.size() - 1).findViewById(R.id.pic));

            // add photo box to UI
            photoGrid.addView(photoBox);
            pictureList.add(picture);

        } else {
            Toast.makeText(this, r.getError().getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // TODO create house obj here
    private void writeNewPost(House house) {
        post.setValue(house);
    }

    private void uploadToFirebase(ArrayList<Uri> uriList) {
        post = mDatabase.child("House_post").push();
        _postId = post.getKey();
        writeNewPost(new House(_posterId, _houseType, _title, _location,
                _description, _startDate, _leaseLength,
                new ArrayList<String>(0), roomList, _tv, _ac, _bus,
                _parking, _videoGame, _gym, _laundry, _pet, _bedroom_number, _bathroom_number));

        String uid = mAuth.getCurrentUser().getUid();
        final DatabaseReference myPostReference = mDatabase.child("Users").child(uid).child("my_house_posts");

        myPostReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> postList = new ArrayList<>();
                for (DataSnapshot postID : dataSnapshot.getChildren()) {
                    postList.add(postID.getValue(String.class));
                }
                postList.add(_postId);
                myPostReference.setValue(postList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

        final StorageReference baseref = mStorageRef.child("House_Images").child(post.getKey());
        StorageReference image_ref;
        int i = 0;
        for (Uri uri : uriList) {
            final int finalI = i;
            i++;
            image_ref = baseref.child(UUID.randomUUID().toString());
            final StorageReference finalImage_ref = image_ref;
            image_ref.putFile(uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get a URL to the uploaded content
                            finalImage_ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    post.child("pictures").child(String.valueOf(finalI)).setValue(uri.toString());
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
        }
    }

    public static class DatePickerFragment extends DialogFragment {
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(),
                    (DatePickerDialog.OnDateSetListener) getActivity(), year, month, day);
        }

    }

    // hide kb
    private void hideKeyBoard(View view) {
        View vv = findViewById(android.R.id.content);
        if (vv != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    class MyTextWatcher implements TextWatcher {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (title.getText().toString().length() > 0 &&
                    street.getText().toString().length() > 0 &&
                    city.getText().toString().length() > 0 &&
                    description.getText().toString().length() > 0) {
                for (LinearLayout layout : roomBoxList)
                    if (((EditText) layout.findViewById(R.id.price)).getText().toString().length() <= 0) {
                        filledIn = false;
                        return;
                    }
                filledIn = true;
            } else {
                filledIn = false;
            }
        }
    }
}
