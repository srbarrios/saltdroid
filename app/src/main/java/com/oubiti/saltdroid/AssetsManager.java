package com.oubiti.saltdroid;

import static android.content.Context.TELECOM_SERVICE;
import static com.oubiti.saltdroid.SaltDroidService.TAG;

import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

public class AssetsManager {

    public static void copyAssetFolder(AssetManager assetManager,
                                          String fromAssetPath, String toPath) {
        final Set<PosixFilePermission> ownerWritable = PosixFilePermissions.fromString("rwxrwxrwx");
        try {
            String[] files = assetManager.list(fromAssetPath);
            String storageFilePath = toPath + "/" + fromAssetPath;
            new File(storageFilePath).mkdirs();
            for (String file : files) {
                String assetFilePath = fromAssetPath + "/" + file;
                if (assetManager.list(assetFilePath).length == 0){
                    InputStream inputStream = assetManager.open(assetFilePath);
                    Files.copy(inputStream, Paths.get(storageFilePath + "/" + file),
                            StandardCopyOption.REPLACE_EXISTING);
                    Files.setPosixFilePermissions(Paths.get(storageFilePath + "/" + file), ownerWritable);
                } else {
                    copyAssetFolder(assetManager, assetFilePath, toPath);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

}
