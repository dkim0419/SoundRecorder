package com.danielkim.soundrecorder.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.danielkim.soundrecorder.DBHelper;
import com.danielkim.soundrecorder.R;
import com.danielkim.soundrecorder.RecordingItem;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by Daniel on 12/29/2014.
 */
public class FileViewerAdapter extends RecyclerView.Adapter<FileViewerAdapter.RecordingsViewHolder>{

    private DBHelper db;
    private static final SimpleDateFormat mDateAddedFormatter = new SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault());
    private static final SimpleDateFormat mLengthFormatter = new SimpleDateFormat("mm:ss", Locale.getDefault());

    @Override
    public void onBindViewHolder(RecordingsViewHolder holder, int position) {
        RecordingItem item = getItem(position);

        holder.vName.setText(item.getName());
        holder.vLength.setText(mLengthFormatter.format(item.getLength()));
        holder.vDateAdded.setText(mDateAddedFormatter.format((int)item.getTime()));
    }

    @Override
    public RecordingsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.card_view, parent, false);

        return new RecordingsViewHolder(itemView);
    }


    public static class RecordingsViewHolder extends RecyclerView.ViewHolder {
        protected TextView vName;
        protected TextView vLength;
        protected TextView vDateAdded;

        public RecordingsViewHolder(View v) {
            super(v);
            vName = (TextView) v.findViewById(R.id.file_name_text);
            vLength = (TextView) v.findViewById(R.id.file_length_text);
            vDateAdded = (TextView) v.findViewById(R.id.file_date_added_text);
        }
    }

    @Override
    public int getItemCount() {
        return db.getCount();
    }

    public RecordingItem getItem(int position) {
        return db.getItemAt(position);
    }

    public FileViewerAdapter(Context context) {
        super();
        db = new DBHelper(context);
    }
}
