package com.sebastianboyd.fitness;


import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.os.Bundle;
import android.view.View;


public final class PushupCounterActivity extends CounterActivity {

    private static final int EXERCISE_ID = 80; // This is strength training

    private float range;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        range = sensor.getMaximumRange();
    }

    @Override
    public void updatePauseState() {
        updatePauseState(R.drawable.bg_button_round_pushups,
                         R.drawable.shape_oval_pushups);
    }

    @Override
    public void sendData(View view) {
        Intent intent = buildDataSenderIntent(EXERCISE_ID);
        startActivity(intent);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float in = event.values[0];
        if (in < range && !paused) {
            exerciseCount++;
            if (exerciseCount == 1) {
                startTime = java.lang.System.currentTimeMillis();
            }
            endTime = java.lang.System.currentTimeMillis();
            updateExerciseCount();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
