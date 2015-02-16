package com.sebastianboyd.fitness;


import android.content.Context;
import android.content.SharedPreferences;


public class SaveData {

    public static final String INCOME = "income";
    public static final String TOTAL_COUNT = "totalCount";
    public static final String TOTAL_TIME = "totalTime";

    public static int getIntPref(Context context, String id) {
        return getPrefs(context).getInt(id, 0);
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences("myprefs", 0);
    }

    public static void setIntPref(Context context, String id, int value) {
        // perform validation etc..
        getPrefs(context).edit().putInt(id, value).commit();
    }
}
