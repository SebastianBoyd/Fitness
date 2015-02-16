package com.sebastianboyd.fitness;


import android.content.Context;
import android.content.SharedPreferences;


public class SaveData {
    private static String MY_STRING_PREF = "stringpref";
    private static String MY_INT_PREF = "intpref";

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences("myprefs", 0);
    }

    public static String getMyStringPref(Context context, String id) {
        return getPrefs(context).getString(id, "default");
    }

    public static int getMyIntPref(Context context, String id) {
        return getPrefs(context).getInt(id, 0);
    }

    public static void setMyStringPref(Context context, String id,
                                       String value) {
        // perform validation etc..
        getPrefs(context).edit().putString(id, value).commit();
    }

    public static void setMyIntPref(Context context, String id, int value) {
        // perform validation etc..
        getPrefs(context).edit().putInt(id, value).commit();
    }
}
