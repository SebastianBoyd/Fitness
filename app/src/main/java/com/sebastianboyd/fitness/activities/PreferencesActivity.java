package com.sebastianboyd.fitness.activities;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;

import com.sebastianboyd.fitness.PrefCache;
import com.sebastianboyd.fitness.R;


public final class PreferencesActivity extends BaseActivity {

    EditText incomeEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        incomeEditText = (EditText) findViewById(R.id.editText);

        Context context = getApplicationContext();
        int income = PrefCache.getIntPref(context, PrefCache.INCOME);
        incomeEditText.setText(Integer.toString(income));
    }

    @Override
    public void onBackPressed() {
        save();

        // HACK ALERT!
        // Zander and Sebastian TODO understand back behavior and unhack
//        super.onBackPressed();
        NavUtils.navigateUpFromSameTask(this);
    }

    private void save() {
        String incomeString = incomeEditText.getText().toString();
        int income;
        try {
            income = Integer.parseInt(incomeString);
        } catch (NumberFormatException e) {
            Log.e("Preferences", "Could not save preferences, income invalid");
            return;
        }
        Context context = getApplicationContext();
        PrefCache.setIntPref(context, PrefCache.INCOME, income);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                save();
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
