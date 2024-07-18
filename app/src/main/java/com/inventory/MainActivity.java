package com.inventory;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import com.inventory.Bdd.BddDOF;
import com.inventory.Bdd.BddLines;
import com.inventory.Bdd.DataBase;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private EditText editTextDate;
    private CardView card, newinv;
    ConstraintLayout constr;
    private int mYear, mMonth, mDay;
    private DataBase dbHelper;
    private BddDOF myDof;
    BddLines bdlines;
    ImageView back, settings, synch;
    Button valider, scan;
    String dateinv;
    View tb;
    TextView  dateold;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Récupérer la couleur primaryColor du thème de l'application
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
        int colorPrimary = typedValue.data;

        tb = findViewById(R.id.include);

        tb.setBackgroundColor(colorPrimary);




        myDof = new BddDOF(this);
        bdlines = new BddLines(this);

        Cursor cursor1 = myDof.getIP();
        if (cursor1.moveToFirst()){
            NetworkConfig.BASE_URL = cursor1.getString(0);
            Log.d("IP", NetworkConfig.BASE_URL);
        }

        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        card = findViewById(R.id.OldInv);
        Cursor cursor = BddDOF.readAllData();
        if (cursor.moveToFirst()) {
            dateinv = cursor.getString(0);
            Log.d("dateinv", dateinv);
        }
        if (cursor == null || cursor.getCount() == 0) {
            card.setVisibility(View.GONE);
        } else {

            dateold = findViewById(R.id.Datemain);
            dateold.setText(dateinv);
            scan = findViewById(R.id.scnaMain);
            scan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    scanCode();
                }
            });
//            card.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                        Intent intent = new Intent(MainActivity.this, Scanning.class);
//                        intent.putExtra("DateInv", dateinv);
//                        startActivity(intent);
//                    }
//            });
        }




        editTextDate = findViewById(R.id.editTextDate);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String formattedDate = dateFormat.format(new Date());
        editTextDate.setText(formattedDate);

        editTextDate.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                    (view, year, monthOfYear, dayOfMonth) -> editTextDate.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year),
                    mYear, mMonth, mDay);
            datePickerDialog.show();
        });

        String date = String.valueOf(editTextDate.getText());
        Log.d("date", date);


        newinv = findViewById(R.id.newInv);
        constr = findViewById(R.id.constr);
        newinv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (constr.getVisibility()==View.GONE){
                    constr.setVisibility(View.VISIBLE);
                }else{constr.setVisibility(View.GONE);}

            }
        });
        valider = findViewById(R.id.ValiderInv);
        valider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dateinv != null) {
                    // Créez une boîte de dialogue
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Lancer un nouvel inventaire ")  // Titre de la boîte de dialogue

                            .setMessage("En lançant cet inventaire vous mettez fin immediatement a l'inventaire actuel. \n Etes-vous sur de vouloir lancer ce nouvel inventaire?")  // Message à afficher
                            .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    long up = myDof.DOfEnd(dateinv);
                                    if (up == -1){
                                        Toast.makeText(MainActivity.this, "Inventaire non terminé pas possible de lancer un nouvel inventair", Toast.LENGTH_LONG).show();
                                    }else{
                                        Toast.makeText(MainActivity.this, "Inventaire terminé un nouvel a été lancé", Toast.LENGTH_LONG).show();
                                        String date = String.valueOf(editTextDate.getText());
                                        Log.d("date", date);
                                        myDof.addDof(date);
                                        // Rafraîchir la page
                                        recreate();
                                    }
                                }
                            })
                            .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Code à exécuter lorsque l'utilisateur appuie sur le bouton Annuler
                                }
                            })
                            .show();
                }else{
                    String date = String.valueOf(editTextDate.getText());
                    Log.d("date", date);
                    myDof.addDof(date);
                    // Rafraîchir la page
                    recreate();
                }
            }
        });


        settings = findViewById(R.id.settings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showIPDialog();
            }
        });



        synch = findViewById(R.id.synchmain);
        synch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    bdlines.SynchronizeLines();
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                Log.d("synch", "clicked");
            }
        });

    }


    private void showIPDialog() {
        DialogFragment dialog = new alertDialogIP();
        dialog.show(getSupportFragmentManager(), "AlertDialogIP");
    }




    private void scanCode() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLauncher.launch(options);


    }
    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null) {
            String CB = result.getContents();
            bdlines.addLines(dateinv, CB);
            // Relaunch the scanner to keep scanning
            scanCode();
        }
    });

}