package sg.azlabs.azpass;

import com.eatthepath.pushy.apns.ApnsClient;
import com.eatthepath.pushy.apns.ApnsClientBuilder;
import com.eatthepath.pushy.apns.PushNotificationResponse;
import com.eatthepath.pushy.apns.auth.ApnsSigningKey;
import com.eatthepath.pushy.apns.util.SimpleApnsPushNotification;
import com.eatthepath.pushy.apns.util.concurrent.PushNotificationFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;


public class APNSHelper {

    private static Logger log = LoggerFactory.getLogger(APNSHelper.class);;

    private static final String PN_IOS_APNS_HOST_PROD = "PRODUCTION";

    private static boolean PN_ENABLED = false;
    private static String  APNS_HOST ;
    private static String  APNS_TOPIC;
    private static ApnsClient apnsClient;

    // private constructor
    private APNSHelper() {}


    public static void init(String teamId, String keyId, String apnsHost, String apnsTopic, String p8FilePath) {
        APNS_HOST  = apnsHost;
        APNS_TOPIC = apnsTopic;

        log.info("Creating APN Client (" + APNS_HOST + ") ...");

        final File file = new File(p8FilePath);
        log.info("Using Apple Auth Key from file: " + file.getAbsolutePath());

        try {

            if (file.exists()) {
                InputStream keyis = new FileInputStream(file);

                apnsClient = new ApnsClientBuilder()
                        .setApnsServer(getAPNSHost())
                        .setSigningKey(ApnsSigningKey.loadFromInputStream(keyis, teamId, keyId))
                        .build();

                PN_ENABLED = true;
                log.info("Apple APNS Client init successful.");

                keyis.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());

            // disable APNS client if encounter error during startup
            PN_ENABLED = false;
            log.info("Error encountered. Push notification not enabled.");
        }
    }

    public static void shutdown() {
        if (PN_ENABLED) {
            log.info("Shutting down APNS Client ...");
            // do nothing
        }
    }

    public static boolean send(String pushToken, String payload) {
        boolean status = false;

        if (PN_ENABLED) {

            log.info("Push notification ...");
            log.debug(payload);

            status = sendImpl (pushToken, payload);

        } else {
            log.info("Push notification not enabled.");
        }
        return status;
    }

    private static String getAPNSHost() {

        if (APNS_HOST != null && APNS_HOST.equals(PN_IOS_APNS_HOST_PROD)) {
            return ApnsClientBuilder.PRODUCTION_APNS_HOST;
        }
        // default to development
        return ApnsClientBuilder.DEVELOPMENT_APNS_HOST;
    }


    private static boolean sendImpl (String token, String payload) {
        boolean status = false;

        try {
            SimpleApnsPushNotification pushNotification = new SimpleApnsPushNotification(token, APNS_TOPIC, payload);

            final PushNotificationFuture<SimpleApnsPushNotification, PushNotificationResponse<SimpleApnsPushNotification>>
                    sendNotificationFuture = apnsClient.sendNotification(pushNotification);

            final PushNotificationResponse<SimpleApnsPushNotification> pushNotificationResponse =
                    sendNotificationFuture.get();
            log.debug("Request: " + payload);

            if (pushNotificationResponse.isAccepted()) {
                log.info("Push notification accepted by APNs gateway.");
                status = true;
            } else {
                log.info("Notification rejected by the APNs gateway: " + pushNotificationResponse.getRejectionReason());

                pushNotificationResponse.getTokenInvalidationTimestamp().ifPresent(timestamp -> {
                    log.info("\t?and the token is invalid as of " + timestamp);
                });
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            log.error(e.toString());
        }

        return status;
    }


}
