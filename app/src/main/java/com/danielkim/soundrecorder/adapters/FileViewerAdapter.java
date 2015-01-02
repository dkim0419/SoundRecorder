package com.danielkim.soundrecorder.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.danielkim.soundrecorder.DBHelper;
import com.danielkim.soundrecorder.R;
import com.danielkim.soundrecorder.RecordingItem;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TooManyListenersException;

/**
 * Created by Daniel on 12/29/2014.
 */
public class FileViewerAdapter extends RecyclerView.Adapter<FileViewerAdapter.RecordingsViewHolder>{

    private static final String LOG_TAG = "FileViewerAdapter";

    private DBHelper db;
    private static final SimpleDateFormat mDateAddedFormatter = new SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault());
    private static final SimpleDateFormat mLengthFormatter = new SimpleDateFormat("mm:ss", Locale.getDefault());

    RecordingItem item;
    Context mContext;

    @Override
    public void onBindViewHolder(final RecordingsViewHolder holder, int position) {

        item = getItem(position);

        holder.vName.setText(item.getName());
        holder.vLength.setText(mLengthFormatter.format(item.getLength()));
        holder.vDateAdded.setText(mDateAddedFormatter.format((int)item.getTime()));

        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                // File delete confirm
                AlertDialog.Builder confirmDelete = new AlertDialog.Builder(mContext);
                confirmDelete.setTitle("Confirm Delete...");
                confirmDelete.setMessage("Are you sure you would like to delete this file?");
                confirmDelete.setCancelable(true);
                confirmDelete.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                try {
                                    //remove item from database, recyclerview, and storage
                                    remove(holder.getPosition());

                                } catch (Exception e) {
                                    Log.e(LOG_TAG, "exception", e);
                                }

                                dialog.cancel();
                            }
                        });
                confirmDelete.setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert = confirmDelete.create();
                alert.show();

                return false;
            }
        });
    }

    @Override
    public RecordingsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        final View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.card_view, parent, false);

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
        return db.getCount();
    }

    public RecordingItem getItem(int position) {
        return db.getItemAt(position);
    }

    public FileViewerAdapter(Context context) {
        super();
        db = new DBHelper(context);
        mContext = context;
    }

    public void remove(int position) {
        //remove item from database, recyclerview and storage

        //delete file from storage
        File file = new File(getItem(position).getFilePath());
        file.delete();

        Toast.makeText(mContext, getItem(position).getName() + " successfully deleted",
                Toast.LENGTH_SHORT).show();

        db.removeItemWithId(getItem(position).getId());
        notifyItemRemoved(position);
    }
}
