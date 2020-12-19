package com.danielkim.soundrecorder.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.danielkim.soundrecorder.CustomAlertDialogForExtractedText;
import com.danielkim.soundrecorder.R;

public class AsyncronusRefreshing extends AsyncTask {
    private Context context;
    private boolean isLoadingEnded;
    private boolean isTaskEnded;
    private CustomAlertDialogForExtractedText customAlertDialogForExtractedText;

    public AsyncronusRefreshing(Context context, CustomAlertDialogForExtractedText customAlertDialogForExtractedText){
        this.context = context;
        this.isLoadingEnded = false;
        this.isTaskEnded = false;
        this.customAlertDialogForExtractedText = customAlertDialogForExtractedText;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        final String baseText = this.context.getResources().getString(R.string.textExtractionInProgress);
        String loadingText = baseText;

        while (!this.isLoadingEnded){
            try {
                loadingText = loadingText + ".";
                publishProgress(loadingText);

                if (loadingText.equals(baseText + "...")) loadingText = baseText;

                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        this.isTaskEnded = true;
        return null;
    }

    @Override
    protected void onProgressUpdate(Object[] values) {
        String loadingText = (String) values[0];
        this.customAlertDialogForExtractedText.setText(loadingText);
    }

    public void endTask(){
        this.isLoadingEnded = true;
    }

    public boolean isTaskEnded(){
        return this.isTaskEnded;
    }
}