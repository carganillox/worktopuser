package org.drinklink.app.patch;

import org.drinklink.app.utils.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

/**
 *
 */
public class PatchManager {

    private final static String TAG = "PatchManager";

    private AppVersionUpdater updater;
    private AppCleaner cleaner;
    private Map<Integer, VersionUpdate> updates = new HashMap<>();

    @Inject
    public PatchManager(AppVersionUpdater updater, AppCleaner cleaner) {
        this.updater = updater;
        this.cleaner = cleaner;
        updates.put(93, new ClearAllUpdate());
    }

    public void patch(File cacheDir) {
        Logger.i(TAG, "patch");
        Integer version = updater.getNewVersion();
        Logger.i(TAG, "update version: " + version);
        cleaner.setCache(cacheDir);
        if (version != null) {
            VersionUpdate versionUpdate = updates.get(version);
            if (versionUpdate != null) {
                Logger.i(TAG, "apply update");
                versionUpdate.update(cleaner);
            }
        }
        updater.persistVersion();
    }

    public interface VersionUpdate {
        void update(AppCleaner cleaner);
    }

    public class ClearAllUpdate implements VersionUpdate {
        @Override
        public void update(AppCleaner cleaner) {
            cleaner.clearApplicationData();
        }
    }
}
