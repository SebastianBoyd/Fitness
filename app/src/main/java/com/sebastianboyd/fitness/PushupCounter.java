package com.sebastianboyd.fitness;

import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.hardware.Sensor;
import android.view.View;
import android.widget.TextView;


public class PushupCounter extends ActionBarActivity implements SensorEventListener{
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private int pushups = 0;
    private float range;
    private TextView currentPushups;
    static final String STATE_PUSHUPS = "player_score";

    private View readyPrompt;
    private View decrementButton;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            pushups = savedInstanceState.getInt(STATE_PUSHUPS);
        }
        setContentView(R.layout.activity_pushup_counter);
        currentPushups = new TextView(this);
        currentPushups = (TextView)findViewById(R.id.pushupsText);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        range = mSensor.getMaximumRange();

        readyPrompt = findViewById(R.id.pushup_counter_ready_prompt);
        decrementButton = findViewById(R.id.button_decrement_pushups);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        updatePushups();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(STATE_PUSHUPS, pushups);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float in = event.values[0];
        if (in < range) pushups++;
        updatePushups();
    }

    public void updatePushups(){
        if (pushups < 0) pushups = 0;
        String out = String.valueOf(pushups);
        currentPushups.setText(out);

        // FUTURE: eventually, animate hide/show
        // However, it can wait, since they will probably not be able to see
        // the animation if they are using the sensor to add pushups.
        if (pushups == 0) {
            decrementButton.setVisibility(View.INVISIBLE);
            readyPrompt.setVisibility(View.VISIBLE);
        } else {
            readyPrompt.setVisibility(View.INVISIBLE);
            decrementButton.setVisibility(View.VISIBLE);
        }
    }

    public void addPushup(View view) {
        pushups++;
        updatePushups();
    }

    public void removePushup(View view) {
        pushups--;
        updatePushups();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
