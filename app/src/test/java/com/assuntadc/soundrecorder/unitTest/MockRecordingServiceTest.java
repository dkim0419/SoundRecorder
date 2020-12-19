package com.assuntadc.soundrecorder.unitTest;

import android.content.Context;
import android.content.Intent;

import com.danielkim.soundrecorder.mockclasses.MockRecordingService;

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
public class MockRecordingServiceTest {
    private static int startId = 0;
    private Context context;
    private ServiceController<MockRecordingService> serviceController;
    private MockRecordingService mockRecordingService;

    @Before
    public void setUp() {
        context = RuntimeEnvironment.application;
        serviceController = Robolectric.buildService(MockRecordingService.class);
    }

    private void goStateS0(){
        mockRecordingService = serviceController.create().get();
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
        System.out.println("RECORDING SERVICE UNIT TEST - TEST N. 1");
        Intent intent = new Intent(context, MockRecordingService.class);

        long start = System.currentTimeMillis();
        long end = 0;

        double expectedRecordingDurationInSeconds = Math.random()*10;
        while (expectedRecordingDurationInSeconds < 0.5) expectedRecordingDurationInSeconds = Math.random()*10;
        expectedRecordingDurationInSeconds = Math.round(expectedRecordingDurationInSeconds*10.0)/10.0;

        goStateS0();
        assertTrue("ILLEGAL STATE INVARIANTS FOR S0", mockRecordingService.isStateS0());

        goStateS1(intent);
        assertTrue("ILLEGAL STATE INVARIANTS FOR S1", mockRecordingService.isStateS1());

        do {
            //recording
            end = System.currentTimeMillis();
        }while (end <= start + expectedRecordingDurationInSeconds*1000);

        goStateS2();
        assertTrue("ILLEGAL STATE INVARIANTS FOR S2", mockRecordingService.isStateS2());

        double actualRecordingDurationInSeconds = Math.round(mockRecordingService.getRecordingDuration()/100.0)/10.0;

        assertTrue("THE DURATION OF THE EXPECTED AND THE ACTUAL RECORDING TIME ARE DIFFERENT" + "\n" +
                "                                      RECORDING DURATION RESULTS" + "\n" +
                "                                      Expected Duration: " + expectedRecordingDurationInSeconds + "\n" +
                "                                      Actual Duration: " + actualRecordingDurationInSeconds + "\n\n" +
                "                                      OUTCOME: TEST FAILED\n", expectedRecordingDurationInSeconds == actualRecordingDurationInSeconds);

        System.out.println("RECORDING DURATION RESULTS");
        System.out.println("Expected Duration: " + expectedRecordingDurationInSeconds);
        System.out.println("Actual Duration: " + actualRecordingDurationInSeconds);
        System.out.println("\nOUTCOME: TEST SUCCESSFUL\n");
    }

    @Test
    public void SecondSequence() {
        // S0 - S1 - S3 - S2
        System.out.println("RECORDING SERVICE UNIT TEST - TEST N. 2");
        Intent intent = new Intent(context, MockRecordingService.class);

        long start = System.currentTimeMillis();
        long end = 0;

        double expectedRecordingDurationInSeconds = Math.random()*10;
        while (expectedRecordingDurationInSeconds < 0.5) expectedRecordingDurationInSeconds = Math.random()*10;
        expectedRecordingDurationInSeconds = Math.round(expectedRecordingDurationInSeconds*10.0)/10.0;

        double expectedTotalBreakTimeInSeconds = Math.random()*10;
        while (expectedTotalBreakTimeInSeconds < 0.5) expectedTotalBreakTimeInSeconds = Math.random()*10;
        expectedTotalBreakTimeInSeconds = Math.round(expectedTotalBreakTimeInSeconds*10.0)/10.0;

        goStateS0();
        assertTrue("ILLEGAL STATE INVARIANTS FOR S0", mockRecordingService.isStateS0());

        goStateS1(intent);
        assertTrue("ILLEGAL STATE INVARIANTS FOR S1", mockRecordingService.isStateS1());

        do {
            //recording
            end = System.currentTimeMillis();
        }while (end <= start + expectedRecordingDurationInSeconds*1000);

        goStateS3(intent);
        assertTrue("ILLEGAL STATE INVARIANTS FOR S3", mockRecordingService.isStateS3());

        start = System.currentTimeMillis();
        do {
            //pause
            end = System.currentTimeMillis();
        }while (end <= start + expectedTotalBreakTimeInSeconds*1000);

        goStateS2();
        assertTrue("ILLEGAL STATE INVARIANTS FOR S2", mockRecordingService.isStateS2());

        double actualRecordingDurationInSeconds = Math.round(mockRecordingService.getRecordingDuration()/100.0)/10.0;
        double actualTotalBreakTimeInSeconds = Math.round((mockRecordingService.getTotalBreakTime() + (System.currentTimeMillis() - mockRecordingService.getPauseTimeStart()))/100.0)/10.0;

        assertTrue("THE DURATION OF THE EXPECTED AND THE ACTUAL RECORDING AND PAUSE TIME ARE DIFFERENT" + "\n" +
                "                                      RECORDING DURATION RESULTS      |       PAUSE DURATION RESULTS" + "\n" +
                "                                      Expected Duration: " + expectedRecordingDurationInSeconds + "          |       " + "Expected Duration: " + expectedTotalBreakTimeInSeconds + "\n" +
                "                                      Actual Duration: " + actualRecordingDurationInSeconds + "            |       " + "Actual Duration: " + actualTotalBreakTimeInSeconds + "\n\n" +
                "                                      OUTCOME: TEST FAILED\n",
                expectedRecordingDurationInSeconds == actualRecordingDurationInSeconds && expectedTotalBreakTimeInSeconds == actualTotalBreakTimeInSeconds);

        System.out.println("RECORDING DURATION RESULTS      |       PAUSE DURATION RESULTS");
        System.out.println("Expected Duration: " + expectedRecordingDurationInSeconds + "          |       " + "Expected Duration: " + expectedTotalBreakTimeInSeconds);
        System.out.println("Actual Duration: " + actualRecordingDurationInSeconds + "            |       " + "Actual Duration: " + actualTotalBreakTimeInSeconds);
        System.out.println("\nOUTCOME: TEST SUCCESSFUL\n");
    }

    @Test
    public void ThirdSequence() {
        // S0 - S1 - S3 - S4 - S2
        System.out.println("RECORDING SERVICE UNIT TEST - TEST N. 3");
        Intent intent = new Intent(context, MockRecordingService.class);

        long start = System.currentTimeMillis();
        long end = 0;

        double expectedRecordingDurationInSeconds = Math.random()*10;
        while (expectedRecordingDurationInSeconds < 0.5) expectedRecordingDurationInSeconds = Math.random()*10;
        expectedRecordingDurationInSeconds = Math.round(expectedRecordingDurationInSeconds*10.0)/10.0;

        double expectedTotalBreakTimeInSeconds = Math.random()*10;
        while (expectedTotalBreakTimeInSeconds < 0.5) expectedTotalBreakTimeInSeconds = Math.random()*10;
        expectedTotalBreakTimeInSeconds = Math.round(expectedTotalBreakTimeInSeconds*10.0)/10.0;

        goStateS0();
        assertTrue("ILLEGAL STATE INVARIANTS FOR S0", mockRecordingService.isStateS0());

        goStateS1(intent);
        assertTrue("ILLEGAL STATE INVARIANTS FOR S1", mockRecordingService.isStateS1());

        do {
            //recording
            end = System.currentTimeMillis();
        }while (end <= start + expectedRecordingDurationInSeconds*500);

        goStateS3(intent);
        assertTrue("ILLEGAL STATE INVARIANTS FOR S3", mockRecordingService.isStateS3());

        start = System.currentTimeMillis();
        do {
            //pause
            end = System.currentTimeMillis();
        }while (end <= start + expectedTotalBreakTimeInSeconds*1000);

        goStateS4(intent);
        assertTrue("ILLEGAL STATE INVARIANTS FOR S4", mockRecordingService.isStateS4());

        start = System.currentTimeMillis();
        do {
            //recording
            end = System.currentTimeMillis();
        }while (end <= start + expectedRecordingDurationInSeconds*500);

        goStateS2();
        assertTrue("ILLEGAL STATE INVARIANTS FOR S2", mockRecordingService.isStateS2());

        double actualRecordingDurationInSeconds = Math.round(mockRecordingService.getRecordingDuration()/100.0)/10.0;
        double actualTotalBreakTimeInSeconds = Math.round((mockRecordingService.getTotalBreakTime())/100.0)/10.0;

        assertTrue("THE DURATION OF THE EXPECTED AND THE ACTUAL RECORDING AND PAUSE TIME ARE DIFFERENT" + "\n" +
                        "                                      RECORDING DURATION RESULTS      |       PAUSE DURATION RESULTS" + "\n" +
                        "                                      Expected Duration: " + expectedRecordingDurationInSeconds + "          |       " + "Expected Duration: " + expectedTotalBreakTimeInSeconds + "\n" +
                        "                                      Actual Duration: " + actualRecordingDurationInSeconds + "            |       " + "Actual Duration: " + actualTotalBreakTimeInSeconds + "\n\n" +
                        "                                      OUTCOME: TEST FAILED\n",
                expectedRecordingDurationInSeconds == actualRecordingDurationInSeconds && expectedTotalBreakTimeInSeconds == actualTotalBreakTimeInSeconds);

        System.out.println("RECORDING DURATION RESULTS      |       PAUSE DURATION RESULTS");
        System.out.println("Expected Duration: " + expectedRecordingDurationInSeconds + "          |       " + "Expected Duration: " + expectedTotalBreakTimeInSeconds);
        System.out.println("Actual Duration: " + actualRecordingDurationInSeconds + "            |       " + "Actual Duration: " + actualTotalBreakTimeInSeconds);
        System.out.println("\nOUTCOME: TEST SUCCESSFUL\n");
    }

    @Test
    public void FourthSequence() {
        // S0 - S1 - S3 - S4 - S3 - S2
        System.out.println("RECORDING SERVICE UNIT TEST - TEST N. 4");
        Intent intent = new Intent(context, MockRecordingService.class);

        long start = System.currentTimeMillis();
        long end = 0;

        double expectedRecordingDurationInSeconds = Math.random()*10;
        while (expectedRecordingDurationInSeconds < 0.5) expectedRecordingDurationInSeconds = Math.random()*10;
        expectedRecordingDurationInSeconds = Math.round(expectedRecordingDurationInSeconds*10.0)/10.0;

        double expectedTotalBreakTimeInSeconds = Math.random()*10;
        while (expectedTotalBreakTimeInSeconds < 0.5) expectedTotalBreakTimeInSeconds = Math.random()*10;
        expectedTotalBreakTimeInSeconds = Math.round(expectedTotalBreakTimeInSeconds*10.0)/10.0;

        goStateS0();
        assertTrue("ILLEGAL STATE INVARIANTS FOR S0", mockRecordingService.isStateS0());

        goStateS1(intent);
        assertTrue("ILLEGAL STATE INVARIANTS FOR S1", mockRecordingService.isStateS1());

        do {
            //recording
            end = System.currentTimeMillis();
        }while (end <= start + expectedRecordingDurationInSeconds*500);

        goStateS3(intent);
        assertTrue("ILLEGAL STATE INVARIANTS FOR S3", mockRecordingService.isStateS3());

        start = System.currentTimeMillis();
        do {
            //pause
            end = System.currentTimeMillis();
        }while (end <= start + expectedTotalBreakTimeInSeconds*500);

        goStateS4(intent);
        assertTrue("ILLEGAL STATE INVARIANTS FOR S4", mockRecordingService.isStateS4());

        start = System.currentTimeMillis();
        do {
            //recording
            end = System.currentTimeMillis();
        }while (end <= start + expectedRecordingDurationInSeconds*500);

        goStateS3(intent);
        assertTrue("ILLEGAL STATE INVARIANTS FOR S3", mockRecordingService.isStateS3());

        start = System.currentTimeMillis();
        do {
            //pause
            end = System.currentTimeMillis();
        }while (end <= start + expectedTotalBreakTimeInSeconds*500);

        goStateS2();
        assertTrue("ILLEGAL STATE INVARIANTS FOR S2", mockRecordingService.isStateS2());

        double actualRecordingDurationInSeconds = Math.round(mockRecordingService.getRecordingDuration()/100.0)/10.0;
        double actualTotalBreakTimeInSeconds = Math.round((mockRecordingService.getTotalBreakTime() + (System.currentTimeMillis() - mockRecordingService.getPauseTimeStart()))/100.0)/10.0;

        assertTrue("THE DURATION OF THE EXPECTED AND THE ACTUAL RECORDING AND PAUSE TIME ARE DIFFERENT" + "\n" +
                        "                                      RECORDING DURATION RESULTS      |       PAUSE DURATION RESULTS" + "\n" +
                        "                                      Expected Duration: " + expectedRecordingDurationInSeconds + "          |       " + "Expected Duration: " + expectedTotalBreakTimeInSeconds + "\n" +
                        "                                      Actual Duration: " + actualRecordingDurationInSeconds + "            |       " + "Actual Duration: " + actualTotalBreakTimeInSeconds + "\n\n" +
                        "                                      OUTCOME: TEST FAILED\n",
                expectedRecordingDurationInSeconds == actualRecordingDurationInSeconds && expectedTotalBreakTimeInSeconds == actualTotalBreakTimeInSeconds);

        System.out.println("RECORDING DURATION RESULTS      |       PAUSE DURATION RESULTS");
        System.out.println("Expected Duration: " + expectedRecordingDurationInSeconds + "          |       " + "Expected Duration: " + expectedTotalBreakTimeInSeconds);
        System.out.println("Actual Duration: " + actualRecordingDurationInSeconds + "            |       " + "Actual Duration: " + actualTotalBreakTimeInSeconds);
        System.out.println("\nOUTCOME: TEST SUCCESSFUL\n");
    }
}
