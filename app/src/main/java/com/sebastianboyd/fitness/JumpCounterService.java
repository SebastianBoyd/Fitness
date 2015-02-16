package com.sebastianboyd.fitness;


import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import java.util.Random;


public class JumpCounterService extends Service{
    private Looper mServiceLooper;
    private long lastUpdate = 0;
    private ServiceHandler mServiceHandler;
    private static final int SHAKE_THRESHOLD = 600;
    private float last_x, last_y, last_z;
    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    // Random number generator
    private final Random mGenerator = new Random();
    protected double exerciseCount = 0;
    protected long startTime = 0;
    protected long endTime = 0;
    protected SensorManager sensorManager;
    protected Sensor sensor;

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        JumpCounterService getService() {
            // Return this instance of LocalService so clients can call public methods
            return JumpCounterService.this;
        }
    }

    private final class ServiceHandler extends Handler
            implements SensorEventListener {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            sensor =
                    sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            stopSelf(msg.arg1);
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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments");
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

    }
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /** method for clients */
    public double getCurrentJumps() {
        return exerciseCount;
    }
}