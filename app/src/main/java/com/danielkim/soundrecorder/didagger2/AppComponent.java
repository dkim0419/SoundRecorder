/*
 * Year: 2017. This class was added by iClaude.
 */

package com.danielkim.soundrecorder.didagger2;

import com.danielkim.soundrecorder.RecordingService;
import com.danielkim.soundrecorder.ScheduledRecordingService;
import com.danielkim.soundrecorder.activities.AddScheduledRecordingActivity;
import com.danielkim.soundrecorder.adapters.FileViewerAdapter;
import com.danielkim.soundrecorder.fragments.ScheduledRecordingsFragment;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Dagger @Component class.
 */
@Component(modules = {AppModule.class, DBHelperModule.class})
@Singleton
public interface AppComponent {
    void inject(AddScheduledRecordingActivity addScheduledRecordingActivity);

    void inject(FileViewerAdapter fileViewerAdapter);

    void inject(RecordingService recordingService);

    void inject(ScheduledRecordingService scheduledRecordingService);

    void inject(ScheduledRecordingsFragment scheduledRecordingsFragment);

}
