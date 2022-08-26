package com.oubiti.saltdroid;

import static com.oubiti.saltdroid.SaltDroidService.TAG;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class UnixManager {
    public static String execCommandVerbose(String[] cmds, String[] environmentKeyValues, File workingDirectory) {
        final java.lang.Process process;
        final StringBuilder stringBuilder = new StringBuilder();
        try {
            process = Runtime.getRuntime().exec(cmds, environmentKeyValues, workingDirectory);
            final BufferedReader inputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            final BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            while ((line = inputReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            if ((line = errorReader.readLine()) != null) {
                stringBuilder.append("\nError message:\n");
                stringBuilder.append(line);
                while ((line = errorReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
            }
        } catch (IOException e){
          Log.e(TAG, e.getMessage());
        }
        return stringBuilder.toString();
    }

    public static void execCommand(String[] cmds, File path) {
        try {
            Runtime.getRuntime().exec(cmds, null, path);
        } catch(IOException e){
            Log.e(TAG, e.getMessage());
        }
    }
}
