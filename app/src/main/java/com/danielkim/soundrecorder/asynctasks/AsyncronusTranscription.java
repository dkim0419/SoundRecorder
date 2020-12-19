package com.danielkim.soundrecorder.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.danielkim.soundrecorder.CustomAlertDialogForExtractedText;
import com.danielkim.soundrecorder.R;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.speech_to_text.v1.SpeechToText;
import com.ibm.watson.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.speech_to_text.v1.model.SpeechRecognitionAlternative;
import com.ibm.watson.speech_to_text.v1.model.SpeechRecognitionResult;
import com.ibm.watson.speech_to_text.v1.model.SpeechRecognitionResults;
import com.ibm.watson.speech_to_text.v1.websocket.BaseRecognizeCallback;

import java.io.FileInputStream;
import java.util.List;

public class AsyncronusTranscription extends AsyncTask {
    private final String apiKey = "ifXU_ZXG_ySVNViaU19SiUnILr5BkhmZJtMIcN-AL6Qc";
    private final String url = "https://api.eu-gb.speech-to-text.watson.cloud.ibm.com/instances/b6c2ed98-71bf-4ebf-a156-af97be159062";

    private Context context;
    private CustomAlertDialogForExtractedText customAlertDialogForExtractedText;
    private FileInputStream audioInputStream;
    private AsyncronusRefreshing asyncronusRefreshing;
    private boolean isTaskEnded;

    public AsyncronusTranscription(Context context, CustomAlertDialogForExtractedText customAlertDialogForExtractedText,
                                   FileInputStream audioInputStream, AsyncronusRefreshing asyncronusRefreshing){
        this.context = context;
        this.customAlertDialogForExtractedText = customAlertDialogForExtractedText;
        this.audioInputStream = audioInputStream;
        this.asyncronusRefreshing = asyncronusRefreshing;
        this.isTaskEnded = false;
    }

    public CustomAlertDialogForExtractedText getCustomAlertDialogForExtractedText() {
        return customAlertDialogForExtractedText;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        //Layout building test string
        //String extractedText = (String) mContext.getResources().getString(R.string.speech_to_text_example);

        String extractedText = contactServiceAndGetTranscript(this.audioInputStream);
        this.asyncronusRefreshing.endTask();
        return extractedText;
    }

    @Override
    protected void onPostExecute(Object result) {
        String extractedText = (String) result;

        while (!this.asyncronusRefreshing.isTaskEnded()){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Log.e("sleep", "The sleep operation has been interrupted.");
            }
        }

        if (!extractedText.equals("")) this.customAlertDialogForExtractedText.setText(extractedText);
        else {
            this.customAlertDialogForExtractedText.setButtonCopyEnabled(false);
            this.customAlertDialogForExtractedText.setText(this.context.getResources().getString(R.string.toast_unable_to_extract_text));
        }

        this.isTaskEnded = true;
    }

    public boolean isTaskEnded (){
        return isTaskEnded;
    }

    private String contactServiceAndGetTranscript(FileInputStream audioInputStream) {
        IamAuthenticator authenticator = new IamAuthenticator(this.apiKey);
        SpeechToText speechToText = new SpeechToText(authenticator);
        speechToText.setServiceUrl(this.url);

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
                List<SpeechRecognitionResult> results = speechRecognitionResults.getResults();

                for (int resultsIndex = 0; resultsIndex < results.size(); resultsIndex++){
                    List<SpeechRecognitionAlternative> alternatives = results.get(resultsIndex).getAlternatives();

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
                    }

                    transcript[0] += alternatives.get(biggerConfidenceIndex).getTranscript() + " ";
                }
            }

            @Override
            public void onDisconnected() {
                transcriptionEnded[0] = true;
            }
        };

        speechToText.recognizeUsingWebSocket(recognizeOptions, baseRecognizeCallback);

        while (!transcriptionEnded[0]){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Log.e("sleep", "The sleep operation has been interrupted.");
            }
        }

        String extractedText = "";
        if(transcript[0].length() > 0) extractedText = transcript[0].substring(0, 1).toUpperCase() + transcript[0].substring(1);

        return extractedText;
    }
}
