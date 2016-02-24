package br.com.zolkin.epos.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import retrofit2.Call;

/**
 * Created by Zolkin on 11/02/16.
 */
public class ePOSDatabaseHelper extends SQLiteOpenHelper {
    private static final String DBName = "ePOSTransactions";
    private static final int DBVersion = 2;

    public ePOSDatabaseHelper(Context context) {
        super(context, DBName, null, DBVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
