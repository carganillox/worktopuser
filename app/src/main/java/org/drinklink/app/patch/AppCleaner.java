package org.drinklink.app.patch;

import org.drinklink.app.utils.Logger;

import java.io.File;

import javax.inject.Inject;

import lombok.Setter;

/**
 *
 */
public class AppCleaner {

    @Setter
    private File cache;

    @Inject
    public AppCleaner() {
    }

    public void clearApplicationData() {
        File appDir = new File(cache.getParent());
        if(appDir.exists()){
            String[] children = appDir.list();
            for(String s : children){
                if(!s.equals("lib")){
                    deleteDir(new File(appDir, s));
                    Logger.i("TAG", "File /data/data/APP_PACKAGE/" + s +" DELETED ");
                }
            }
        }
    }
    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }
}
