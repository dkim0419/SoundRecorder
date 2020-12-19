package com.assuntadc.soundrecorder.integrationTest;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;

import com.danielkim.soundrecorder.CustomAlertDialogForExtractedText;
import com.danielkim.soundrecorder.adapters.FileViewerAdapter;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.speech_to_text.v1.SpeechToText;
import com.ibm.watson.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.speech_to_text.v1.model.SpeechRecognitionAlternative;
import com.ibm.watson.speech_to_text.v1.model.SpeechRecognitionResult;
import com.ibm.watson.speech_to_text.v1.model.SpeechRecognitionResults;
import com.ibm.watson.speech_to_text.v1.websocket.BaseRecognizeCallback;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import static org.junit.Assert.assertNotNull;


@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21)
public class FileViewerAdapterTester {
    private Context context;
    private String audioTestFilePath;

    @Before
    public void setUp() {
        context = RuntimeEnvironment.application;
        audioTestFilePath = new File("src/test/res/rec_test.wav")
                .getAbsolutePath();
    }

    @Test
    public void startApp() throws FileNotFoundException {
        FileViewerAdapter adapter = new FileViewerAdapter(context, new LinearLayoutManager(context));
        assertNotNull("ERROR: DATABASE OBJECT IS NULL", adapter.getDatabase());

        CustomAlertDialogForExtractedText customAlertDialogForExtractedText = new CustomAlertDialogForExtractedText(context);
        customAlertDialogForExtractedText.show();
        customAlertDialogForExtractedText.setText("Testo");

        System.out.println(customAlertDialogForExtractedText.getBodyTextView().getText());
        assertNotNull("ERROR: DATABASE OBJECT IS NULL", customAlertDialogForExtractedText);

        System.out.println(contactServiceAndGetTranscript(new FileInputStream(audioTestFilePath)));

        adapter.performSpeechToText(audioTestFilePath);
    }


    private String contactServiceAndGetTranscript(FileInputStream audioInputStream){
        IamAuthenticator authenticator = new IamAuthenticator("ifXU_ZXG_ySVNViaU19SiUnILr5BkhmZJtMIcN-AL6Qc");
        SpeechToText speechToText = new SpeechToText(authenticator);
        speechToText.setServiceUrl("https://api.eu-gb.speech-to-text.watson.cloud.ibm.com/instances/b6c2ed98-71bf-4ebf-a156-af97be159062");

        final String[] transcript = new String[1];
        transcript[0] = "";

        final Boolean[] transcriptionEnded = new Boolean[1];
        transcriptionEnded[0] = false;

        RecognizeOptions recognizeOptions = new RecognizeOptions.Builder()
                .audio(audioInputStream)
                .contentType("audio/wav")
                .model("en-US_BroadbandModel")
                .maxAlternatives(5)
                .build();

        BaseRecognizeCallback baseRecognizeCallback = new BaseRecognizeCallback() {

            @Override
            public void onTranscription (SpeechRecognitionResults speechRecognitionResults) {
                System.out.println("Transcription started");
                List<SpeechRecognitionResult> results = speechRecognitionResults.getResults();
                System.out.println(" " + results.size());

                System.out.println(results.toString());

                for (int resultsIndex = 0; resultsIndex < results.size(); resultsIndex++){
                    List<SpeechRecognitionAlternative> alternatives = results.get(resultsIndex).getAlternatives();
                    System.out.println("Alternatives: " + alternatives.size());
                    System.out.println(alternatives.toString());

                    int biggerConfidenceIndex = 0;
                    double maxConfidence = 0;
                    for (int alternativeIndex = 0; alternativeIndex < alternatives.size(); alternativeIndex++){
                        double currentConfidence;

                        if (alternatives.get(alternativeIndex).getConfidence() != null) currentConfidence = alternatives.get(alternativeIndex).getConfidence();
                        else currentConfidence = 0;

                        if(currentConfidence > maxConfidence){
                            maxConfidence = currentConfidence;
                            biggerConfidenceIndex = alternativeIndex;
                        }

                        System.out.println(biggerConfidenceIndex);
                    }

                    transcript[0] += alternatives.get(biggerConfidenceIndex).getTranscript() + " ";

                    System.out.println(transcript[0]);
                }

                System.out.println("Transcription Ended");

            }

            @Override
            public void onDisconnected() {
                transcriptionEnded[0] = true;
                System.out.println("Disconnected");
                System.out.println(transcriptionEnded[0]);
            }
        };

        speechToText.recognizeUsingWebSocket(recognizeOptions, baseRecognizeCallback);
        System.out.println("Sono qui");

        while (!transcriptionEnded[0]){
            System.out.println(transcriptionEnded[0]);
        }

        System.out.println("Loop ended");
        String firstChar = transcript[0].substring(0, 1);
        return transcript[0].replace(firstChar, firstChar.toUpperCase());
    }



}
