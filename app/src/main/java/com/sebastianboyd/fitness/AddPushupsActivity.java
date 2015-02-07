package com.sebastianboyd.fitness;


import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class AddPushupsActivity extends BaseActivity implements
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
    //    private View decrementButton;
    private View resetButton;
    private ViewGroup pausedControlLayout;
    private ViewGroup resumedControlLayout;

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
//        decrementButton = findViewById(R.id.button_decrement_pushups);
        resetButton = findViewById(R.id.button_reset_pushups);
        pausedControlLayout = (ViewGroup) findViewById(
                R.id.pushup_paused_control_layout);
        resumedControlLayout = (ViewGroup) findViewById(
                R.id.pushup_resumed_control_layout);
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
//            decrementButton.setVisibility(View.INVISIBLE);
            readyPrompt.setVisibility(View.VISIBLE);
            resetButton.setVisibility(View.INVISIBLE);

            paused = false;
            updatePauseState();
        } else {
            readyPrompt.setVisibility(View.INVISIBLE);
            resetButton.setVisibility(View.VISIBLE);
//            decrementButton.setVisibility(View.VISIBLE);
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
            pausedControlLayout.setVisibility(View.VISIBLE);
            resumedControlLayout.setVisibility(View.GONE);
            newBG = getResources().getDrawable(
                    R.drawable.bg_button_round_darkgrey);
        } else {
            resumedControlLayout.setVisibility(View.VISIBLE);
            pausedControlLayout.setVisibility(View.GONE);
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

    public void resetCount(View view) {
        // FUTURE: animate this action
        pushups = 0;
        updateExerciseCount();
    }

    /**
     * Commit the pushup count and associated metadata to the server.
     *
     * @param view
     *         The view calling this method.
     */
    public void sendData(View view) {
        // TODO: Implement API call to Google Fit (this is you, Sebastian)
        NavUtils.navigateUpFromSameTask(this);
    }
}
