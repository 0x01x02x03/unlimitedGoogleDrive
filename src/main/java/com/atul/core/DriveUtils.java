package com.atul.core;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.About;
import com.google.api.services.drive.model.File;
import com.google.common.io.Files;

import javax.activation.MimetypesFileTypeMap;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Created by atul on 18/05/16.
 */
public class DriveUtils {

    private Drive drive;

    public DriveUtils(Drive drive) {
        this.drive = drive;
    }

    public Long getFreeSpace() {
        Long freeSpaceKb = 0L;
        try {
            About about = drive.about().get()
                    .setFields("storageQuota, user")
                    .execute();

            System.out.println("Current user name: " + about.getUser().getDisplayName());

            freeSpaceKb = (about.getStorageQuota().getLimit() - about.getStorageQuota().getUsage());
            System.out.println("Free quota (kbytes): " + freeSpaceKb);
            System.out.println("Free quota (kbytes): " + about.toPrettyString());
        } catch (Exception ex) {
            System.out.println("Exception while getting free space");
            ex.printStackTrace();
        }
        return freeSpaceKb;
    }

    public boolean uploadFile(String path) {
        File fileMetaData = new File();
        java.io.File file = new java.io.File(path);
        fileMetaData.setName(file.getName());
        MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
        fileMetaData.setMimeType(mimeTypesMap.getContentType(file));

        FileContent mediaContent = new FileContent(mimeTypesMap.getContentType(file), file);

        System.out.print("Uploading file");
        File f = null;
        try {
            f = drive.files().create(fileMetaData, mediaContent)
                    .setFields("id")
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        System.out.println("File ID: " + f.getId());

        return true;
    }
}
