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
  