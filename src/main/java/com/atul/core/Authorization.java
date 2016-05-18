package com.atul.core;

import com.atul.config.Singletons;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.services.drive.DriveScopes;

import java.io.InputStreamReader;
import java.util.Collections;

/**
 * Created by atul on 18/05/16.
 */
public class Authorization {

    /** Authorizes the installed application to access user's protected data. */
    private Credential authorize() throws Exception {
        // load client secrets
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(Singletons.JSON_FACTORY,
                new InputStreamReader(Main.class.getResourceAsStream("/client_secrets.json")));
        if (clientSecrets.getDetails().getClientId().startsWith("Enter")
                || clientSecrets.getDetails().getClientSecret().startsWith("Enter ")) {
            System.out.println(
                    "Enter Client ID and Secret from https://code.google.com/apis/console/?api=drive "
                            + "into src/main/resources/client_secrets.json");
            System.exit(1);
        }
        System.out.println("WOAAHHHH");
        // set up authorization code flow
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                Singletons.httpTransport, Singletons.JSON_FACTORY, clientSecrets,
                Collections.singleton(DriveScopes.DRIVE_FILE)).setDataStoreFactory(Singletons.dataStoreFactory)
                .build();
        // authorize
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    public Credential getCredential() {
        Credential credential = null;
        try {
            credential = authorize();
        } catch (Exception ex) {
            System.out.println("Problem while authorizing.");
            ex.printStackTrace();
        }
        return credential;
    }
}
