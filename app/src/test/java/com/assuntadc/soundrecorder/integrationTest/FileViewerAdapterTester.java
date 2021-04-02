package com.assuntadc.soundrecorder.integrationTest;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.LinearLayoutManager;

import com.danielkim.soundrecorder.CustomAlertDialogForExtractedText;
import com.danielkim.soundrecorder.R;
import com.danielkim.soundrecorder.adapters.FileViewerAdapter;
import com.danielkim.soundrecorder.asynctasks.AsyncronusTranscription;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

import java.io.File;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.robolectric.shadows.ShadowLooper.shadowMainLooper;

@RunWith(RobolectricTestRunner.class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(sdk = 21)
public class FileViewerAdapterTester {
    private Context context;
    private String audioTestFilePath;

    @Before
    public void setUp() {
        context = RuntimeEnvironment.application;
        audioTestFilePath = new File("src/test/java/com/assuntadc/soundrecorder/res/rec_test.wav").getAbsolutePath();
    }

    @Test
    public void testSpeechToTest() throws InterruptedException {
        //Test integration between FileViewerAdapter and DBHelper
        FileViewerAdapter adapter = new FileViewerAdapter(context, new LinearLayoutManager(context));
        assertNotNull("ERROR: DATABASE OBJECT IS NULL", adapter.getDatabase());

        //Test integration between FileViewerAdapter and CustomAlertDialogForExtractedText, AsyncronusRefreshing,
        // AsyncronusTranscription and the IBM Speech-To-Text service
        AsyncTask[] asyncTasks = adapter.performSpeechToTextForTesting(audioTestFilePath);
        AsyncronusTranscription asyncronusTranscription = (AsyncronusTranscription) asyncTasks[1];

        while (!asyncronusTranscription.isTaskEnded()){
            shadowMainLooper().idle();
            Thread.sleep(500);
        }

        CustomAlertDialogForExtractedText customAlertDialogForExtractedText = asyncronusTranscription.getCustomAlertDialogForExtractedText();
        assertNotNull("ERROR: CUSTOMALERTDIALOG OBJECT IS NULL", customAlertDialogForExtractedText);

        String customAlertDialogBody = customAlertDialogForExtractedText.getBodyTextView().getText().toString();
        String errorStringToCompare = context.getResources().getString(R.string.toast_unable_to_extract_text);

        assertNotEquals(errorStringToCompare.toUpperCase(), customAlertDialogBody, errorStringToCompare);

        System.out.println("Testo estratto: " + customAlertDialogBody);
    }
}
