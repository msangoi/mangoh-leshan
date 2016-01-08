package com.sierrawireless.mangoh.objects;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class ProcessUtil {

    public static String command(String... command) {

        try {
            Process process = new ProcessBuilder(command).start();

            if (process.waitFor(5, TimeUnit.SECONDS)) {
                if (process.getErrorStream().available() > 0) {
                    try (BufferedReader r = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                        System.err.println(command + ": " + r.readLine());
                    }
                } else {
                    try (BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                        return r.readLine();
                    }
                }
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

}
