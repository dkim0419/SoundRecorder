package com.danielkim.soundrecorder.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.danielkim.soundrecorder.R;
import com.danielkim.soundrecorder.fragments.SettingsFragment;

/**
 * Created by Daniel on 5/22/2017.
 * Updated by Jibesh on 28/04/2019
 */

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setPopupTheme(R.style.ThemeOverlay_AppCompat_Light);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.action_settings);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new SettingsFragment())
                .commit();
    }
}
