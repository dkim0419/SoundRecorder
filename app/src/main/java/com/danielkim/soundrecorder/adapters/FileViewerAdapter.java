package com.danielkim.soundrecorder.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.danielkim.soundrecorder.CustomAlertDialogForExtractedText;
import com.danielkim.soundrecorder.DBHelper;
import com.danielkim.soundrecorder.R;
import com.danielkim.soundrecorder.RecordingItem;
import com.danielkim.soundrecorder.fragments.PlaybackFragment;
import com.danielkim.soundrecorder.listeners.OnDatabaseChangedListener;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.speech_to_text.v1.SpeechToText;
import com.ibm.watson.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.speech_to_text.v1.model.SpeechRecognitionAlternative;
import com.ibm.watson.speech_to_text.v1.model.SpeechRecognitionResult;
import com.ibm.watson.speech_to_text.v1.model.SpeechRecognitionResults;
import com.ibm.watson.speech_to_text.v1.websocket.BaseRecognizeCallback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Daniel on 12/29/2014.
 */
public class FileViewerAdapter extends RecyclerView.Adapter<FileViewerAdapter.RecordingsViewHolder>
    implements OnDatabaseChangedListener {

    private static final String LOG_TAG = "FileViewerAdapter";
    private DBHelper mDatabase;
    private RecordingItem item;
    private Context mContext;
    private LinearLayoutManager llm;
    private final String apiKey = "ifXU_ZXG_ySVNViaU19SiUnILr5BkhmZJtMIcN-AL6Qc";
    private final String url = "https://api.eu-gb.speech-to-text.watson.cloud.ibm.com/instances/b6c2ed98-71bf-4ebf-a156-af97be159062";

    public FileViewerAdapter(Context context, LinearLayoutManager linearLayoutManager) {
        super();
        this.mContext = context;
        this.mDatabase = DBHelper.getInstance(mContext);
        this.mDatabase.checkConsistencyWithFileSistem();
        this.mDatabase.setOnDatabaseChangedListener(this);
        this.llm = linearLayoutManager;
    }

    @Override
    public void onBindViewHolder(final RecordingsViewHolder holder, int position) {
        this.item = getItem(position);
        long itemDuration = item.getLength();

        long minutes = TimeUnit.MILLISECONDS.toMinutes(itemDuration);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(itemDuration) - TimeUnit.MINUTES.toSeconds(minutes);

        holder.vName.setText(item.getName());
        holder.vLength.setText(String.format("%02d:%02d", minutes, seconds));
        holder.vDateAdded.setText(
            DateUtils.formatDateTime(
                mContext,
                item.getTime(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_YEAR
            )
        );

        // define an on click listener to open PlaybackFragment
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getPosition();
                File file = new File(getItem(position).getFilePath());

                if (file.exists()){
                    try {
                        PlaybackFragment playbackFragment = new PlaybackFragment().newInstance(getItem(holder.getPosition()));
                        FragmentTransaction transaction = ((FragmentActivity) mContext)
                                .getSupportFragmentManager()
                                .beginTransaction();

                        playbackFragment.show(transaction, "dialog_playback");

                    } catch (Exception e) {
                        Log.e(LOG_TAG, "exception", e);
                    }
                }
                else Toast.makeText(mContext, mContext.getResources().getString(R.string.toast_file_does_not_exist), Toast.LENGTH_LONG).show();
            }
        });

        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final ArrayList<String> entrys = new ArrayList<String>();
                entrys.add(mContext.getString(R.string.dialog_file_convert));
                entrys.add(mContext.getString(R.string.dialog_file_share));
                entrys.add(mContext.getString(R.string.dialog_file_rename));
                entrys.add(mContext.getString(R.string.dialog_file_delete));

                final CharSequence[] items = entrys.toArray(new CharSequence[entrys.size()]);

                // File delete confirm
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(mContext.getString(R.string.dialog_title_options));
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (item == 0){
                            if(isDeviceConnected()){
                                FileInputStream audioInputStream;
                                try {
                                    audioInputStream = new FileInputStream(getItem(holder.getPosition()).getFilePath());

                                    CustomAlertDialogForExtractedText customAlertDialogForExtractedText = new CustomAlertDialogForExtractedText(mContext);
                                    customAlertDialogForExtractedText.show();
                                    customAlertDialogForExtractedText.setText(mContext.getResources().getString(R.string.textExtractionInProgress));

                                    AsyncronusRefreshing asyncronusRefreshing = new AsyncronusRefreshing(customAlertDialogForExtractedText);
                                    AsyncronusTranscription asyncronusTranscription = new AsyncronusTranscription(customAlertDialogForExtractedText, audioInputStream, asyncronusRefreshing);

                                    asyncronusRefreshing.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
                                    asyncronusTranscription.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);

                                } catch (FileNotFoundException e) {
                                   Toast.makeText(mContext, mContext.getResources().getString(R.string.toast_file_does_not_exist), Toast.LENGTH_LONG).show();
                                }
                            }else{
                                new AlertDialog.Builder(mContext)
                                        .setTitle(mContext.getString(R.string.dialog_device_not_connected_title))
                                        .setMessage(mContext.getString(R.string.dialog_device_not_connected_message))
                                        .setPositiveButton(android.R.string.ok, null)
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .show();
                            }
                        }
                        else if (item == 1) {
                            if(isDeviceConnected()){
                                try {
                                    shareFileDialog(holder.getPosition());
                                }
                                catch (FileNotFoundException e){
                                    Toast.makeText(mContext, mContext.getResources().getString(R.string.toast_file_does_not_exist), Toast.LENGTH_LONG).show();
                                }
                            }else{
                                new AlertDialog.Builder(mContext)
                                        .setTitle(mContext.getString(R.string.dialog_device_not_connected_title))
                                        .setMessage(mContext.getString(R.string.dialog_device_not_connected_message))
                                        .setPositiveButton(android.R.string.ok, null)
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .show();
                            }
                        }
                        else if (item == 2) {
                            try {
                                renameFileDialog(holder.getPosition());
                            }
                            catch (FileNotFoundException e){
                                Toast.makeText(mContext, mContext.getResources().getString(R.string.toast_file_does_not_exist), Toast.LENGTH_LONG).show();
                            }
                        }
                        else if (item == 3) {
                            deleteFileDialog(holder.getPosition());
                        }
                    }
                });
                builder.setCancelable(true);
                builder.setNegativeButton(mContext.getString(R.string.dialog_action_cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert = builder.create();
                alert.show();

                return false;
            }
        });
    }

    private String contactServiceAndGetTranscript(FileInputStream audioInputStream){
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

        while (!transcriptionEnded[0]);

        String firstChar = transcript[0].substring(0, 1);
        return transcript[0].replace(firstChar, firstChar.toUpperCase());
    }

    @Override
    public RecordingsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view, parent, false);

        mContext = parent.getContext();

        return new RecordingsViewHolder(itemView);
    }

    public static class RecordingsViewHolder extends RecyclerView.ViewHolder {
        protected TextView vName;
        protected TextView vLength;
        protected TextView vDateAdded;
        protected View cardView;

        public RecordingsViewHolder(View v) {
            super(v);
            vName = (TextView) v.findViewById(R.id.file_name_text);
            vLength = (TextView) v.findViewById(R.id.file_length_text);
            vDateAdded = (TextView) v.findViewById(R.id.file_date_added_text);
            cardView = v.findViewById(R.id.card_view);
        }
    }

    @Override
    public int getItemCount() {
        return mDatabase.getCount();
    }

    public RecordingItem getItem(int position) {
        return mDatabase.getItemAt(position);
    }

    @Override
    public void onNewDatabaseEntryAdded() {
        //item added to top of the list
        notifyItemInserted(getItemCount() - 1);
        llm.scrollToPosition(getItemCount() - 1);
    }

    @Override
    //TODO
    public void onDatabaseEntryRenamed() {

    }

    public void remove(int position) {
        //remove item from database, recyclerview and storage

        //delete file from storage
        File file = new File(getItem(position).getFilePath());
        file.delete();

        Toast.makeText(
            mContext,
            String.format(
                mContext.getString(R.string.toast_file_delete),
                getItem(position).getName()
            ),
            Toast.LENGTH_SHORT
        ).show();

        mDatabase.removeItemWithId(getItem(position).getId());
        notifyItemRemoved(position);
    }

    //TODO
    public void removeOutOfApp(String filePath) {
        //user deletes a saved recording out of the application through another application
    }

    private boolean newNameAlreadyExists(String newName){
        boolean nameAlreadyExsists = false;

        for (int i = 0; i < this.mDatabase.getCount() && !nameAlreadyExsists; i++){
            if (newName.equals(this.mDatabase.getItemAt(i).getName())) nameAlreadyExsists = true;
        }

        return nameAlreadyExsists;
    }

    public void rename(int position, String name) {
        //rename a file
        String mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFilePath += "/SoundRecorder/" + name;
        File f = new File(mFilePath);

        if (f.exists() && !f.isDirectory()) {
            //file name is not unique, cannot rename file.
            Toast.makeText(mContext, String.format(mContext.getString(R.string.toast_file_exists), name), Toast.LENGTH_SHORT).show();
        }
        else if (newNameAlreadyExists(name)){
            Toast.makeText(mContext, mContext.getResources().getString(R.string.nameAlreadyExistsInDB), Toast.LENGTH_SHORT).show();
        }
        else {
            //file name is unique, rename file
            File oldFilePath = new File(getItem(position).getFilePath());
            oldFilePath.renameTo(f);
            mDatabase.renameItem(getItem(position), name, mFilePath);
            notifyItemChanged(position);
        }
    }

    public void shareFileDialog(int position) throws FileNotFoundException{
        File file = new File(getItem(position).getFilePath());

        if(file.exists()){
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(getItem(position).getFilePath())));
            shareIntent.setType("audio/wav");
            mContext.startActivity(Intent.createChooser(shareIntent, mContext.getText(R.string.send_to)));
        }
        else{
            throw new FileNotFoundException();
        }
    }

    public void renameFileDialog (final int position) throws FileNotFoundException{
        File file = new File(getItem(position).getFilePath());

        if (file.exists()){
            // File rename dialog
            AlertDialog.Builder renameFileBuilder = new AlertDialog.Builder(mContext);

            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.dialog_rename_file, null);

            final EditText input = (EditText) view.findViewById(R.id.new_name);
            input.setText(file.getName().replace(".wav", ""));

            renameFileBuilder.setTitle(mContext.getString(R.string.dialog_title_rename));
            renameFileBuilder.setCancelable(true);
            renameFileBuilder.setPositiveButton(mContext.getString(R.string.dialog_action_ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            try {
                                String value = input.getText().toString().trim() + ".wav";
                                rename(position, value);

                            } catch (Exception e) {
                                Log.e(LOG_TAG, "exception", e);
                            }

                            dialog.cancel();
                        }
                    });
            renameFileBuilder.setNegativeButton(mContext.getString(R.string.dialog_action_cancel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            renameFileBuilder.setView(view);
            AlertDialog alert = renameFileBuilder.create();
            alert.show();
        }
        else throw new FileNotFoundException();
    }

    public void deleteFileDialog (final int position) {
        // File delete confirm
        AlertDialog.Builder confirmDelete = new AlertDialog.Builder(mContext);
        confirmDelete.setTitle(mContext.getString(R.string.dialog_title_delete));
        confirmDelete.setMessage(mContext.getString(R.string.dialog_text_delete));
        confirmDelete.setCancelable(true);
        confirmDelete.setPositiveButton(mContext.getString(R.string.dialog_action_yes),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            //remove item from database, recyclerview, and storage
                            remove(position);

                        } catch (Exception e) {
                            Log.e(LOG_TAG, "exception", e);
                        }

                        dialog.cancel();
                    }
                });
        confirmDelete.setNegativeButton(mContext.getString(R.string.dialog_action_no),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = confirmDelete.create();
        alert.show();
    }

    private boolean isDeviceConnected(){
        boolean isConnected = false;

        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();

        if(ni != null) isConnected = ni.isConnected();

        return isConnected;
    }

    class AsyncronusRefreshing extends AsyncTask{
        private boolean isLoadingEnded;
        private CustomAlertDialogForExtractedText customAlertDialogForExtractedText;

        public AsyncronusRefreshing(CustomAlertDialogForExtractedText customAlertDialogForExtractedText){
            this.isLoadingEnded = false;
            this.customAlertDialogForExtractedText = customAlertDialogForExtractedText;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            final String baseText = mContext.getResources().getString(R.string.textExtractionInProgress);
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
    }

    class AsyncronusTranscription extends AsyncTask{
        private CustomAlertDialogForExtractedText customAlertDialogForExtractedText;
        private FileInputStream audioInputStream;
        private AsyncronusRefreshing asyncronusRefreshing;

        public AsyncronusTranscription(CustomAlertDialogForExtractedText customAlertDialogForExtractedText, FileInputStream audioInputStream, AsyncronusRefreshing asyncronusRefreshing){
            this.customAlertDialogForExtractedText = customAlertDialogForExtractedText;
            this.audioInputStream = audioInputStream;
            this.asyncronusRefreshing = asyncronusRefreshing;
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

            if (!extractedText.equals("")) this.customAlertDialogForExtractedText.setText(extractedText);
            else {
                this.customAlertDialogForExtractedText.setButtonCopyEnabled(false);
                this.customAlertDialogForExtractedText.setText(mContext.getResources().getString(R.string.toast_unable_to_extract_text));
            }
        }
    }
}
