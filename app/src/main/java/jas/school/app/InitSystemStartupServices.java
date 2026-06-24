package jas.school.app;


import android.content.Intent;

import com.androix.AbstractInitStartup;
import com.androix.AbstractMainActivity;

import jas.school.app.R;

/**
 * Purpose: bootstrap the MDD framework when the app is started by a service or other background entry point.
 */
public class InitSystemStartupServices extends AbstractInitStartup {

    @Override
    public void setup(Intent intent) {
        // Mirrors MainActivity setup so background startup can register the same app modules.
        AbstractMainActivity.setup("jas.school",1, R.id.fragmentContainer,InitSystemStartupServices.class, R.id.class,context,Registration.class);
    }
}
