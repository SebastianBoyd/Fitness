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
            lifeText = timeToString(days, "day", "days");
        } else if (hours > 0) {
            lifeText = timeToString(hours, "hour", "hours");
        } else if (minutes > 0) {
            lifeText = timeToString(minutes, "minute", "minutes");
        } else {
            lifeText = "a few seconds";
        }

        lifeTextView.setText(lifeText + " of lifetime saved");
        moneyTextView.setText(String.format("$%.2f earned in that time",
                                            moneyEarned));
    }

    private static String timeToString(int time, String singular,
                                       String plural) {
        return String.format("%d %s", time, (time > 1) ? plural : singular);
    }
}
