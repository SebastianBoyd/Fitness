package com.sebastianboyd.fitness;


import android.app.AlertDialog;
import android.content.Context;
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
        Context context = getApplicationContext();
        if (SaveData.getIntPref(context, String.valueOf(EXERCISE_ID) + "intro")
            == 0) {
            intro();
        }
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

    public void intro(){
        //TODO make abstract and not hack
        Context context = getApplicationContext();
        SaveData.setIntPref(context,
                            String.valueOf(EXERCISE_ID) + "intro",
                            1);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string
                                                           .dialog_intro_pushups_title))
                .setMessage(getResources().getString(R.string
                                                             .dialog_intro_pushups_message))
                .setPositiveButton(getResources().getString(R.string.dialog_intro_accept), null)
                .setCancelable(false)
                .create();

        AlertDialog saveDataDialog = new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.dialog_intro_save_title))
                .setMessage(getResources().getString(R.string.dialog_intro_save_message))
                .setPositiveButton(getResources().getString(R.string.dialog_intro_accept),
                                   null)
                .setCancelable(false)
                .create();
        saveDataDialog.show();
        dialog.show();

    }
}
