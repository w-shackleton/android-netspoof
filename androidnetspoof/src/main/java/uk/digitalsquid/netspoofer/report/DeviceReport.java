package uk.digitalsquid.netspoofer.report;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import uk.digitalsquid.netspoofer.config.FileFinder;
import uk.digitalsquid.netspoofer.config.FileInstaller;
import uk.digitalsquid.netspoofer.config.IOHelpers;
import uk.digitalsquid.netspoofer.config.LogConf;
import uk.digitalsquid.netspoofer.config.ProcessRunner;

/**
 * Generates a complete device report for debugging
 */
public class DeviceReport implements LogConf {
    private byte[] logs;
    private byte[] netConf;

    private String deviceInfo;

    private final File directory;

    public DeviceReport(Context context, boolean info, boolean logs, boolean allLogs, boolean networkConfig) {
        directory = new File(context.getFilesDir(), "public/");
        directory.mkdir();
        Log.i(TAG, String.format("Collecting logs. info: %b, logs: %b, allLogs: %b, networkConfig: %b",
                info,
                logs,
                allLogs,
                networkConfig));
        if (logs || allLogs) {
            try {
                this.logs = runLogcat(allLogs);
            } catch (IOException e) {
                Log.e(TAG, "Failed to collect logcat output", e);
                this.logs = ("Failed to collect logcat output\n" + e.getMessage()).getBytes();
            }
        }

        if (networkConfig) {
            try {
                netConf = runCollectNetConf(context);
            } catch (IOException e) {
                Log.e(TAG, "Failed to collect net config", e);
                this.netConf = ("Failed to collect net config\n" + e.getMessage()).getBytes();
            }
        }

        if (info) {
            deviceInfo = getDeviceInfo();
        }
    }

    private static byte[] runLogcat(boolean all) throws IOException {
        ProcessBuilder procRun = all ? new ProcessBuilder(
                "logcat",
                "-d",
                "*:V") : new ProcessBuilder(
                "logcat",
                "-d",
                "android-netspoof:V",
                "*:S");
        procRun.redirectErrorStream(true);
        Process proc = procRun.start();

        byte[] result = IOHelpers.readFileContentsToByte(proc.getInputStream());
        proc.destroy();
        return result;
    }

    private static byte[] runCollectNetConf(Context context) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(FileFinder.SU, "-c",
                FileInstaller.getScriptPath(context, "collect_netconf") + " " +
                        FileInstaller.getScriptPath(context, "config")); // Pass config script as arg.
        pb.redirectErrorStream(true);

        Log.d(TAG, "netconf command: " + pb.command());

        // We now write the env to a config file, which is loaded in.
        Map<String, String> env = new HashMap<String, String>();

        env.put("IPTABLES", FileFinder.IPTABLES);

        ProcessRunner.writeEnvConfigFile(context, env);

        Process proc = pb.start();

        byte[] result = IOHelpers.readFileContentsToByte(proc.getInputStream());
        proc.destroy();
        return result;
    }

    private static String getDeviceInfo() {
        return String.format(Locale.ENGLISH,
                "Build.BOARD: %s\n" +
                "Build.BOOTLOADER: %s\n" +
                "Build.BRAND: %s\n" +
                "Build.CPU_ABI: %s\n" +
                "Build.CPU_ABI2: %s\n" +
                "Build.DEVICE: %s\n" +
                "Build.DISPLAY: %s\n" +
                "Build.FINGERPRINT: %s\n" +
                "Build.HARDWARE: %s\n" +
                "Build.HOST: %s\n" +
                "Build.ID: %s\n" +
                "Build.MANUFACTURER: %s\n" +
                "Build.MODEL: %s\n" +
                "Build.PRODUCT: %s\n" +
                // "Build.SERIAL: %s\n" +
                "Build.TAGS: %s\n" +
                "Build.TIME: %s\n" +
                "Build.TYPE: %s\n" +
                "Build.UNKNOWN: %s\n" +
                "Build.USER: %s\n",

                Build.BOARD,
                Build.BOOTLOADER,
                Build.BRAND,
                Build.CPU_ABI,
                Build.CPU_ABI2,
                Build.DEVICE,
                Build.DISPLAY,
                Build.FINGERPRINT,
                Build.HARDWARE,
                Build.HOST,
                Build.ID,
                Build.MANUFACTURER,
                Build.MODEL,
                Build.PRODUCT,
                // Build.SERIAL,
                Build.TAGS,
                Build.TIME,
                Build.TYPE,
                Build.UNKNOWN,
                Build.USER);
    }

    public File generate() throws IOException {
        File report = new File(directory, "report.zip");
        ZipOutputStream zip = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(report)));

        if (logs != null) {
            zip.putNextEntry(new ZipEntry("logs.txt"));
            zip.write(logs);
        }
        if (netConf != null) {
            zip.putNextEntry(new ZipEntry("netconf.txt"));
            zip.write(netConf);
        }
        if (deviceInfo != null) {
            zip.putNextEntry(new ZipEntry("deviceinfo.txt"));
            zip.write(deviceInfo.getBytes());
        }
        zip.close();

        return report;
    }
}
