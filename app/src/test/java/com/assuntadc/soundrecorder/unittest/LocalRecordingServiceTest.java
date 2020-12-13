package com.assuntadc.soundrecorder.unittest;

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

import static org.junit.Assert.assertTrue;


@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21)
public class LocalRecordingServiceTest {
    private static int startId = 0;
    private ServiceController<com.danielkim.soundrecorder.mockclasses.LocalRecordingService> serviceController;
    private com.danielkim.soundrecorder.mockclasses.LocalRecordingService localRecordingService;

    @Before
    public void setUp() {
        serviceController = Robolectric.buildService(com.danielkim.soundrecorder.mockclasses.LocalRecordingService.class);
    }

    @AfterClass
    public static void tearDown() {

    }

    private void goStateS0(){
        localRecordingService = serviceController.create().get();
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
        Intent intent = new Intent(RuntimeEnvironment.application, com.danielkim.soundrecorder.mockclasses.LocalRecordingService.class);

        long start = System.currentTimeMillis();
        long end = 0;

        double expectedRecordingDurationInSeconds = Math.random()*10;
        while (expectedRecordingDurationInSeconds < 0.5) expectedRecordingDurationInSeconds = Math.random()*10;
        expectedRecordingDurationInSeconds = Math.round(expectedRecordingDurationInSeconds*10.0)/10.0;

        goStateS0();
        assertTrue("ILLEGAL STATE INVARIANTS FOR S0", localRecordingService.isStateS0());

        goStateS1(intent);
        assertTrue("ILLEGAL STATE INVARIANTS FOR S1", localRecordingService.isStateS1());

        do {
            //recording
            end = System.currentTimeMillis();
        }while (end <= start + expectedRecordingDurationInSeconds*1000);

        goStateS2();
        assertTrue("ILLEGAL STATE INVARIANTS FOR S2", localRecordingService.isStateS2());

        double actualRecordingDurationInSeconds = Math.round(localRecordingService.getRecordingDuration()/100.0)/10.0;

        assertTrue("THE DURATION OF THE EXPECTED AND THE ACTUAL RECORDING TIME ARE DIFFERENT", expectedRecordingDurationInSeconds == actualRecordingDurationInSeconds);

        System.out.println("RECORDING DURATION RESULTS");
        System.out.println("Expected Duration: " + expectedRecordingDurationInSeconds);
        System.out.println("Actual Duration: " + actualRecordingDurationInSeconds);
        System.out.println("OUTCOME: TEST SUCCESSFUL\n");
    }

    @Test
    public void SecondSequence() {
        // S0 - S1 - S3 - S2
        Intent intent = new Intent(RuntimeEnvironment.application, com.danielkim.soundrecorder.mockclasses.LocalRecordingService.class);

        long start = System.currentTimeMillis();
        long end = 0;

        double expectedRecordingDurationInSeconds = Math.random()*10;
        while (expectedRecordingDurationInSeconds < 0.5) expectedRecordingDurationInSeconds = Math.random()*10;
        expectedRecordingDurationInSeconds = Math.round(expectedRecordingDurationInSeconds*10.0)/10.0;

        double expectedTotalBreakTimeInSeconds = Math.random()*10;
        while (expectedTotalBreakTimeInSeconds < 0.5) expectedTotalBreakTimeInSeconds = Math.random()*10;
        expectedTotalBreakTimeInSeconds = Math.round(expectedTotalBreakTimeInSeconds*10.0)/10.0;

        goStateS0();
        assertTrue("ILLEGAL STATE INVARIANTS FOR S0", localRecordingService.isStateS0());

        goStateS1(intent);
        assertTrue("ILLEGAL STATE INVARIANTS FOR S1", localRecordingService.isStateS1());

        do {
            //recording
            end = System.currentTimeMillis();
        }while (end <= start + expectedRecordingDurationInSeconds*1000);

        goStateS3(intent);
        assertTrue("ILLEGAL STATE INVARIANTS FOR S3", localRecordingService.isStateS3());

        start = System.currentTimeMillis();
        do {
            //pause
            end = System.currentTimeMillis();
        }while (end <= start + expectedTotalBreakTimeInSeconds*1000);

        goStateS2();
        assertTrue("ILLEGAL STATE INVARIANTS FOR S2", localRecordingService.isStateS2());

        double actualRecordingDurationInSeconds = Math.round(localRecordingService.getRecordingDuration()/100.0)/10.0;
        double actualTotalBreakTimeInSeconds = Math.round((localRecordingService.getTotalBreakTime() + (System.currentTimeMillis() - localRecordingService.getPauseTimeStart()))/100.0)/10.0;

        assertTrue("THE DURATION OF THE EXPECTED AND THE ACTUAL RECORDING AND PAUSE TIME ARE DIFFERENT",
                expectedRecordingDurationInSeconds == actualRecordingDurationInSeconds && expectedTotalBreakTimeInSeconds == actualTotalBreakTimeInSeconds);

        System.out.println("RECORDING DURATION RESULTS      |       PAUSE DURATION RESULTS");
        System.out.println("Expected Duration: " + expectedRecordingDurationInSeconds + "          |       " + "Expected Duration: " + expectedTotalBreakTimeInSeconds);
        System.out.println("Actual Duration: " + actualRecordingDurationInSeconds + "            |       " + "Actual Duration: " + actualTotalBreakTimeInSeconds);
        System.out.println("OUTCOME: TEST SUCCESSFUL\n");
    }

    @Test
    public void ThirdSequence() {
        // S0 - S1 - S3 - S4 - S2
        Intent intent = new Intent(RuntimeEnvironment.application, com.danielkim.soundrecorder.mockclasses.LocalRecordingService.class);

        long start = System.currentTimeMillis();
        long end = 0;

        double expectedRecordingDurationInSeconds = Math.random()*10;
        while (expectedRecordingDurationInSeconds < 0.5) expectedRecordingDurationInSeconds = Math.random()*10;
        expectedRecordingDurationInSeconds = Math.round(expectedRecordingDurationInSeconds*10.0)/10.0;

        double expectedTotalBreakTimeInSeconds = Math.random()*10;
        while (expectedTotalBreakTimeInSeconds < 0.5) expectedTotalBreakTimeInSeconds = Math.random()*10;
        expectedTotalBreakTimeInSeconds = Math.round(expectedTotalBreakTimeInSeconds*10.0)/10.0;

        goStateS0();
        assertTrue("ILLEGAL STATE INVARIANTS FOR S0", localRecordingService.isStateS0());

        goStateS1(intent);
        assertTrue("ILLEGAL STATE INVARIANTS FOR S1", localRecordingService.isStateS1());

        do {
            //recording
            end = System.currentTimeMillis();
        }while (end <= start + expectedRecordingDurationInSeconds*500);

        goStateS3(intent);
        assertTrue("ILLEGAL STATE INVARIANTS FOR S3", localRecordingService.isStateS3());

        start = System.currentTimeMillis();
        do {
            //pause
            end = System.currentTimeMillis();
        }while (end <= start + expectedTotalBreakTimeInSeconds*1000);

        goStateS4(intent);
        assertTrue("ILLEGAL STATE INVARIANTS FOR S4", localRecordingService.isStateS4());

        start = System.currentTimeMillis();
        do {
            //recording
            end = System.currentTimeMillis();
        }while (end <= start + expectedRecordingDurationInSeconds*500);

        goStateS2();
        assertTrue("ILLEGAL STATE INVARIANTS FOR S2", localRecordingService.isStateS2());

        double actualRecordingDurationInSeconds = Math.round(localRecordingService.getRecordingDuration()/100.0)/10.0;
        double actualTotalBreakTimeInSeconds = Math.round((localRecordingService.getTotalBreakTime())/100.0)/10.0;

        assertTrue("THE DURATION OF THE EXPECTED AND THE ACTUAL RECORDING AND PAUSE TIME ARE DIFFERENT",
                expectedRecordingDurationInSeconds == actualRecordingDurationInSeconds && expectedTotalBreakTimeInSeconds == actualTotalBreakTimeInSeconds);

        System.out.println("RECORDING DURATION RESULTS      |       PAUSE DURATION RESULTS");
        System.out.println("Expected Duration: " + expectedRecordingDurationInSeconds + "          |       " + "Expected Duration: " + expectedTotalBreakTimeInSeconds);
        System.out.println("Actual Duration: " + actualRecordingDurationInSeconds + "            |       " + "Actual Duration: " + actualTotalBreakTimeInSeconds);
        System.out.println("OUTCOME: TEST SUCCESSFUL\n");
    }

    @Test
    public void FourthSequence() {
        // S0 - S1 - S3 - S4 - S3 - S2
        Intent intent = new Intent(RuntimeEnvironment.application, com.danielkim.soundrecorder.mockclasses.LocalRecordingService.class);

        long start = System.currentTimeMillis();
        long end = 0;

        double expectedRecordingDurationInSeconds = Math.random()*10;
        while (expectedRecordingDurationInSeconds < 0.5) expectedRecordingDurationInSeconds = Math.random()*10;
        expectedRecordingDurationInSeconds = Math.round(expectedRecordingDurationInSeconds*10.0)/10.0;

        double expectedTotalBreakTimeInSeconds = Math.random()*10;
        while (expectedTotalBreakTimeInSeconds < 0.5) expectedTotalBreakTimeInSeconds = Math.random()*10;
        expectedTotalBreakTimeInSeconds = Math.round(expectedTotalBreakTimeInSeconds*10.0)/10.0;

        goStateS0();
        assertTrue("ILLEGAL STATE INVARIANTS FOR S0", localRecordingService.isStateS0());

        goStateS1(intent);
        assertTrue("ILLEGAL STATE INVARIANTS FOR S1", localRecordingService.isStateS1());

        do {
            //recording
            end = System.currentTimeMillis();
        }while (end <= start + expectedRecordingDurationInSeconds*500);

        goStateS3(intent);
        assertTrue("ILLEGAL STATE INVARIANTS FOR S3", localRecordingService.isStateS3());

        start = System.currentTimeMillis();
        do {
            //pause
            end = System.currentTimeMillis();
        }while (end <= start + expectedTotalBreakTimeInSeconds*500);

        goStateS4(intent);
        assertTrue("ILLEGAL STATE INVARIANTS FOR S4", localRecordingService.isStateS4());

        start = System.currentTimeMillis();
        do {
            //recording
            end = System.currentTimeMillis();
        }while (end <= start + expectedRecordingDurationInSeconds*500);

        goStateS3(intent);
        assertTrue("ILLEGAL STATE INVARIANTS FOR S3", localRecordingService.isStateS3());

        start = System.currentTimeMillis();
        do {
            //pause
            end = System.currentTimeMillis();
        }while (end <= start + expectedTotalBreakTimeInSeconds*500);

        goStateS2();
        assertTrue("ILLEGAL STATE INVARIANTS FOR S2", localRecordingService.isStateS2());

        double actualRecordingDurationInSeconds = Math.round(localRecordingService.getRecordingDuration()/100.0)/10.0;
        double actualTotalBreakTimeInSeconds = Math.round((localRecordingService.getTotalBreakTime() + (System.currentTimeMillis() - localRecordingService.getPauseTimeStart()))/100.0)/10.0;

        assertTrue("THE DURATION OF THE EXPECTED AND THE ACTUAL RECORDING AND PAUSE TIME ARE DIFFERENT",
                expectedRecordingDurationInSeconds == actualRecordingDurationInSeconds && expectedTotalBreakTimeInSeconds == actualTotalBreakTimeInSeconds);

        System.out.println("RECORDING DURATION RESULTS      |       PAUSE DURATION RESULTS");
        System.out.println("Expected Duration: " + expectedRecordingDurationInSeconds + "          |       " + "Expected Duration: " + expectedTotalBreakTimeInSeconds);
        System.out.println("Actual Duration: " + actualRecordingDurationInSeconds + "            |       " + "Actual Duration: " + actualTotalBreakTimeInSeconds);
        System.out.println("OUTCOME: TEST SUCCESSFUL\n");
    }
}
