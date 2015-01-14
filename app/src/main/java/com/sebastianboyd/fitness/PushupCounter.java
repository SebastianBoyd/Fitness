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


public class PushupCounter extends ActionBarActivity implements SensorEventListener{
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private int Pushups = 0;
    private float range;
    private TextView currentPushups;
    static final String STATE_PUSHUPS = "player_score";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            Pushups = savedInstanceState.getInt(STATE_PUSHUPS);
        }
        setContentView(R.layout.activity_pushup_counter);
        currentPushups = new TextView(this);
        currentPushups = (TextView)findViewById(R.id.pushupsText);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        range = mSensor.getMaximumRange();


    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(STATE_PUSHUPS, Pushups);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float in = event.values[0];
        if (in < range) Pushups++;
        String out = String.valueOf(Pushups);

        currentPushups.setText(out);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
