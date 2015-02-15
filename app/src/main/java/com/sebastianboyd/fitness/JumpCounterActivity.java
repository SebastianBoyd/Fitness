package com.sebastianboyd.fitness;


import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.os.Bundle;
import android.view.View;


public final class JumpCounterActivity extends CounterActivity {

    private static final int EXERCISE_ID = 9; // Aerobics
    private static final int SHAKE_THRESHOLD = 600;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sensor =
                sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
    }

    @Override
    public void updatePauseState() {
        updatePauseState(R.drawable.bg_button_round_jumps,
                         R.drawable.shape_oval_jumps);
    }

    @Override
    public void sendData(View view) {
        Intent intent = buildDataSenderIntent(EXERCISE_ID);
        startActivity(intent);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;
        if (mySensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            long curTime = System.currentTimeMillis();

            if ((curTime - lastUpdate) > 150) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float speed = Math.abs(x + y + z - last_x - last_y - last_z) /
                              diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    exerciseCount = exerciseCount + 0.5;
                    if (exerciseCount == 1) {
                        startTime = java.lang.System.currentTimeMillis();
                    }
                    endTime = java.lang.System.currentTimeMillis();
                    updateExerciseCount();
                }

                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
