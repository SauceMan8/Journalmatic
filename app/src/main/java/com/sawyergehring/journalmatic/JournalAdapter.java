package com.sawyergehring.journalmatic;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static java.text.DateFormat.getDateInstance;

public class JournalAdapter extends RecyclerView.Adapter<JournalAdapter.JournalViewHolder> {

    private Context mContext;
    private Cursor mCursor;
    private DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);



    public JournalAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
    }

    public class JournalViewHolder extends RecyclerView.ViewHolder {
        public TextView timestampText;
        public TextView contentText;

        public JournalViewHolder(@NonNull View itemView) {
            super(itemView);

            contentText = itemView.findViewById(R.id.entry_list_item_text);
            timestampText = itemView.findViewById(R.id.entry_list_date_text);

        }
    }


    @NonNull
    @Override
    public JournalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.entry_item, parent, false);
        return new JournalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JournalViewHolder holder, int position) {
        if (!mCursor.moveToPosition(position)) {
            return;
        }

        String content = mCursor.getString(mCursor.getColumnIndex(JournalContract.JournalEntry.COLUMN_TEXT));
        String dateString = mCursor.getString(mCursor.getColumnIndex(JournalContract.JournalEntry.COLUMN_DATE));

        holder.timestampText.setText(dateString);
        holder.contentText.setText(content);
    }

    private String trimDate(String dateString) {
        if (dateString == null) {
            return "date missing";
        }
        Date myDate = null;
        DateFormat inputFormatter = new SimpleDateFormat("yyyy/MM/dd HH:MM:SS.SSS", Locale.US);
        try {
            myDate = inputFormatter.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (myDate == null) {
            return "failed to parse Date";
        }
        return df.format(myDate);
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        if (mCursor != null) {
            mCursor.close();
        }

        mCursor = newCursor;

        if (newCursor != null) {
            notifyDataSetChanged();
        }
    }
}
