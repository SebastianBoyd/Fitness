package com.sebastianboyd.fitness;


import android.app.ActivityManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;


/**
 * A base class for all of our activities.
 * <p/>
 * Allows us to share some basic methods that all activities want.
 */
public abstract class BaseActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        styleRecentAppBar();
    }

    /**
     * Style the recent apps menu in Lollipop.
     */
    protected void styleRecentAppBar() {
        if (Build.VERSION.SDK_INT >= 21) {
            String title = getResources().getString(R.string.title_app);
            Bitmap icon = BitmapFactory.decodeResource(
                    getResources(), R.drawable.ic_launcher);
            int color = getResources().getColor(R.color.primary_dark);

            setTaskDescription(new ActivityManager.TaskDescription(title, icon,
                                                                   color));
        }
    }
}
