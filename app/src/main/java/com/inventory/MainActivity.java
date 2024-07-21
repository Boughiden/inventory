package com.inventory;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
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
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private EditText editTextDate, cb;
    private CardView card, newinv;
    ConstraintLayout constr;
    private int mYear, mMonth, mDay;
    private DataBase dbHelper;
    private BddDOF myDof;
    BddLines bdlines;
    ImageView back, settings, synch;
    Button valider, end;
    String dateinv;
    ImageButton add, scan;
    View tb;
    TextView  dateold;

    private List<String> barcodeList;
    private Runnable runnable;

    private Handler handler;



    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        synch = findViewById(R.id.synchmain);

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
            synch.setVisibility(View.GONE);
        } else {

            dateold = findViewById(R.id.Datemain);
            dateold.setText(dateinv);
            scan = findViewById(R.id.scanMain);
            scan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    scanCode();
                }
            });

            cb = findViewById(R.id.editCB);
            add = findViewById(R.id.addcb);

            Handler handler = new Handler();

            cb.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    // Pas besoin d'implémenter cette méthode
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    // Pas besoin d'implémenter cette méthode
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (runnable != null) {
                        handler.removeCallbacks(runnable);
                    }

                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            String text = editable.toString();
                            if (!text.isEmpty()) {
                                if (text.contains("\n")) { // Détecter plusieurs codes-barres
                                    String[] barcodes = text.split("\n");
                                    for (String barcode : barcodes) {
                                        if (!barcode.trim().isEmpty()) {
                                            barcodeList.add(barcode.trim());
                                        }
                                    }
                                    cb.setText("");  // Réinitialiser le champ après l'ajout
                                    // Ajouter les codes-barres à la base de données
                                    addBarcodesToDatabase();
                                } else { // Un seul code-barres
                                    bdlines.addLines(dateinv, text);
                                    cb.setText("");  // Réinitialiser le champ après l'ajout
                                }
                            }
                        }
                    };

                    handler.postDelayed(runnable, 500);
                }
            });

            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (cb.getText() != null && !cb.getText().toString().isEmpty()) {
                        bdlines.addLines(dateinv, String.valueOf(cb.getText()));
                        cb.setText("");  // Réinitialiser le champ après l'ajout
                    }
                }
            });



            end = findViewById(R.id.end);
            end.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("end", "cliecked");
                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage("Etes-vous sur de vouloir finir cet inventaire?")  // Message à afficher
                            .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    myDof.DOfEnd(dateinv);

                                    // Rafraîchir la page
                                    recreate();
                                }
                            })
                            .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            })
                            .show();
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

        EditText editTextDate = findViewById(R.id.editTextDate);
        editTextDate.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdating) {
                    return;
                }

                isUpdating = true;

                String input = s.toString();
                String cleaned = input.replaceAll("[^\\d]", "");
                StringBuilder formatted = new StringBuilder();

                int length = cleaned.length();
                for (int i = 0; i < length; i++) {
                    formatted.append(cleaned.charAt(i));
                    if ((i == 1 || i == 3) && i + 1 < length) {
                        formatted.append("/");
                    }
                }

                editTextDate.setText(formatted.toString());
                editTextDate.setSelection(formatted.length());

                isUpdating = false;
            }
        });




        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String formattedDate = dateFormat.format(new Date());
        editTextDate.setText(formattedDate);

        editTextDate.setOnTouchListener((v, event) -> {
            final int DRAWABLE_END = 2; // Right drawable index
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (editTextDate.getRight() - editTextDate.getCompoundDrawables()[DRAWABLE_END].getBounds().width())) {
                    // Click on the drawable
                    final Calendar c = Calendar.getInstance();
                    int mYear = c.get(Calendar.YEAR);
                    int mMonth = c.get(Calendar.MONTH);
                    int mDay = c.get(Calendar.DAY_OF_MONTH);

                    DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                            (view, year, monthOfYear, dayOfMonth) -> editTextDate.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year),
                            mYear, mMonth, mDay);
                    datePickerDialog.show();
                    return true;
                }
            }
            return false;
        });

        editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editTextDate.setText("");
            }
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


    // l'ajout a la bdd

    private void addBarcodesToDatabase() {
        if (!barcodeList.isEmpty()) {
            for (String barcode : barcodeList) {
                bdlines.addLines(dateinv, barcode);
            }
            barcodeList.clear();  // Vider la liste après l'ajout à la base de données
        }
    }

}