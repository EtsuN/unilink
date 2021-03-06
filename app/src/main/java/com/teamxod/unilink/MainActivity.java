package com.teamxod.unilink;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teamxod.unilink.chat.ChatFragment;
import com.teamxod.unilink.house.HousingFragment;
import com.teamxod.unilink.roommate.RoommateFragment;
import com.teamxod.unilink.user.AuthenticationActivity;
import com.teamxod.unilink.user.InitiateProfile;
import com.teamxod.unilink.user.MeFragment;

import java.lang.reflect.Field;


public class MainActivity extends AppCompatActivity {

    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        database.child("Visible").keepSynced(true);
        database.child("Preference").keepSynced(true);

        //check the need for initiate user profile
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference mUserReference = mDatabase.child("Users");
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth == null || mAuth.getCurrentUser() == null) {
            Intent auth = new Intent(MainActivity.this, AuthenticationActivity.class);
            startActivity(auth);
            finish();
        }
        uid = mAuth.getCurrentUser().getUid();
        mUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                // already set profile
                if (!snapshot.hasChild(uid)) {
                    Intent InitiateProfileIntent = new Intent(MainActivity.this, InitiateProfile.class);
                    startActivity(InitiateProfileIntent);
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;
                switch (item.getItemId()) {
                    case R.id.navigation_housing:
                        fragment = new HousingFragment();
                        break;
                    case R.id.navigation_roommate:
                        fragment = new RoommateFragment();
                        break;
                    case R.id.navigation_chat:
                        fragment = new ChatFragment();
//                        Intent InitiateProfileIntent = new Intent(MainActivity.this, RealtimeDbChatActivity.class);
//                        startActivity(InitiateProfileIntent);
//                        finish();
                        break;
                    case R.id.navigation_my:
                        fragment = new MeFragment();
                        break;
                }
                return loadFragment(fragment);
            }
        });

        loadFragment(new HousingFragment());
        BottomNavigationViewHelper.disableShiftMode(navigation);
    }


    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

}

//helper to disable shift mode of the navbar
class BottomNavigationViewHelper {
    @SuppressLint("RestrictedApi")
    public static void disableShiftMode(BottomNavigationView view) {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) view.getChildAt(0);
        try {
            Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
            shiftingMode.setAccessible(true);
            shiftingMode.setBoolean(menuView, false);
            shiftingMode.setAccessible(false);
            for (int i = 0; i < menuView.getChildCount(); i++) {
                BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
                item.setShiftingMode(false);
                // set once again checked value, so view will be updated
                item.setChecked(item.getItemData().isChecked());
            }
        } catch (NoSuchFieldException e) {
            Log.e("BNVHelper", "Unable to get shift mode field", e);
        } catch (IllegalAccessException e) {
            Log.e("BNVHelper", "Unable to change value of shift mode", e);
        }
    }
}