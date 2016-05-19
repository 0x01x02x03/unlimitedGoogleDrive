package com.atul.core;

import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.About;
import com.google.api.services.drive.model.File;
import com.google.common.io.Files;

import javax.activation.MimetypesFileTypeMap;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Scanner;

/**
 * Created by atul on 18/05/16.
 */
public class DriveUtils {

    private Drive drive;

    public DriveUtils(Drive drive) {
        this.drive = drive;
    }

    public String getRootFolderID() {
        String root = null;
        try {
            root = drive.files().get("0AMfX02Qydr5yUk9PVA").setFields("id").execute().getId();
            System.out.println("root " + root);
        } catch (Exception ex) {
            System.out.println("Exception while getting root folder ");
            ex.printStackTrace();
        }
        return root;
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
        } catch (Exception ex) {
            System.out.println("Exception while getting free space");
            ex.printStackTrace();
        }
        return freeSpaceKb;
    }

    public String uploadFolder(String path, String parentID) {
        String folderID = null;
        java.io.File folder = new java.io.File(path);

        try {
            File fileMetadata = new File();
            fileMetadata.setName(folder.getName());
            fileMetadata.setMimeType("application/vnd.google-apps.folder");
            fileMetadata.setParents(Collections.singletonList(parentID));

            File file = drive.files().create(fileMetadata)
                    .setFields("id")
                    .execute();
            folderID = file.getId();
            System.out.println("Folder ID for " + path + " : " + file.getId());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return folderID;
    }

    public boolean uploadPath(String path) {
        String driveRootID = getRootFolderID();

        java.io.File folder = new java.io.File(path);
        if (!folder.isDirectory()) {
            return uploadFile(path, driveRootID);
        } else {
            // Upload this folder and get the folderId.
            String folderID = uploadFolder(path, driveRootID);
            return uploadDir(path, folderID);
        }
    }

    /*
    Recursively upload the contents of the directory.
     */
    public boolean uploadDir(String path, String parentID) {
        System.out.println("Dir uplaod Path is " + path);
        java.io.File folder = new java.io.File(path);
        // Upload this folder and get the folderId.
        for(java.io.File f : folder.listFiles()) {
            boolean success = false;
            if (f.isDirectory()) {
                // upload this folder and recursively call this.
                String newFolderID = uploadFolder(f.getPath(), parentID);
                success = uploadDir(f.getPath(), newFolderID);
            }

            else {
                System.out.println("file Path is " + f.getPath());
                success = uploadFile(f.getPath(), parentID);
            }
            if (!success) {
                System.out.println("Error encountered while uploading " + f.getPath()
                        + " \n Do you want to continue? (y/n)");
                Scanner scanner = new Scanner(System.in);
                String shouldContinue = scanner.nextLine();
                if (shouldContinue.equals("y") || shouldContinue.equals("Y")) {
                    continue;
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean uploadFile(String path, String parent) {
        File fileMetaData = new File();
        java.io.File file = new java.io.File(path);
        MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();

        fileMetaData.setName(file.getName());
        fileMetaData.setMimeType(mimeTypesMap.getContentType(file));
        fileMetaData.setParents(Collections.singletonList(parent));

        FileContent mediaContent = new FileContent(mimeTypesMap.getContentType(file), file);

        File f = null;
        Drive.Files.Create create = null;

        try {
            System.out.println("Uploading file " + file.getName());
            create = drive.files().create(fileMetaData, mediaContent)
                    .setFields("id, parents");
            create.getMediaHttpUploader().setProgressListener(new FileUpdateProgressListener());

            // Using default chunk size of 10MB.
            create.getMediaHttpUploader().setChunkSize(MediaHttpUploader.DEFAULT_CHUNK_SIZE);
            f = create.execute();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        System.out.println("File ID: " + f.getId() + " Parent: " + f.getParents().toString());
        return true;
    }
}
