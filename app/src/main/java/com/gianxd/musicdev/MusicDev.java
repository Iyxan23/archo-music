package com.gianxd.musicdev;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.RecentlyNonNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class MusicDev extends Application {

    public static Context applicationContext;
    public static volatile Handler applicationHandler;
    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

    @Override
    public void onCreate() {

        applicationContext = this;
        applicationHandler = new Handler(MusicDev.applicationContext.getMainLooper());
        this.uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(@NonNull Thread thread, @NonNull Throwable ex) {
                Intent intent = new Intent(getApplicationContext(), DebugActivity.class);

                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("error", getStackTrace(ex));

                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 11111, intent, PendingIntent.FLAG_ONE_SHOT);

                AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 1000, pendingIntent);

                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(2);

                uncaughtExceptionHandler.uncaughtException(thread, ex);
            }
        });
        super.onCreate();
    }


    private String getStackTrace(Throwable th) {
        final Writer result = new StringWriter();

        final PrintWriter printWriter = new PrintWriter(result);
        Throwable cause = th;

        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        final String stacktraceAsString = result.toString();
        printWriter.close();

        return stacktraceAsString;
    }
}
