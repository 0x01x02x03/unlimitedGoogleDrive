package com.atul.core;

import com.atul.config.Singletons;
import com.google.api.services.drive.Drive;

import java.util.Scanner;

/**
 * Created by atul on 18/05/16.
 */
public class Main {
    private static final Long freeSpaceThreshold = 1000000000L; // 1GB.
    public static void main(String [] args ) {
        try {
            Drive drive = Singletons.getDriveService();
            DriveUtils driveUtils = new DriveUtils(drive);
            System.out.println("Free space is " + driveUtils.getFreeSpace());

            while(true) {
                // Read a path from stdIn.
                Scanner sc = new Scanner(System.in);
                String path = sc.nextLine();
                if (path.equals("end") || path.isEmpty()) {
                    break;
                }
                driveUtils.uploadFile(path);
            }
            System.out.println("Free space is " + driveUtils.getFreeSpace());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
