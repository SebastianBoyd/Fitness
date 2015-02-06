package com.sebastianboyd.fitness;


import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class AddPushupsActivity extends ActionBarActivity implements
                                                          SensorEventListener {
    static final String STATE_PUSHUPS = "pushup_count";
    static final String STATE_PAUSED = "pushup_count_paused";

    private int pushups = 0;
    private boolean paused = false;
    private float range;

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private Button counterCircle;
    private View readyPrompt;
    private View decrementButton;
    private ViewGroup adjusterLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            pushups = savedInstanceState.getInt(STATE_PUSHUPS);
            paused = savedInstanceState.getBoolean(STATE_PAUSED);
        }
        setContentView(R.layout.activity_pushup_counter);

        counterCircle = (Button) findViewById(R.id.pushups_counter_circle);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        range = mSensor.getMaximumRange();
        readyPrompt = findViewById(R.id.pushup_counter_ready_prompt);
        decrementButton = findViewById(R.id.button_decrement_pushups);
        adjusterLayout = (ViewGroup) findViewById(R.id.pushup_adjuster_layout);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensor,
                                        SensorManager.SENSOR_DELAY_NORMAL);
        updateExerciseCount();
        updatePauseState();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(STATE_PUSHUPS, pushups);
        savedInstanceState.putBoolean(STATE_PAUSED, paused);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Ensure the pushup count is reflected in the activity's views.
     */
    public void updateExerciseCount() {
        if (pushups < 0) pushups = 0;
        String out = String.valueOf(pushups);
        counterCircle.setText(out);

        // FUTURE: animate hide/show
        // Especially hide, since they will most likely be using the sensor to
        // add pushups, so they won't see the animation
        if (pushups == 0) {
            decrementButton.setVisibility(View.INVISIBLE);
            readyPrompt.setVisibility(View.VISIBLE);
        } else {
            readyPrompt.setVisibility(View.INVISIBLE);
            decrementButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Ensure everything matches the pause state.
     */
    public void updatePauseState() {
        // FUTURE: show time data between first and last pushup on pause
        // Should only count pushups detected by the sensor.
        // FUTURE: animate all of the transitions
        Drawable newBG;
        if (paused) {
            adjusterLayout.setVisibility(View.GONE);
            newBG = getResources().getDrawable(
                    R.drawable.bg_button_round_darkgrey);
        } else {
            adjusterLayout.setVisibility(View.VISIBLE);
            if (pushups == 0) {
                newBG = getResources()
                        .getDrawable(R.drawable.shape_oval_pushups);
            } else {
                newBG = getResources().getDrawable(
                        R.drawable.bg_button_round_pushups);
            }
        }
        counterCircle.setBackground(newBG);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float in = event.values[0];
        if (in < range && !paused) pushups++;
        updateExerciseCount();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void addPushup(View view) {
        pushups++;
        updateExerciseCount();
    }

    public void removePushup(View view) {
        pushups--;
        updateExerciseCount();
    }

    public void togglePause(View view) {
        // Cannot be paused if pushups == 0
        paused = pushups > 0 && !paused;
        updatePauseState();
    }
}
