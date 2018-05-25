package com.teamxod.unilink;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import junit.framework.Test;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import info.hoang8f.android.segmented.SegmentedGroup;


public class My_preference extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_preference);

        // seekbar for the sleep time
        SeekBar sleep_seekBar = (SeekBar)findViewById(R.id.sleep_seekbar);
        sleep_seekBar.setMax(9);
        final TextView sleep_seebar_text = (TextView)findViewById(R.id.sleep_seekbar_text);
        sleep_seebar_text.setText("9PM - 6AM");
        sleep_seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressV;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressV = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(progressV < 4) {
                    int time = progressV + 9;
                    sleep_seebar_text.setText(time + " PM");
                }
                else{
                    int time = progressV - 3;
                    sleep_seebar_text.setText(time + " AM");
                }
            }
        });

        // seekbar for the clean times
        SeekBar clean_seekBar = (SeekBar)findViewById(R.id.clean_seekbar);
        clean_seekBar.setMax(7);
        final TextView clean_seebar_text = (TextView)findViewById(R.id.clean_seekbar_text);
        clean_seebar_text.setText("0 times - 7 times");
        clean_seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressV;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressV = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                clean_seebar_text.setText("             " + progressV + " times");
            }
        });

        // button groups for yes and no
        SegmentedGroup bring = (SegmentedGroup)findViewById(R.id.bring_button);
        bring.setTintColor(Color.parseColor("#cc66ff"));

        SegmentedGroup pet = (SegmentedGroup)findViewById(R.id.pet_button);
        pet.setTintColor(Color.parseColor("#cc66ff"));

        SegmentedGroup cook = (SegmentedGroup)findViewById(R.id.cook_button);
        cook.setTintColor(Color.parseColor("#cc66ff"));

        // hobbies section
        final CheckedTextView surfing = (CheckedTextView)findViewById(R.id.surfing);
        surfing.setCheckMarkDrawable(R.drawable.unchecked);
        final CheckedTextView hiking = (CheckedTextView)findViewById(R.id.hiking);
        hiking.setCheckMarkDrawable(R.drawable.unchecked);
        final CheckedTextView skiing = (CheckedTextView)findViewById(R.id.skiing);
        skiing.setCheckMarkDrawable(R.drawable.unchecked);
        final CheckedTextView gaming = (CheckedTextView)findViewById(R.id.gaming);
        gaming.setCheckMarkDrawable(R.drawable.unchecked);


        surfing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!surfing.isChecked()) {
                    surfing.setChecked(true);
                    surfing.setCheckMarkDrawable(R.drawable.checked);
                }else{
                    surfing.setChecked(false);
                    surfing.setCheckMarkDrawable(R.drawable.unchecked);
                }

            }
        });

        hiking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!hiking.isChecked()){
                    hiking.setCheckMarkDrawable(R.drawable.checked);
                    hiking.setChecked(true);
                }else {
                    hiking.setCheckMarkDrawable(R.drawable.unchecked);
                    hiking.setChecked(false);
                }
            }
        });

        skiing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!skiing.isChecked()){
                    skiing.setChecked(true);
                    skiing.setCheckMarkDrawable(R.drawable.checked);
                }else {
                    skiing.setCheckMarkDrawable(R.drawable.unchecked);
                    skiing.setChecked(false);
                }
            }
        });

        gaming.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!gaming.isChecked()){
                    gaming.setChecked(true);
                    gaming.setCheckMarkDrawable(R.drawable.checked);
                }else {
                    gaming.setCheckMarkDrawable(R.drawable.unchecked);
                    gaming.setChecked(false);
                }
            }
        });

        // spinner for the language spinner
        Spinner languageSpinner = (Spinner)findViewById(R.id.languege_spinner);
        ArrayAdapter<CharSequence> languageAdaptor = ArrayAdapter.createFromResource(this,R.array.language_array, android.R.layout.simple_spinner_item);
        languageAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(languageAdaptor);


    }
}
