package com.sebastianboyd.fitness.activities;


import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;
import com.sebastianboyd.fitness.PrefCache;
import com.sebastianboyd.fitness.R;
import com.sebastianboyd.fitness.fragments.StatViewFragment;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

// Zander TODO support landscape by using fragments
// Zander TODO rework activity transtions


public final class MainActivity extends BaseActivity {

    private static final int REQUEST_OAUTH = 1;
    /**
     * Track whether an authorization activity is stacking over the current
     * activity, i.e. when a known auth error is being resolved, such as
     * showing the account chooser or presenting a consent dialog. This avoids
     * common duplications as might happen on screen rotations, etc.
     */
    private static final String AUTH_PENDING = "auth_state_pending";
    private static final String TAG = "Fit Auth";
    private static final String DATE_FORMAT = "yyyy.MM.dd HH:mm:ss";
    private long[] inputData = null;
    private boolean authInProgress = false;
    private GoogleApiClient apiClient = null;

    /**
     * Annual income in USD.
     * <p/>
     * Defaults to $60,000.
     */
    private int salary = 60000;

    private StatViewFragment statViewFragment;

    /**
     * Amount of bonus life gained through exercise in milliseconds.
     */
    private long lifeGained = 0;
    private double moneyEarned = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        inputData =
                intent.getLongArrayExtra(PushupCounterActivity.EXTRA_MESSAGE);
        Log.v(TAG, Arrays.toString(inputData));
        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }

        statViewFragment = (StatViewFragment) getFragmentManager()
                .findFragmentById(R.id.stat_view);

        buildFitnessClient();

        if (Build.VERSION.SDK_INT >= 21) {
            findViewById(R.id.pushup_button).setTransitionName(
                    getResources()
                            .getString(R.string.transition_id_counter_circle));
        }
        configureTransitions();
        Context context = getApplicationContext();
        int income = PrefCache.getIntPref(context, PrefCache.INCOME);
        if (income != 0) {
            salary = income;
        } else {
            PrefCache.setIntPref(context, PrefCache.INCOME, salary);
        }
        Log.v("Data", String.valueOf(salary));
    }

    /**
     * Build a {@link GoogleApiClient}.
     * <p/>
     * It will authenticate the user and allow the application to connect to
     * Fitness APIs. The scopes included should match the scopes your app needs
     * (see documentation for details). Authentication will occasionally fail
     * intentionally, and in those cases, there will be a known resolution,
     * which the OnConnectionFailedListener() can address. Examples of this
     * include the user never having signed in before, or having multiple
     * accounts on the device and needing to specify which account to use, etc.
     */
    private void buildFitnessClient() {
        // Create the Google API Client
        apiClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.API)
                .addScope(new Scope(Scopes.FITNESS_BODY_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addConnectionCallbacks(
                        new GoogleApiClient.ConnectionCallbacks() {

                            @Override
                            public void onConnected(Bundle bundle) {
                                Log.i(TAG, "Connected!!!");
                                // Now you can make calls to the Fitness APIs.
                                // Put application specific code here.
                                new InsertAndVerifyDataTask().execute();
                            }

                            @Override
                            public void onConnectionSuspended(int i) {
                                // If your connection to the sensor gets lost at some point,
                                // you'll be able to determine the reason and react to it here.
                                if (i ==
                                    GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                                    Log.i(TAG,
                                          "Connection lost.  Cause: Network Lost.");
                                } else if (i ==
                                           GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                                    Log.i(TAG,
                                          "Connection lost.  Reason: Service Disconnected");
                                }
                            }
                        }
                )

                .addOnConnectionFailedListener(
                        new GoogleApiClient.OnConnectionFailedListener() {
                            // Called whenever the API client fails to connect.
                            @Override
                            public void onConnectionFailed(
                                    ConnectionResult result) {
                                Log.i(TAG, "Connection failed. Cause: " +
                                           result.toString());
                                if (!result.hasResolution()) {
                                    // Show the localized error dialog
                                    GooglePlayServicesUtil.getErrorDialog(
                                            result.getErrorCode(),
                                            MainActivity.this, 0).show();
                                    return;
                                }
                                // The failure has a resolution. Resolve it.
                                // Called typically when the app is not yet authorized, and an
                                // authorization dialog is displayed to the user.
                                if (!authInProgress) {
                                    try {
                                        Log.i(TAG,
                                              "Attempting to resolve failed connection");
                                        authInProgress = true;
                                        result.startResolutionForResult(
                                                MainActivity.this,
                                                REQUEST_OAUTH);
                                    } catch (IntentSender.SendIntentException e) {
                                        Log.e(TAG,
                                              "Exception while starting resolution activity",
                                              e);
                                    }
                                }
                            }
                        }
                ).build();
    }

    private DataReadRequest getData() {
        // Setting a start and end date using a range of 1 week before this moment.
        int days = getStatTimeScope();
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long startTime = 1;
        long endTime = cal.getTimeInMillis();
        if (days > 0) {
            cal.add(Calendar.DAY_OF_YEAR, -days);
            startTime = cal.getTimeInMillis();
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        Log.i(TAG, "Range Start: " + dateFormat.format(startTime));
        Log.i(TAG, "Range End: " + dateFormat.format(endTime));

        return new DataReadRequest.Builder()
                // The data request can specify multiple data types to return, effectively
                // combining multiple data queries into one call.
                // In this example, it's very unlikely that the request is for several hundred
                // datapoints each consisting of a few steps and a timestamp.  The more likely
                // scenario is wanting to see how many steps were walked per day, for 7 days.
                .aggregate(DataType.TYPE_ACTIVITY_SEGMENT,
                           DataType.AGGREGATE_ACTIVITY_SUMMARY)
                        // Analogous to a "Group By" in SQL, defines how data should be aggregated.
                        // bucketByTime allows for a time span, whereas bucketBySession would allow
                        // bucketing by "sessions", which would need to be defined in code.
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();
    }

    /**
     * Get the scope from a spinner view.
     *
     * @return Number of days.
     */
    private int getStatTimeScope() {
        // Zander TODO implement a spinner view and read it here
        // For now, just return a scope of one day, so it defaults to today
        return 1;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (apiClient.isConnected()) {
            apiClient.disconnect();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == REQUEST_OAUTH) {
            authInProgress = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!apiClient.isConnecting() && !apiClient.isConnected()) {
                    apiClient.connect();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        new InsertAndVerifyDataTask().execute();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // FUTURE save counters
        super.onSaveInstanceState(outState);
        outState.putBoolean(AUTH_PENDING, authInProgress);
    }

    @Override
    protected void onStart() {
        super.onStart();
        apiClient.connect();
        Log.i(TAG, "Connecting...");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, PreferencesActivity.class);
                startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    private DataSet insertFitnessData(long activity, long startTime,
                                      long endTime) {
        Log.i(TAG, "Creating a new data insert request");

        // Create a data source
        DataSource dataSource = new DataSource.Builder()
                .setAppPackageName(this)
                .setDataType(DataType.TYPE_ACTIVITY_SEGMENT)
                .setName(TAG + " - Activity")
                .setType(DataSource.TYPE_RAW)
                .build();

        // Create a data set
        DataSet dataSet = DataSet.create(dataSource);
        // For each data point, specify a start time, end time, and the data value -- in this case,
        // the number of new steps.
        DataPoint dataPoint = dataSet.createDataPoint()
                                     .setTimeInterval(startTime, endTime,
                                                      TimeUnit.MILLISECONDS);
        dataPoint.getValue(Field.FIELD_ACTIVITY).setInt((int) activity);
        dataSet.add(dataPoint);
        // [END build_insert_data_request]

        return dataSet;
    }

    private Integer[][] parseData(DataReadResult dataReadResult) {
        Integer[][] activityArray = new Integer[107][2];
        activityArray[3][1] = 0;
        activityArray[0][1] = 0;
        if (dataReadResult.getBuckets().size() > 0) {
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    for (DataPoint dp : dataSet.getDataPoints()) {
                        Value activityValue = dp.getValue(Field.FIELD_ACTIVITY);
                        Value durationValue = dp.getValue(Field.FIELD_DURATION);
                        Integer activityInt = activityValue.asInt();
                        Integer durationInt = durationValue.asInt();
                        if (activityArray[activityInt][0] == null) {
                            activityArray[activityInt][0] = 0;

                        }
                        activityArray[activityInt][0] =
                                activityArray[activityInt][0] + durationInt;

                    }
                }
            }
        } else if (dataReadResult.getDataSets().size() > 0) {
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                for (DataPoint dp : dataSet.getDataPoints()) {
                    Value activityValue = dp.getValue(Field
                                                              .FIELD_ACTIVITY);
                    Value durationValue = dp.getValue(Field.FIELD_DURATION);
                    Integer activityInt = activityValue.asInt();
                    Integer durationInt = durationValue.asInt();
                    if (activityArray[activityInt] == null) {
                        activityArray[activityInt][0] = 0;

                    }
                    activityArray[activityInt][0] =
                            activityArray[activityInt][0] + durationInt;

                }
            }

        }

        return activityArray;
    }

    /**
     * Calculate bonus life expectancy.
     *
     * @param activityArray
     *         An array of exercises returned by parseData.
     * @return Bonus life expectancy in milliseconds.
     */
    private long calculateLife(Integer[][] activityArray) {
        int totalMilliseconds = 0;
        for (Integer[] activity : activityArray) {
            int multiplier = 1;
            if (activity[1] != null) {
                multiplier = activity[1];
            }
            if (activity[0] != null) {
                totalMilliseconds += activity[0] * multiplier;
            }
        }
        return totalMilliseconds * 7;
    }

    private double calculateMoney(long lifeGained) {
        double workWeek = 0.28;
        return lifeGained * salary * workWeek / (365 * 24 * 60 * 1000);
    }

    public void startPushups(View view) {
        startActivity(
                new Intent(this, PushupCounterActivity.class),
                findViewById(R.id.pushup_button),
                getResources().getString(R.string.transition_id_counter_circle)
        );
    }

    public void startActivity(Intent intent, View transitionView,
                              String transitionID) {
        if (Build.VERSION.SDK_INT >= 21) {
            //noinspection unchecked
            Bundle options = ActivityOptions.makeSceneTransitionAnimation(
                    this,
                    Pair.create(transitionView, transitionID)
            ).toBundle();
            startActivity(intent, options);
        } else {
            startActivity(intent);
        }
    }

    public void startJumps(View view) {
        startActivity(
                new Intent(this, JumpCounterActivity.class),
                findViewById(R.id.jump_button),
                getResources().getString(R.string.transition_id_counter_circle)
        );
    }

    private class InsertAndVerifyDataTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            if (inputData != null) {
                DataSet dataSet = insertFitnessData(inputData[0], inputData[1],
                                                    inputData[2]);
                Log.i(TAG, "Inserting the dataset in the History API");
                com.google.android.gms.common.api.Status insertStatus =
                        Fitness.HistoryApi.insertData(apiClient, dataSet)
                                          .await(1, TimeUnit.MINUTES);

                // Before querying the data, check to see if the
                // insertion succeeded.
                if (!insertStatus.isSuccess()) {
                    Log.i(TAG, "There was a problem inserting the dataset.");
                    return null;
                }

                Log.i(TAG, "Data insert was successful!");
                inputData = null;
            }


            DataReadRequest readRequest = getData();
            DataReadResult dataReadResult =
                    Fitness.HistoryApi.readData(apiClient, readRequest)
                                      .await(1, TimeUnit.MINUTES);
            lifeGained = calculateLife(parseData(dataReadResult));
            moneyEarned = calculateMoney(lifeGained);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    statViewFragment.displayText(lifeGained, moneyEarned);
                }
            });
            return null;
        }
    }
}
