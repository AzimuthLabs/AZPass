package sg.azlabs.azpass;

import com.google.auth.oauth2.GoogleCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Scanner;

public class FCMHelper {

    private static Logger log = LoggerFactory.getLogger(FCMHelper.class);

    private static boolean PN_ENABLED = false;
    private static String FCM_PROJECT_ID;
    private static String FCM_KEY_FILE;
    private static String FCM_SEND_ENDPOINT;

    private static final String FCM_BASE_URL      = "https://fcm.googleapis.com";

    // authentication key file will always be of the following format
    // and will always be placed in WEB-INF/classes directory

    private static final String   MESSAGING_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
    private static final String[] SCOPES          = { MESSAGING_SCOPE };

    // private constructor
    private FCMHelper() {}


    public static void init(String projectId, String adminSDKFilePath) {
        final String methodName = "init";

        FCM_PROJECT_ID = projectId;
        FCM_KEY_FILE = adminSDKFilePath;
        FCM_SEND_ENDPOINT = "/v1/projects/" + FCM_PROJECT_ID + "/messages:send";

        log.info("Creating FCM Client ...");

        try {

            // call getAccessToken to test connectivity to FCM upon start-up
            String accessToken = getAccessToken();
            log.debug("Access Token: " + accessToken);
            PN_ENABLED = true;

        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());

            // disable FCM client if encounter error during startup
            PN_ENABLED = false;
            log.info("Error encountered. Push notification not enabled.");
        }
    }

    public static void shutdown() {
        final String methodName = "shutdown";

        if (PN_ENABLED) {
            log.info("Shutting down FCM Client ...");
            // do nothing
        }
    }

    public static boolean send(String payload) {
        boolean status = false;

        if (PN_ENABLED) {

            log.info("Push notification ...");
            log.debug(payload);

            status = sendImpl (payload);

        } else {
            log.info("Push notification not enabled.");
        }
        return status;
    }

    // Retrieve a valid access token that can be use to authorize requests to the FCM REST
    private static String getAccessToken() throws IOException {

        final File file = new File(FCM_KEY_FILE);
        log.info("Using Google Credentials from file: " + file.getAbsolutePath());
        if (file.exists()) {
            FileInputStream s = new FileInputStream(file);

            GoogleCredentials googleCredential = GoogleCredentials.fromStream(s).createScoped(Arrays.asList(SCOPES));
            googleCredential.refreshIfExpired();

            return googleCredential.getAccessToken().getTokenValue();

        } else {
            throw new IOException("FCM Admin SDK Key File not found");
        }
    }

    // Create HttpURLConnection that can be used for both retrieving and publishing.
    private static HttpURLConnection getConnection() throws IOException {

        log.info("Establishing a connection to " + FCM_BASE_URL + FCM_SEND_ENDPOINT);
        URL url = new URL(FCM_BASE_URL + FCM_SEND_ENDPOINT);

        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestProperty("Authorization", "Bearer " + getAccessToken());
        httpURLConnection.setRequestProperty("Content-Type",  "application/json; UTF-8");

        return httpURLConnection;
    }

    private static boolean sendImpl (String payload) {
        final String methodName = "sendImpl";

        boolean status = false;

        try {
            HttpURLConnection connection = getConnection();
            connection.setDoOutput(true);
            DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.writeBytes(payload);
            outputStream.flush();
            outputStream.close();
            log.debug("Request: " + payload );

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                String response = inputStreamToString(connection.getInputStream());
                log.info("Push notification accepted by FCM gateway.");
                log.debug("Response: " + response);
                status = true;
            } else {
                String response = inputStreamToString(connection.getErrorStream());
                log.error("Notification rejected by the Firebase gateway.");
                log.error("Response: " + response);
            }

        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.toString());
        }

        return status;
    }

    // Read contents of InputStream into String.
    private static String inputStreamToString(InputStream inputStream) throws IOException {

        StringBuilder stringBuilder = new StringBuilder();
        Scanner scanner = new Scanner(inputStream);
        while (scanner.hasNext()) {
            stringBuilder.append(scanner.nextLine());
        }
        scanner.close();
        return stringBuilder.toString();
    }

}
