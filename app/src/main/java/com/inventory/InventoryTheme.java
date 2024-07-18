package com.inventory;

import android.app.Application;
import android.util.TypedValue;
import android.widget.Toolbar;

import com.google.android.material.color.DynamicColors;

public class InventoryTheme extends Application {
    @Override
    public void onCreate(){
        super.onCreate();
        DynamicColors.applyToActivitiesIfAvailable(this);

    }

}

