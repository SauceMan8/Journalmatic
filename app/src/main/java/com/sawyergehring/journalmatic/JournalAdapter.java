package com.sawyergehring.journalmatic;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class JournalAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private Cursor mCursor;



    public JournalAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
    }

    public class JournalViewHolder extends RecyclerView.ViewHolder {
        public TextView content;

        public JournalViewHolder(@NonNull View itemView) {
            super(itemView);

            content = itemView.findViewById(R.id.editText2);

        }
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
