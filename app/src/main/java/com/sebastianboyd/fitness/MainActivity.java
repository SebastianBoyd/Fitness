package com.sebastianboyd.fitness;


import android.app.ActivityOptions;
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
import android.widget.TextView;

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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

//TODO add preferences

public class MainActivity extends BaseActivity {

    private static final int REQUEST_OAUTH = 1;
    /**
     * Track whether an authorization activity is stacking over the current
     * activity, i.e. when
     * a known auth error is being resolved, such as showing the account
     * chooser
     * or presenting a
     * consent dialog. This avoids common duplications as might happen on
     * screen
     * rotations, etc.
     */
    private static final String AUTH_PENDING = "auth_state_pending";
    private static final String TAG = "Fit Auth";
    private static final String DATE_FORMAT = "yyyy.MM.dd HH:mm:ss";
    public long lifeGained = 0;
    private boolean authInProgress = false;
    private GoogleApiClient mClient = null;
    private int salary = 60000;
    private double moneyEarned = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        configureTransitions();

        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }

        buildFitnessClient();
        getData(7);

        if (Build.VERSION.SDK_INT >= 21) {
            findViewById(R.id.add_pushup_button).setTransitionName(
                    getResources()
                            .getString(R.string.transition_pushup_circle));
        }

    }

    /**
     * Build a {@link GoogleApiClient} that will authenticate the user and
     * allow
     * the application
     * to connect to Fitness APIs. The scopes included should match the scopes
     * your app needs
     * (see documentation for details). Authentication will occasionally fail
     * intentionally,
     * and in those cases, there will be a known resolution, which the
     * OnConnectionFailedListener()
     * can address. Examples of this include the user never having signed in
     * before, or having
     * multiple accounts on the device and needing to specify which account to
     * use, etc.
     */
    private void buildFitnessClient() {
        // Create the Google API Client
        mClient = new GoogleApiClient.Builder(this)
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
                )
                .build();
    }

    protected DataReadRequest getData(int days) {
        // Setting a start and end date using a range of 1 week before this moment.
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long startTime = 1;
        long endTime = cal.getTimeInMillis();
        if (days > 0){
            cal.add(Calendar.DAY_OF_YEAR, - days);
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

    private DataSet insertFitnessData(int startTime, int endTime,
                                      int activity) {
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
                                     .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
        dataPoint.getValue(Field.FIELD_ACTIVITY).setInt(activity);
        dataSet.add(dataPoint);
        // [END build_insert_data_request]

        return dataSet;
    }

    protected void displayText() {
        setContentView(R.layout.activity_main);
        TextView life;
        int day = (int)TimeUnit.SECONDS.toDays(lifeGained);
        long hours = TimeUnit.SECONDS.toHours(lifeGained) - (day * 24);
        long minute = TimeUnit.SECONDS.toMinutes(lifeGained) - (TimeUnit.SECONDS
                                                               .toHours(lifeGained)* 60);
        long second = TimeUnit.SECONDS.toSeconds(lifeGained) - (TimeUnit.SECONDS
                                                               .toMinutes(lifeGained)
                                                                *60);
        life = (TextView)findViewById(R.id.life);
        String str = String.valueOf(day) + "," + String.valueOf(hours) +
                     "," + String.valueOf(minute) + "," + String.valueOf(second);
        life.setText(str);
        TextView money;
        money = (TextView)findViewById(R.id.money);
        String strMoney = String.valueOf(moneyEarned);
        money.setText("$" + strMoney);

    }


    @Override
    protected void onStop() {
        super.onStop();
        if (mClient.isConnected()) {
            mClient.disconnect();
        }
    }

    protected Integer[][] parseData(DataReadResult dataReadResult) {
        Integer[][] activityArray = new Integer[107][2];
        activityArray[3][1] = 0;
        activityArray[0][1] = 0;
        if (dataReadResult.getBuckets().size() > 0) {
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    for (DataPoint dp : dataSet.getDataPoints()) {
                        Value activityValue = dp.getValue(Field
                                                                  .FIELD_ACTIVITY);
                        Value durationValue = dp.getValue(Field.FIELD_DURATION);
                        Integer activityInt = activityValue.asInt();
                        Integer durationInt = durationValue.asInt();
                        if (activityArray[activityInt][0] == null){
                            activityArray[activityInt][0] = 0;

                        }
                        activityArray[activityInt][0] =
                                activityArray[activityInt][0] + durationInt;


                    }
                }
            }
        }

        else if (dataReadResult.getDataSets().size() > 0) {
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

    private void calculateLife(Integer[][] activityArray){
        int totalMilliseconds = 0;
        for (Integer[] activity : activityArray) {
            int multiplier = 1;
            if (activity[1] != null){
                multiplier = activity[1];
            }
            if (activity[0] != null) {
                totalMilliseconds = totalMilliseconds + (activity[0] * multiplier);
            }


        }
        int totalSeconds = (totalMilliseconds / 1000);
        lifeGained = totalSeconds * 7;
    }

    private void calculateMoney(){
        double workWeek = 0.28;
        double moneyEarnedLong = lifeGained * salary * workWeek / 365 / 24 / 60;
        moneyEarned = round(moneyEarnedLong, 2);
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    //For testing
    // TODO delete on release
    /*
    private void printData(DataReadResult dataReadResult) {
        // [START parse_read_data_result]
        // If the DataReadRequest object specified aggregated data, dataReadResult will be returned
        // as buckets containing DataSets, instead of just DataSets.
        if (dataReadResult.getBuckets().size() > 0) {
            Log.i(TAG, "Number of returned buckets of DataSets is: "
                       + dataReadResult.getBuckets().size());
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    dumpDataSet(dataSet);
                }
            }
        } else if (dataReadResult.getDataSets().size() > 0) {
            Log.i(TAG, "Number of returned DataSets is: "
                       + dataReadResult.getDataSets().size());
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                dumpDataSet(dataSet);
            }
        }
        // [END parse_read_data_result]
    }


    private void dumpDataSet(DataSet dataSet) {
        Log.i(TAG,
              "Data returned for Data type: " +
              dataSet.getDataType().getName());
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

        for (DataPoint dp : dataSet.getDataPoints()) {
            Log.i(TAG, "Data point:");
            Log.i(TAG, "\tType: " + dp.getDataType().getName());
            Log.i(TAG, "\tStart: " + dateFormat
                    .format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.i(TAG, "\tEnd: " +
                       dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
            for (Field field : dp.getDataType().getFields()) {
                Log.i(TAG, "\tField: " + field.getName() +
                           " Value: " + dp.getValue(field));
            }
        }
    }
    */

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == REQUEST_OAUTH) {
            authInProgress = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mClient.isConnecting() && !mClient.isConnected()) {
                    mClient.connect();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(AUTH_PENDING, authInProgress);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect to the Fitness API
        Log.i(TAG, "Connecting...");
        mClient.connect();
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
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void startPushups(View view) {
        Intent intent = new Intent(this, AddPushupsActivity.class);
        final View pushupButton = findViewById(R.id.add_pushup_button);

        if (Build.VERSION.SDK_INT >= 21) {
            //noinspection unchecked
            Bundle options =
                    ActivityOptions.makeSceneTransitionAnimation(
                            this,
                            Pair.create(pushupButton,
                                        pushupButton.getTransitionName())
                            // FUTURE: Make this work at some point
//                            Pair.create(findViewById(
//                                                android.R.id.navigationBarBackground),
//                                        Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME),
//                            Pair.create(findViewById(
//                                                android.R.id.statusBarBackground),
//                                        Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME)
                    ).toBundle();
            startActivity(intent, options);
        } else {
            startActivity(intent);
        }
    }

    public void startJumps(View view) {
        Intent intent = new Intent(this, AddJumpsActivity.class);
        startActivity(intent);
    }


    private class InsertAndVerifyDataTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            //DataSet dataSet = insertFitnessData(1, 2, 4);

            DataReadRequest readRequest = getData(7);
            DataReadResult dataReadResult =
                    Fitness.HistoryApi.readData(mClient,
                                                readRequest)
                                      .await(1, TimeUnit.MINUTES);
            calculateLife(parseData(dataReadResult));
            calculateMoney();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    displayText();
                }
            });
            return null;
        }

    }
}
