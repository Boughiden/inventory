package com.inventory;

import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.DialogFragment;

import com.inventory.Bdd.BddDOF;
import com.inventory.Bdd.BddLines;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.text.ParseException;
import java.util.Objects;

public class Scanning extends AppCompatActivity {

    private TextView dateInv;

    private BddLines bddL;
    private BddDOF bddDOF;
    private Button startInv;

    private String date;
    ImageView back, synch, settings;
    View tb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanning);


        // Récupérer la couleur primaryColor du thème de l'application
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
        int colorPrimary = typedValue.data;

        tb = findViewById(R.id.include1);

        tb.setBackgroundColor(colorPrimary);

        if (!Objects.equals(NetworkConfig.BASE_URL, "")){
            Log.d("IP", NetworkConfig.BASE_URL);
        }


        bddDOF = new BddDOF(Scanning.this);
        Cursor cursor1 = BddDOF.readAllData();
        if (cursor1.moveToFirst()) {
            date = cursor1.getString(0);
        }
        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //date = getIntent().getStringExtra("DateInv");

        assert date != null;
        Log.d("Date", date);

        dateInv = findViewById(R.id.dateInv);
        dateInv.setText(date);


        startInv = findViewById(R.id.startScanning);


        bddL = new BddLines(Scanning.this);

        Cursor cursor = BddLines.readAllData();
        if (cursor != null || cursor.getCount() != 0){
            startInv.setText("Continuer l'inventaire");
        }

        startInv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            scanCode();
            }
        });

        synch = findViewById(R.id.synch);
        synch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    bddL.SynchronizeLines();
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                Log.d("synch", "clicked");
            }
        });

        settings = findViewById(R.id.settings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showIPDialog();
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
            bddL.addLines(date, CB);
            // Relaunch the scanner to keep scanning
            scanCode();
        }
    });

}