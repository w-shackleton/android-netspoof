package uk.digitalsquid.netspoofer.report;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

    public DeviceReport(Context context, boolean logs, boolean allLogs, boolean networkConfig) {
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
                this.logs = ("Failed to collect net config\n" + e.getMessage()).getBytes();
            }
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
}
