package com.sebastianboyd.fitness;


import android.app.ActivityManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.transition.Fade;
import android.transition.Transition;
import android.view.Window;


/**
 * A base class for all of our activities.
 * <p/>
 * Allows us to share some basic methods that all activities want.
 */
public abstract class BaseActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        }
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

    protected void configureTransitions() {
        if (Build.VERSION.SDK_INT >= 21) {
            Transition fade = new Fade();
            fade.excludeTarget(android.R.id.navigationBarBackground, true);
            getWindow().setExitTransition(fade);
            getWindow().setEnterTransition(fade);
        }
    }
}
