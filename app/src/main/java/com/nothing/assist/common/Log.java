package com.nothing.assist.common;

import android.app.Application;

import androidx.lifecycle.MutableLiveData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Log {
    private static File logFile;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-HH:mm:ss", Locale.getDefault());
    public static MutableLiveData<String> liveData = new MutableLiveData<>();
    private static boolean isInitialized = false;

    public static void initialize(Application context) {
        if (isInitialized) {
            return;
        }
        logFile = new File(context.getFilesDir(), "log.txt");
        isInitialized = true;
    }


    public static int i(String tag, String msg) {
        writeToFile(tag, msg);
        return android.util.Log.i(tag, msg);
    }


    public static int e(String tag, String msg, Exception e) {
        writeToFile(tag, msg);
        return android.util.Log.e(tag, msg, e);
    }


    private static void writeToFile(String tag, String msg) {
        try (PrintWriter out = new PrintWriter(new FileOutputStream(logFile, true))) {
            String log = String.format("%s %s: %s", sdf.format(new Date()), tag, msg);
            out.println(log);
            liveData.postValue(log);
        } catch (Exception e) {
            android.util.Log.e("Log", "Failed to write to log file", e);
        }
    }

    public static String readLog() {
        StringBuilder log = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(Files.newInputStream(logFile.toPath())))) {
            String line;
            while ((line = in.readLine()) != null) {
                log.append(line).append('\n');
            }
        } catch (Exception e) {
            android.util.Log.e("Log", "Failed to read log file", e);
        }
        return log.toString();
    }

    public static void clearLog() {
        if (logFile.exists()) {
            logFile.delete();
        }
        try {
            logFile.createNewFile();
        } catch (Exception e) {
            android.util.Log.e("Log", "Failed to clear log file", e);
        }
    }
}