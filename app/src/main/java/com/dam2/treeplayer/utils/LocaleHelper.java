package com.dam2.treeplayer.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;

import java.util.Locale;

public class LocaleHelper {

    private static final String SELECTED_LANGUAGE = "Locale.Helper.Selected.Language";

    public static Context setLocale(Context context, String language) {
        persist(context, language);
        return updateResources(context, language);
    }

    public static Context setLocale(Context context) {
        return updateResources(context, getPersistedData(context));
    }

    private static void persist(Context context, String language) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString(SELECTED_LANGUAGE, language).apply();
    }

    private static String getPersistedData(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(SELECTED_LANGUAGE, "es"); // español por defecto
    }

    private static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale);
            context = context.createConfigurationContext(configuration);
        } else {
            configuration.locale = locale;
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        }
        return context;
    }

    public static Locale getLocale(Resources resources) {
        Configuration configuration = resources.getConfiguration();
        return Build.VERSION.SDK_INT >= 24 ? configuration.getLocales().get(0) : configuration.locale;
    }
}