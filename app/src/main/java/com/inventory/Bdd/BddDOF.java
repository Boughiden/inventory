package com.inventory.Bdd;

import static com.inventory.Bdd.DataBase.DOF_DATE;
import static com.inventory.Bdd.DataBase.DOF_DONE;
import static com.inventory.Bdd.DataBase.IP;
import static com.inventory.Bdd.DataBase.TABLE_DOF;
import static com.inventory.Bdd.DataBase.TABLE_IP;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import org.jetbrains.annotations.Nullable;

public class BddDOF {
        private static Context context;
        private static DataBase dbHelper;


        public BddDOF(Context context) {
            this.context = context;
            dbHelper = new DataBase(context);
        }

    // la fonction select
    public static Cursor readAllData() {
        String query = "SELECT " + DOF_DATE + " FROM " + TABLE_DOF + " WHERE " + DOF_DONE + " = 0 LIMIT 1";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        if (db != null) {
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }

    public void addDof(  @Nullable String date) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DOF_DATE, date);


        long result = db.insert(TABLE_DOF, null, cv);
        if (result == -1) {
            Toast.makeText(context, "Failed", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "DOF Ajouté", Toast.LENGTH_LONG).show();
        }
    }

    public void addIP(@Nullable String ip) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.beginTransaction();
        try {
            db.delete(TABLE_IP, null, null);

            ContentValues cv = new ContentValues();
            cv.put(IP, ip);
            long result = db.insert(TABLE_IP, null, cv);

            if (result == -1) {
                Toast.makeText(context, "IP non ajoutée", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "IP Ajouté", Toast.LENGTH_LONG).show();
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("addIP", "Erreur en ajoutant @IP: " + e.getMessage());
        } finally {
            db.endTransaction();
        }
    }

    public long DOfEnd(String date){
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(DOF_DONE,1);
            long result = db.update(TABLE_DOF, cv, DOF_DATE + " = ?", new String[] { date });
            if (result == -1){
                Toast.makeText(context,"inventaire non terminé", Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(context, "inventaire terminé", Toast.LENGTH_LONG).show();
            }

            return result;
    }


    public Cursor getIP() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String query = "SELECT distinct " + IP + " from " + TABLE_IP;
        Cursor cursor = null;
        if (db != null) {
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }

}


