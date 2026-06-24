package jas.school.app;


import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.androix.AbstractMainActivity;
import com.androix.NPersistence;
import com.androix.U;


/**
 * Purpose: main Android activity that initializes the framework, toolbar, and first module screen.
 */
public final class MainActivity extends AbstractMainActivity {
    // Menu feature note:
    // The toolbar button does not directly hold all module entries.
    // Instead, it opens a dedicated SearchUI-based screen (`MenuUI`)
    // so the menu stays full-screen and easy to extend later.


    public void init() {
        init(R.layout.activity_main);
        // Share the activity with the helper facade before actions start using dialogs or preferences.
        F.init(this);
        NPersistence.createDBIfNotExist("school.sql");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Intent intent = this.getIntent();
        //home = plese assign your home ui;
        U.checkAndRequestPermissions(MainActivity.this);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        AbstractMainActivity.setup("jas.school", this, R.id.fragmentContainer, InitSystemStartupServices.class, R.id.class);
        // To apply dark theme here and style.xml should be enabled Theme.AppCompat.DayNight
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        super.onCreate(savedInstanceState);
        init();
    }

    @Override
    public void onStart() {
        super.onStart();
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // `menu_main` belongs to MainActivity only.
        // SearchUI screens use `menu_search` through ModelSearchUI registration, not through this method.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_open_menu) {


           // navigate(MenuUI.class, "menu", true, getString(R.string.menu_title));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
