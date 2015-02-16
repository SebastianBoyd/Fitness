package com.sebastianboyd.fitness;


// Zander TODO add animations
// Zander TODO show time in pause screen
// Sebastian and/or Zander TODO get their average time, and compare it


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.Transition;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


/**
 * Base class for counter activities that use sensors.
 */
public abstract class CounterActivity extends BaseActivity implements
                                                           SensorEventListener {
    public final static String EXTRA_MESSAGE = "com.sebastianboyd" +
                                               ".fitness.MESSAGE";
    static final String STATE_EXERCISE_COUNT = "exercise_count";
    static final String STATE_PAUSED = "counter_paused";

    // TODO make reference to final field in api
//    protected abstract static final int EXERCISE_ID; // TODO make this work
    // I would like to make sendData() automatically inherited, but fields
    // aren't allowed to be abstract or overwritten for superclass methods.

    protected SensorManager sensorManager;
    protected Sensor sensor;

    protected boolean paused = false;
    protected double exerciseCount = 0;
    protected long startTime = 0;
    protected long endTime = 0;

    protected View readyPrompt, resetButton;
    protected Button counterCircle;
    protected ViewGroup pausedControlLayout, resumedControlLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            exerciseCount = savedInstanceState.getDouble(STATE_EXERCISE_COUNT);
            paused = savedInstanceState.getBoolean(STATE_PAUSED);
        }
        setContentView(R.layout.activity_counter);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        counterCircle = (Button) findViewById(R.id.counter_circle);
        readyPrompt = findViewById(R.id.counter_ready_prompt);
        resetButton = findViewById(R.id.button_reset);
        pausedControlLayout = (ViewGroup) findViewById(
                R.id.paused_control_layout);
        resumedControlLayout = (ViewGroup) findViewById(
                R.id.resumed_control_layout);

        if (Build.VERSION.SDK_INT >= 21) {
            counterCircle.setTransitionName(
                    getResources()
                            .getString(R.string.transition_id_counter_circle));
        }

        configureTransitions();

        // FUTURE: make this approach work
        // Makes action bar and status bar fade in activity transition
//        if (Build.VERSION.SDK_INT >= 21) {
//            // Postpone the transition until the window's decor view has
//            // finished its layout.
//            postponeEnterTransition();
//            final ViewTreeObserver observer =
//                    getWindow().getDecorView().getViewTreeObserver();
//            observer.addOnPreDrawListener(
//                    new ViewTreeObserver.OnPreDrawListener() {
//                        @Override
//                        public boolean onPreDraw() {
//                            observer.removeOnPreDrawListener(this);
//                            startPostponedEnterTransition();
//                            return true;
//                        }
//                    });
//        }
    }

    /**
     * Hide pushup count until animation is complete.
     */
    @Override
    protected void configureTransitions() {
        // TODO: this is hacky, Zander will fix
        super.configureTransitions();
        if (Build.VERSION.SDK_INT >= 21) {
            Transition fade = new Fade();
            fade.excludeTarget(android.R.id.navigationBarBackground, true);
            fade.addListener(new Transition.TransitionListener() {
                @Override
                public void onTransitionStart(Transition transition) {
                }

                @Override
                public void onTransitionEnd(Transition transition) {
                    updateExerciseCount();
                }

                @Override
                public void onTransitionCancel(Transition transition) {
                }

                @Override
                public void onTransitionPause(Transition transition) {
                }

                @Override
                public void onTransitionResume(Transition transition) {
                }
            });

            getWindow().setEnterTransition(fade);
        } else {
            updateExerciseCount();
        }
    }

    /**
     * Ensure the exercise count is reflected in the activity's views.
     */
    public void updateExerciseCount() {
        if (exerciseCount < 0) exerciseCount = 0;
        String out = String.valueOf((int) exerciseCount);
        counterCircle.setText(out);

        // FUTURE: animate hide/show
        // Especially hide, since they will most likely be using the sensor to
        // add exerciseCount, so they won't see the animation
        if ((int) exerciseCount == 0) {
            readyPrompt.setVisibility(View.VISIBLE);
            resetButton.setVisibility(View.INVISIBLE);

            paused = false;
            updatePauseState();
        } else {
            readyPrompt.setVisibility(View.INVISIBLE);
            resetButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Ensure everything matches the pause state.
     */
    public abstract void updatePauseState();

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensor,
                                       SensorManager.SENSOR_DELAY_NORMAL);
        updatePauseState();
        // TODO: this is an extension of the hacky configureTransition
//        if (Build.VERSION.SDK_INT < 21) {
//            updateExerciseCount();
//        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putDouble(STATE_EXERCISE_COUNT, exerciseCount);
        savedInstanceState.putBoolean(STATE_PAUSED, paused);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                promptDiscard();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void promptDiscard() {
        Callback positiveCallback = new Callback() {
            @Override
            public void fire() {
                discard();
                finish();
            }
        };
        promptDiscard(positiveCallback);
    }

    /**
     * Dump data as necessary.
     */
    public void discard() {
        // TODO: (on release) review this
        // Should it be removed, or should we keep it just in case we ever
        // need it?
        // May be unnecessary, the activity may clear all of our data for us.
        // I don't know, so this is up to you, Sebastian.
    }

    public void promptDiscard(final Callback positiveCallback) {
        if (!hasUnsavedData()) {
            positiveCallback.fire();
            return;
        }

        DialogInterface.OnClickListener positiveListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        positiveCallback.fire();
                    }
                };
        DialogInterface.OnClickListener negativeListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                };

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_discard_title)
                .setMessage(R.string.dialog_discard_message)
                .setPositiveButton(R.string.dialog_discard_accept,
                                   positiveListener)
                .setNegativeButton(R.string.dialog_discard_cancel,
                                   negativeListener)
                .setCancelable(true)
                .create();
        dialog.show();
    }

    public boolean hasUnsavedData() {
        return (int) exerciseCount > 0;
    }

    @Override
    public void onBackPressed() {
        Callback onDiscard = new Callback() {
            @Override
            public void fire() {
                discard();
                // Just to make the animation pretty.
                counterCircle.setText("");
                CounterActivity.super.onBackPressed();
            }
        };
        promptDiscard(onDiscard);
    }

    protected void updatePauseState(int counterCircleActiveBG,
                                    int counterCircleUnclickableBG) {
        // FUTURE: show time data between first and last pushup on pause
        // Should only count exerciseCount detected by the sensor.
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
            if ((int) exerciseCount == 0) {
                newBG = getResources().getDrawable(counterCircleUnclickableBG);
            } else {
                newBG = getResources().getDrawable(counterCircleActiveBG);
            }
        }
        counterCircle.setBackground(newBG);
    }

    protected Intent buildDataSenderIntent(int exerciseID) {
        Context context = getApplicationContext();
        // Save total exercise count
        int previousTotalCount = SaveData.getIntPref(
                context, String.valueOf(exerciseID) + SaveData.TOTAL_COUNT);
        SaveData.setIntPref(context,
                            String.valueOf(exerciseID) + SaveData.TOTAL_COUNT,
                            previousTotalCount + (int) exerciseCount);
        // Save total time
        long thisTime = endTime - startTime;
        int previousTotalTime = SaveData.getIntPref(
                context, String.valueOf(exerciseID) + SaveData.TOTAL_TIME);
        SaveData.setIntPref(context,
                            String.valueOf(exerciseID) + SaveData.TOTAL_TIME,
                            previousTotalTime + (int) thisTime);
        // Zander TODO implement this and display to user
        Log.v("Stats", String.valueOf(SaveData.getIntPref(
                context, String.valueOf(exerciseID) + "totalCount")));
        Log.v("Stats", String.valueOf(SaveData.getIntPref(
                context, String.valueOf(exerciseID) + "totalTime")));

        Intent intent = new Intent(this, MainActivity.class);
        if (exerciseCount > 0) {
            long[] intentData = new long[3];
            intentData[0] = exerciseID;
            intentData[1] = startTime;
            intentData[2] = endTime;
            intent.putExtra(EXTRA_MESSAGE, intentData);
        }
        return intent;
    }

    public void incrementCount(View view) {
        exerciseCount++;
        updateExerciseCount();
    }

    public void decrementCount(View view) {
        exerciseCount--;
        updateExerciseCount();
    }

    public void togglePause(View view) {
        // Cannot be paused if exerciseCount == 0
        paused = exerciseCount > 0 && !paused;
        updatePauseState();
    }

    public void resetCount(View view) {
        // FUTURE: animate this action
        // Maybe a circular reveal.
        exerciseCount = 0;
        updateExerciseCount();
    }

    /**
     * Commit the exercise count and associated metadata to the server.
     *
     * @param view
     *         A reference to the okay button calling this method.
     */
    public void sendData(View view) {
    }
}
