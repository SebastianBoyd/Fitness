package com.sebastianboyd.fitness.fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sebastianboyd.fitness.R;

import java.util.concurrent.TimeUnit;


/**
 * Display the user's statistics such as lifetime saved.
 */
public class StatViewFragment extends Fragment {

    TextView lifeTextView, moneyTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stat_view, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        lifeTextView = (TextView) getActivity().findViewById(R.id.life_number);
        moneyTextView =
                (TextView) getActivity().findViewById(R.id.money_number);
    }

    public void displayText(long lifeGained, double moneyEarned) {
        String lifeText;
        int days = (int) TimeUnit.MILLISECONDS.toDays(lifeGained);
        int hours = (int) TimeUnit.MILLISECONDS.toHours(lifeGained);
        int minutes = (int) TimeUnit.MILLISECONDS.toMinutes(lifeGained);

        if (days > 0) {
            lifeText = timeToString(days, getResources()
                                            .getString(R.string.time_day_singular), getResources().getString(R.string
                                                                     .time_day_plural));
        } else if (hours > 0) {
            lifeText = timeToString(hours, getResources()
                    .getString(R.string.time_hour_singular), getResources()
                    .getString(R.string.time_hour_plural));
        } else if (minutes > 0) {
            lifeText = timeToString(minutes, getResources()
                    .getString(R.string.time_minute_singular), getResources()
                    .getString(R.string.time_minute_plural));
        } else {
            lifeText = getResources()
                    .getString(R.string.time_seconds);
        }

        lifeTextView.setText(lifeText + " " + getResources()
                .getString(R.string.lifetimesaved_text));
        moneyTextView.setText(getResources()
                                      .getString(R.string
                                                         .currency) + String.format
                ("%.2f ", moneyEarned) + getResources()
                .getString(R.string.moneyearned_text));
    }

    private static String timeToString(int time, String singular,
                                       String plural) {
        return String.format("%d %s", time, (time > 1) ? plural : singular);
    }
}
