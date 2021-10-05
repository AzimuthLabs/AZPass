# AZPass

AZPass is a push-notification two-factor authentication (2FA) mobile app built to work with the Gluu Server. Read more: <a href="https://super.gluu.org/">Super Gluu</a>.

The codes here are to be deployed on Gluu Server to trigger a push-notification directly to FCM (Firebase Cloud Messaging) for Android devices and APNs (Apple Push Notification service) for Apple devices.

The ideas are taken from 

```
		<artifactId>oxnotify</artifactId>
		<groupId>org.gluu</groupId>
		<version>4.2.3.Final</version>
```

When a push notification is triggered in Gluu Server, the message is sent to Amazon Simple Notification Service (SNS) before it is routed to the corresponding FCM or APNS.

<img src="img/super_gluu_sns.png" alt="Default Gluu Push Notification via Amazon SNS" />

There are options to send to Google and Apple directly. However, GCM (Google Cloud Messaging) and p12 file are no longer supported.

<img src="img/super_gluu_old_conf.png" alt="Gluu Push Notification configuration (old)" />

Therefore, AZPass attempts to provide up-to-date Helper classes to send push notification directly to FCM and APNs. 

<img src="img/super_gluu_fcm.png" alt="Gluu Push Notification - FCM" />

<img src="img/super_gluu_apns.png" alt="Gluu Push Notification - APNs" />

# Deploy 

``` 
  ...
```


# Mobile App

We work with another local company (IC.SG) to customize the look-and-feel of Super Gluu. We rebrand it as **AZPass**.

The repos for **AZPass** are in:
1. https://github.com/Identiticoders/android-super-gluu
2. https://github.com/Identiticoders/ios-super-gluu

Both are forks from: 
1. https://github.com/GluuFederation/android-super-gluu
2. https://github.com/GluuFederation/ios-super-gluu

