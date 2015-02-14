package com.sebastianboyd.fitness;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.Transition;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class AddPushupsActivity extends BaseActivity implements
                                                     SensorEventListener {
    public final static String EXTRA_MESSAGE = "com.sebastianboyd" +
                                               ".fitness.MESSAGE";
    static final String STATE_PUSHUPS = "pushup_count";
    static final String STATE_PAUSED = "pushup_count_paused";
    private long pushups = 0;
    private long startTime = 0;
    private long endTime = 0;
    private boolean paused = false;
    private float range;
    static final int activity = 80; // TODO make static final (this is strength
    // training)

    private SensorManager mSensorManager;
    private Sensor mSensor;

    private View readyPrompt, resetButton;
    private Button counterCircle;
    private ViewGroup pausedControlLayout, resumedControlLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            pushups = savedInstanceState.getLong(STATE_PUSHUPS);
            paused = savedInstanceState.getBoolean(STATE_PAUSED);
        }
        setContentView(R.layout.activity_add_pushups);

        counterCircle = (Button) findViewById(R.id.pushups_counter_circle);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        range = mSensor.getMaximumRange();
        readyPrompt = findViewById(R.id.pushup_counter_ready_prompt);
        resetButton = findViewById(R.id.button_reset_pushups);
        pausedControlLayout = (ViewGroup) findViewById(
                R.id.pushup_paused_control_layout);
        resumedControlLayout = (ViewGroup) findViewById(
                R.id.pushup_resumed_control_layout);

        if (Build.VERSION.SDK_INT >= 21) {
            counterCircle.setTransitionName(
                    getResources()
                            .getString(R.string.transition_pushup_circle));
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
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensor,
                                        SensorManager.SENSOR_DELAY_NORMAL);
        updatePauseState();
        // TODO: this is an extension of the hacky configureTransition
//        if (Build.VERSION.SDK_INT < 21) {
//            updateExerciseCount();
//        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putLong(STATE_PUSHUPS, pushups);
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

        DialogInterface.OnClickListener negativeListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                };

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_discard_title)
                .setMessage(R.string.dialog_discard_message)
                .setPositiveButton(R.string.dialog_discard_accept,
                                   new DialogInterface.OnClickListener() {
                                       @Override
                                       public void onClick(
                                               DialogInterface dialog,
                                               int which) {
                                           positiveCallback.fire();
                                       }
                                   })
                .setNegativeButton(R.string.dialog_discard_cancel,
                                   negativeListener)
                .setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public boolean hasUnsavedData() {
        return pushups > 0;
    }

    @Override
    public void onBackPressed() {
        Callback onDiscard = new Callback() {
            @Override
            public void fire() {
                discard();
                // Just to make the animation pretty.
                counterCircle.setText("");
                AddPushupsActivity.super.onBackPressed();
            }
        };
        promptDiscard(onDiscard);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float in = event.values[0];
        if (in < range && !paused) {
            pushups++;
            if (pushups == 1){
                startTime = java.lang.System.currentTimeMillis() + 3;
            }
            endTime = java.lang.System.currentTimeMillis();
            updateExerciseCount();
        }
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
        // Maybe a circular reveal.
        pushups = 0;
        updateExerciseCount();
    }

    /**
     * Commit the pushup count and associated metadata to the server.
     *
     * @param view
     *         A reference to the okay button calling this method.
     */
    public void sendData(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        if (pushups > 0) {
            long[] intentData = new long[3];
            intentData[0] = activity;
            intentData[1] = startTime;
            intentData[2] = endTime;
            intent.putExtra(EXTRA_MESSAGE, intentData);
        }
        startActivity(intent);
    }

    public static abstract class Callback {
        public abstract void fire();
    }
}
