package com.assuntadc.soundrecorder.integrationtest;
import android.support.v7.widget.LinearLayoutManager;

import com.danielkim.soundrecorder.adapters.FileViewerAdapter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

//ALL-CALL-SITE CRITERIA ADOPTED

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21)
public class FileViewerAdapterTester {

    //INTEGRATION TEST WITH CUSTOM ALERT DIALOG FOR EXTRACTED TEST
    @Test
    public void onBindViewHolder(){
        FileViewerAdapter fwa = new FileViewerAdapter(RuntimeEnvironment.application,
                new LinearLayoutManager());


    }

}
