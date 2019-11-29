//package com.sawyergehring.journalmatic;
//
//import android.content.Context;
//import android.database.Cursor;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.CursorAdapter;
//import android.widget.TextView;
//
///*************************************************
// * Title: NoteTakingApp
// * Author: MilindAmrutkar
// * Date: 08-11-2017
// * Availability: https://github.com/MilindAmrutkar/NoteTakingApp
// *************************************************/
//
//public class JournalCursorAdapter  extends CursorAdapter{
//
//
//    public JournalCursorAdapter(Context context, Cursor c, int flags) {
//        super(context, c, flags);
//    }
//
//    @Override
//    public View newView(Context context, Cursor cursor, ViewGroup parent) {
//        //Reading and inflating layout for the note_list_item, and passing it back whenever the newView
//        //method is called
//        return LayoutInflater.from(context).inflate(
//                R.layout.entry_list_item,
//                parent,
//                false
//        );
//    }
//
//    @Override
//    public void bindView(View view, Context context, Cursor cursor) {
//        //When you bind the view, you receive an instance of the cursor object, and it will already point to the
//        //particular row of your database that's supposed to be displayed.
//        String noteText = cursor.getString(
//                cursor.getColumnIndex(DBOpenHelper.ENTRY_TEXT)
//        );
//
//        //we'll check whether there's a line feed
//        int pos = noteText.indexOf(10); //10 is ASCII value of line feed character
//        if(pos != -1) {
//            noteText = noteText.substring(0, pos)+ " ...";
//        }
//
//        TextView tv = view.findViewById(R.id.tvEntry);
//        tv.setText(noteText);
//    }
//}