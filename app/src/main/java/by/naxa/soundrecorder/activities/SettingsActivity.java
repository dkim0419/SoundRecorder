package by.naxa.soundrecorder.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import by.naxa.soundrecorder.R;
import by.naxa.soundrecorder.fragments.SettingsFragment;

/**
 * A {@link PreferenceActivity} that presents a set of application settings.
 * On handset devices, settings are presented as a single list.
 * <p>
 * See <a href="https://material.io/design/platform-guidance/android-settings.html">
 * Material Design: Android Settings</a> for design guidelines and the <a
 * href="https://d.android.com/guide/topics/ui/settings">Settings API Guide</a>
 * for more information on developing a Settings UI.
 *
 * Created by Daniel on 5/22/2017.
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        setupActionBar();

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new SettingsFragment())
                .commit();
    }

    /**
     * Set up the {@link android.app.ActionBar}.
     */
    private void setupActionBar() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.action_settings);
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
    }

}
