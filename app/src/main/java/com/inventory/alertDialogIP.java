package com.inventory;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.inventory.Bdd.BddDOF;

public class alertDialogIP extends DialogFragment {

    private EditText ipAddressEditText;
    private BddDOF dof;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        dof = new BddDOF(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_adress_ip, null);

        ipAddressEditText = view.findViewById(R.id.editTextIpAddress);
        Cursor cursor = dof.getIP();
        if (cursor.moveToFirst()){
            ipAddressEditText.setText(cursor.getString(0));
        }


        builder.setView(view)
                .setTitle("Insérez l'@IP ")
                .setPositiveButton("Enregistrer", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        NetworkConfig.BASE_URL = ipAddressEditText.getText().toString();
                        dof.addIP(NetworkConfig.BASE_URL);
                        // Handle saving the IP address here
                        Toast.makeText(getActivity(), "@IP enregistrée: " + NetworkConfig.BASE_URL, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });

        return builder.create();
    }
}
