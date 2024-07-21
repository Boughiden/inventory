package com.inventory.Bdd;

import static com.inventory.Bdd.DataBase.DOF_DATE;
import static com.inventory.Bdd.DataBase.LINES_CODEBARRE;
import static com.inventory.Bdd.DataBase.LINES_DOF;
import static com.inventory.Bdd.DataBase.LINES_SYNC;
import static com.inventory.Bdd.DataBase.TABLE_DOF;
import static com.inventory.Bdd.DataBase.TABLE_LINES;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.inventory.NetworkConfig;

import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BddLines {

    private static Context context;
    private static DataBase dbHelper;


    public BddLines(Context context) {
        this.context = context;
        dbHelper = new DataBase(context);
    }

    // la fonction select
    public static Cursor readAllData() {
        String query = "SELECT " + LINES_CODEBARRE + ", "+LINES_DOF+ " FROM " + TABLE_LINES +" WHERE "+ LINES_SYNC +" = 0" ;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        if (db != null) {
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }


    public void update(String code){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(LINES_SYNC, 1);
        long result = db.update(TABLE_LINES, cv, LINES_CODEBARRE + " = ?", new String[] { code });
        if (result == -1){
            Toast.makeText(context,"inventaire non terminé", Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(context, "inventaire terminé", Toast.LENGTH_LONG).show();
        }
    }


    public void addLines(@Nullable String date, @Nullable String CB) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Insérer le nouveau code-barres
        ContentValues cv = new ContentValues();
        cv.put(LINES_CODEBARRE, CB);
        cv.put(LINES_DOF, date);

        long result = db.insert(TABLE_LINES, null, cv);
        if (result == -1) {
            Toast.makeText(context, "Échec de l'ajout", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "DOF Ajouté", Toast.LENGTH_LONG).show();
        }
    }





    public void SynchronizeLines() throws ParseException {
        Log.d("Synchronisation", "appelé");
        Cursor cursor = readAllData();
        if (cursor == null) {
            Log.e("Synchronisation", "Erreur : le curseur est nul");
            return;
        }

        RequestQueue requestQueue = Volley.newRequestQueue(context);

        while (cursor.moveToNext()) {
            String cb = cursor.getString(0);
            String dof = cursor.getString(1);


            // Format de la date d'entrée
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy");
            // Format de la date de sortie
            SimpleDateFormat outputFormat = new SimpleDateFormat("MM/dd/yyyy");

            Date date = inputFormat.parse(dof);
            assert date != null;
            String formattedDate = outputFormat.format(date);


            Log.d("Contenu", formattedDate + " - " + cb);
            String url = "http://" + NetworkConfig.BASE_URL + ":5000/inventory";
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("DOF", formattedDate);
                jsonObject.put("CB", cb);
            } catch (JSONException e) {
                Log.e("Synchronisation", "Erreur JSON : " + e.getMessage());
                continue; // Passer à la ligne suivante en cas d'erreur
            }

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.POST, url, jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String message = response.getString("message");
                                Log.d("Synchronisation", "Réponse reçue : " + message);
                                if (message.equals("item ajouté a l'inventaire")){
                                    update(cb);
                                }
                            } catch (JSONException e) {
                                Log.e("Synchronisation", "Erreur de parsing de la réponse : " + e.getMessage());
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("Synchronisation", "Erreur de réponse : " + error.getMessage());
                            // Gérer l'erreur, par exemple en affichant un message à l'utilisateur
                        }
                    }
            );

            requestQueue.add(jsonObjectRequest);
        }

        if (!cursor.isClosed()) {
            cursor.close();
        }
    }



}
