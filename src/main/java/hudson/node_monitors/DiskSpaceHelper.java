package hudson.node_monitors;

import hudson.slaves.OfflineCause;

import static hudson.node_monitors.DiskSpaceMonitorDescriptor.DiskSpace;

public class DiskSpaceHelper {

    private DiskSpaceHelper() {
        // static access only
    }

    public static String getOfflineCause(OfflineCause cause) {
        if (cause != null && cause instanceof DiskSpace) {
            DiskSpace offlineCause = (DiskSpace) cause;
            return "Disk space too low. Size left: " + offlineCause.getGbLeft() + " Gb";
        }
        return null;
    }
}
