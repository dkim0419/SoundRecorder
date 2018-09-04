package by.naxa.soundrecorder.fragments;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import androidx.annotation.Nullable;

import by.naxa.soundrecorder.BuildConfig;
import by.naxa.soundrecorder.R;
import by.naxa.soundrecorder.activities.SettingsActivity;
import by.naxa.soundrecorder.util.MySharedPreferences;

/**
 * This fragment shows general preferences.
 * Created by Daniel on 5/22/2017.
 */
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        final CheckBoxPreference highQualityPref = (CheckBoxPreference) findPreference(
                getResources().getString(R.string.pref_high_quality_key));
        highQualityPref.setChecked(MySharedPreferences.getPrefHighQuality(getActivity()));
        highQualityPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                MySharedPreferences.setPrefHighQuality(getActivity(), (boolean) newValue);
                return true;
            }
        });

        final Preference aboutPref = findPreference("pref_about");
        aboutPref.setSummary(getString(R.string.pref_about_desc, BuildConfig.VERSION_NAME));

        final Preference licensesPref = findPreference("pref_licenses");
        licensesPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new LicensesFragment().show(
                        ((SettingsActivity) getActivity()).getSupportFragmentManager()
                                .beginTransaction(), "dialog_licenses");
                return true;
            }
        });
    }
}
