package com.sebastianboyd.fitness;

import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.hardware.Sensor;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


public class PushupCounter extends ActionBarActivity implements SensorEventListener{
    private final SensorManager mSensorManager;
    private Sensor mProximity;
    private TextView currentPushups;

    public PushupCounter(){
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mProximity = mProximity.getDefaultSensor(Sensor.TYPE_PROXIMITY);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pushup_counter, menu);
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pushup_counter);
        currentPushups = new TextView(this);
        currentPushups = (TextView)findViewById(R.id.pushupsText);


    }
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Toast.makeText(getApplicationContext(), "ok", Toast.LENGTH_SHORT);
        float a = event.values[0];
        String out = String.valueOf(a);
        currentPushups.setText(out);


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
