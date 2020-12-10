package com.assuntadc.soundrecorder;

import android.content.Intent;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ServiceController;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21)
public class RecordingServiceTest {
    private static int startId = 0;
    private ServiceController<LocalRecordingService> serviceController;

    @Before
    public void setUp() {
        serviceController = Robolectric.buildService(LocalRecordingService.class);
    }

    @AfterClass
    public static void tearDown() {

    }

    private void goStateS0(){
        serviceController.create();
    }

    private void goStateS1(Intent intent){
        startId++;
        serviceController.withIntent(intent).startCommand(0, startId);
    }

    private void goStateS2(){
        serviceController.destroy();
    }

    private void goStateS3(Intent intent) {
        intent.putExtra("inPause", true);

        startId++;
        serviceController.withIntent(intent).startCommand(0, startId);
    }

    private void goStateS4(Intent intent) {
        intent.putExtra("inPause", false);

        startId++;
        serviceController.withIntent(intent).startCommand(0, startId);
    }

    @Test
    public void firstSequence() {
        //Test sequence S0 - S1 - S2

        Intent intent = new Intent(RuntimeEnvironment.application, LocalRecordingService.class);

        long start = System.currentTimeMillis();
        long end = 0;

        goStateS0();

        goStateS1(intent);

        do {
            //recording
            end = System.currentTimeMillis();
        }while (end <= start + 6*1000);


        goStateS2();

    }

    @Test
    public void SecondSequence() {
        // S0 - S1 - S3 - S2
        Intent intent = new Intent(RuntimeEnvironment.application, LocalRecordingService.class);

        long start = System.currentTimeMillis();
        long end = 0;

        goStateS0();
        goStateS1(intent);

        do {
            //recording
            end = System.currentTimeMillis();
        }while (end <= start + 6*1000);

        goStateS3(intent);

        start = System.currentTimeMillis();
        do {
            //pause
            end = System.currentTimeMillis();
        }while (end <= start + 6*1000);

        goStateS2();
    }

    @Test
    public void ThirdSequence() {
        // S0 - S1 - S3 - S4 - S2
        Intent intent = new Intent(RuntimeEnvironment.application, LocalRecordingService.class);

        long start = System.currentTimeMillis();
        long end = 0;

        goStateS0();
        goStateS1(intent);

        do {
            //recording
            end = System.currentTimeMillis();
        }while (end <= start + 6*1000);

        goStateS3(intent);

        start = System.currentTimeMillis();
        do {
            //pause
            end = System.currentTimeMillis();
        }while (end <= start + 6*1000);

        goStateS4(intent);

        start = System.currentTimeMillis();
        do {
            //pause
            end = System.currentTimeMillis();
        }while (end <= start + 6*1000);

        goStateS2();
    }

    @Test
    public void FourthSequence() {
        // S0 - S1 - S3 - S4 - S3 - S2
        Intent intent = new Intent(RuntimeEnvironment.application, LocalRecordingService.class);

        long start = System.currentTimeMillis();
        long end = 0;

        goStateS0();
        goStateS1(intent);

        do {
            //recording
            end = System.currentTimeMillis();
        }while (end <= start + 6*1000);

        goStateS3(intent);

        start = System.currentTimeMillis();
        do {
            //pause
            end = System.currentTimeMillis();
        }while (end <= start + 6*1000);

        goStateS4(intent);

        start = System.currentTimeMillis();
        do {
            //pause
            end = System.currentTimeMillis();
        }while (end <= start + 6*1000);

        goStateS3(intent);

        start = System.currentTimeMillis();
        do {
            //pause
            end = System.currentTimeMillis();
        }while (end <= start + 6*1000);

        goStateS2();
    }
}
