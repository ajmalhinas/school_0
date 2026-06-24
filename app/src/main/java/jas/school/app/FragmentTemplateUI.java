package jas.school.app;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.androix.Fragmentx;

import jas.school.app.R;

/**
 * Purpose: starter fragment template kept as a copy source for new screens in this project.
 */
public class FragmentTemplateUI extends Fragmentx {
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // This stays intentionally minimal so new UIs can be cloned from it quickly.
        return inflater.inflate(R.layout.fragmenttemplate_ui, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
