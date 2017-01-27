package com.rahularodi.zephyr;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Rahul Arodi on 11/6/2016.
 */
public class UserDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "SleepInfo.db";
    private static final int DATABASE_VERSION = 1;
    private static final String CREATE_QUERY =
            "CREATE TABLE "+UserContract.NewSleepInfo.TABLE_NAME+"("+UserContract.NewSleepInfo.TIME_STAMP+" TEXT,"+
                    UserContract.NewSleepInfo.HEART_RATE+" TEXT"+ UserContract.NewSleepInfo.RR_INTERVAL+" TEXT"+ UserContract.NewSleepInfo.SPEED+" TEXT);";

    public UserDBHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.e("DATABASE OPERATIONS", "Database Created / opened");
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_QUERY);
        Log.e("DATABASE OPERATIONS", "Table Created...");
    }

    public void addInformation(String timestamp, String heartrate, String rrinterval, String speed, SQLiteDatabase db)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put(UserContract.NewSleepInfo.TIME_STAMP, timestamp);
        contentValues.put(UserContract.NewSleepInfo.HEART_RATE, heartrate);
        contentValues.put(UserContract.NewSleepInfo.RR_INTERVAL, rrinterval);
        contentValues.put(UserContract.NewSleepInfo.SPEED, speed);
        db.insert(UserContract.NewSleepInfo.TABLE_NAME, null, contentValues);
        Log.e("DATABASE OPERATIONS", "Row Inserted...");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
