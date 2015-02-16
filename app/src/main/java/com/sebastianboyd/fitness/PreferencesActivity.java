package com.sebastianboyd.fitness;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;


public class PreferencesActivity extends Activity {

    EditText incomeEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        incomeEditText = (EditText) findViewById(R.id.editText);

        Context context = getApplicationContext();
        int income = SaveData.getMyIntPref(context, "income");
        incomeEditText.setText(Integer.toString(income));
    }

    @Override
    public void onBackPressed() {
        save();
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                save();
//                finish();
                return false;
        }
        return super.onOptionsItemSelected(item);
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
        SaveData.setMyIntPref(context, "income", income);
    }
}
