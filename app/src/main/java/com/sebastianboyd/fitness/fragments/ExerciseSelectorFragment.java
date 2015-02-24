package com.sebastianboyd.fitness.fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sebastianboyd.fitness.R;


/**
 * Fragment displays a list of links to {@link com.sebastianboyd.fitness.activities.CounterActivity}.
 */
public class ExerciseSelectorFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_exercise_selector,
                                container, false);
    }
}
