package com.inventory.Bdd;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.Date;
import java.text.SimpleDateFormat;

public class DataBase extends SQLiteOpenHelper {



    private static Context context;

    // declaration de la BDD inventory
    private static final String DATABASE_NAME = "inventory";
    private static final int DATABASE_VERSION = 3;

    // declaration  de la table DOF
    static final String TABLE_DOF="dof";
    static final String DOF_DATE = "date";
    static final String DOF_DONE = "DONE";

    // declaration de la table LINES
    static final String TABLE_LINES = "LINES";
    static final String LINES_ID = "id";
    static final String LINES_CODEBARRE = "codebarre";
    static final String LINES_DOF = "lines_dof";
    static final String LINES_SYNC = "sync";


    // declaration de la table @IP
    static final String TABLE_IP = "ip";
    static final String IP = "IP";

    public DataBase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {

        // creation de la table  DOF
        String QueryDOF = "CREATE TABLE "+ TABLE_DOF +
                "(" +
                DOF_DATE + " date PRIMARY KEY, "+
                DOF_DONE + " integer default 0" +
                ");";
        db.execSQL(QueryDOF);

        // creation de la table  LINES
        String QueryLines = "CREATE TABLE "+ TABLE_LINES+
                "(" +
                LINES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "+
                LINES_CODEBARRE + " text , " +
                LINES_DOF + " date, " +
                LINES_SYNC + " INTEGER default 0, " +
                "FOREIGN KEY ("+LINES_DOF+") REFERENCES " + TABLE_DOF + "(" + DOF_DATE + ") " +
                ");";
        db.execSQL(QueryLines);


        // creation de la table  DOF
        String QueryIP = "CREATE TABLE "+ TABLE_IP +
                "(" +
                IP + " String PRIMARY KEY "+
                ");";
        db.execSQL(QueryIP);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_DOF);
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_LINES);
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_IP);
        onCreate(db);

    }
}