package com.myjavaapp.android;

import android.app.Application;
import android.util.Log;

import org.koin.core.context.GlobalContext;
import org.koin.core.logger.Level;

public class MusicApp extends Application {

    private static final String TAG = "MusicApp";

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Koin for the Kotlin YouTube Music Scraper library
        try {
            // Check if Koin is already started
            if (GlobalContext.INSTANCE.getOrNull() == null) {
                // Start Koin and register Android context
                org.koin.core.KoinApplication koinApp = GlobalContext.INSTANCE.startKoin(
                    app -> {
                        // Set log level
                        app.printLogger(Level.ERROR);
                        return null;
                    }
                );

                // Register Android context with Koin
                org.koin.android.ext.koin.KoinExtKt.androidContext(
                    koinApp,
                    this
                );

                Log.d(TAG, "Koin initialized with Android context");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize Koin", e);
        }
    }
}
