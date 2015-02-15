package com.sebastianboyd.fitness;


import android.content.Context;
import android.content.SharedPreferences;


public class SaveData {
    private static String MY_STRING_PREF = "stringpref";
    private static String MY_INT_PREF = "intpref";

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences("myprefs", 0);
    }

    public static String getMyStringPref(Context context) {
        return getPrefs(context).getString(MY_STRING_PREF, "default");
    }

    public static int getMyIntPref(Context context) {
        return getPrefs(context).getInt(MY_INT_PREF, 42);
    }

    public static void setMyStringPref(Context context, String value) {
        // perform validation etc..
        getPrefs(context).edit().putString(MY_STRING_PREF, value).commit();
    }

    public static void setMyIntPref(Context context, int value) {
        // perform validation etc..
        getPrefs(context).edit().putInt(MY_INT_PREF, value).commit();
    }
}
