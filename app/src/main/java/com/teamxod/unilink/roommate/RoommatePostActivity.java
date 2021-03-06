package com.teamxod.unilink.roommate;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.progresviews.ProgressWheel;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teamxod.unilink.R;
import com.teamxod.unilink.chat.RealtimeDbChatActivity;
import com.teamxod.unilink.user.Preference;
import com.teamxod.unilink.user.Profile;
import com.teamxod.unilink.user.User;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class RoommatePostActivity extends AppCompatActivity {

    private final static int MARGIN = 15;
    private String userUID;
    private String myUID;
    private User user;
    private DatabaseReference userReference;
    private DatabaseReference preferenceReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roommate_post);

        Intent intent = getIntent();

        if (intent.hasExtra("uid")) {
            userUID = intent.getExtras().getString("uid");
        } else {
            Toast.makeText(this, "User does not exist", Toast.LENGTH_LONG).show();
            finish();
        }

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        userReference = database.child("Users").child(userUID);
        preferenceReference = database.child("Preference");
        myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();


        ImageView back_btn = findViewById(R.id.back_button);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button contact_btn = findViewById(R.id.roommate_contact);
        contact_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chatIntent = new Intent(RoommatePostActivity.this, RealtimeDbChatActivity.class);
                chatIntent.putExtra("user_id", userUID);
                chatIntent.putExtra("user_name", user.getName());
                startActivity(chatIntent);
            }
        });


        loadData();

    }

    private void loadData() {
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                updateUI();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

        preferenceReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Preference myPreference = dataSnapshot.child(myUID).getValue(Preference.class);
                Preference userPreference = dataSnapshot.child(userUID).getValue(Preference.class);
                ArrayList<String> tags;
                double score;
                if (myPreference != null && userPreference != null) {
                    Recommendation recommendation = new Recommendation(myPreference, userPreference);
                    score = recommendation.getScore();
                    tags = recommendation.getTagList();
                } else {
                    tags = new ArrayList<>();
                    score = 0;
                }
                setScoreAndTags(score, tags);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }

    private void setScoreAndTags(double score, ArrayList<String> tags) {

        TextView ScoreTextView = findViewById(R.id.roommate_score);

        ProgressWheel pw = findViewById(R.id.roommate_progress);

        pw.setPercentage((int) (score * 36));

        TagViewGroup tagGroup = findViewById(R.id.tag);

        DecimalFormat df = new DecimalFormat("0.00");
        String temp = df.format(score);

        if (score != 0)
            ScoreTextView.setText(temp);
        else
            ScoreTextView.setText("?");

        ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = MARGIN;
        lp.rightMargin = MARGIN;
        lp.topMargin = MARGIN;
        lp.bottomMargin = MARGIN;
        for (int i = 0; i < tags.size(); i++) {
            TextView tagView = new TextView(this);
            tagView.setText(tags.get(i));
            tagView.setTextAppearance(R.style.tag_text);

            tagView.setBackgroundResource(R.drawable.roommate_tag_layout);
            tagGroup.addView(tagView, lp);
        }
    }

    private void updateUI() {
        TextView title = findViewById(R.id.roommate_title);
        TextView name = findViewById(R.id.roommate_name);
        TextView gender = findViewById(R.id.roommate_gender);
        TextView schoolYear = findViewById(R.id.school_year);
        TextView graduateYear = findViewById(R.id.graduate_year);
        TextView description = findViewById(R.id.roommate_description);
        ImageView picture = findViewById(R.id.poster_picture);
        TextView bottomName = findViewById(R.id.roommate_bottom_name);
        TextView bottomYear = findViewById(R.id.roommate_bottom_year);

        title.setText(user.getName());
        name.setText(user.getName());
        bottomName.setText(user.getName());
        gender.setText(user.getGender());
        int enterYear = Integer.parseInt(user.getYearGraduate()) - 4;
        int currentYear = Calendar.getInstance().get(Calendar.YEAR) - enterYear;
        String temp;
        if (currentYear > 5)
            temp = "Alumni";
        else if (currentYear <= 0)
            temp = "Incoming Student";
        else
            temp = "Graduating " + user.getYearGraduate();
        schoolYear.setText(temp);
        bottomYear.setText(temp);
        //temp = "Graduating " + user.getYearGraduate();
        //graduateYear.setText(temp);
        description.setText(user.getDescription());
        Glide.with(this)
                .load(user.getPicture())
                .apply(RequestOptions.circleCropTransform())
                .into(picture);

        // click avatar to show profile
        picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RoommatePostActivity.this, Profile.class);
                i.putExtra("uid", userUID);
                startActivity(i);
            }
        });
    }
}
