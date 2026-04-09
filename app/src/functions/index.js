const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const logger = require("firebase-functions/logger");
const admin = require("firebase-admin");

admin.initializeApp();

exports.sendPushOnNotificationCreated = onDocumentCreated(
  "users/{userId}/notifications/{notificationId}",
  async (event) => {
    const snapshot = event.data;
    if (!snapshot) {
      logger.warn("No snapshot in event.");
      return;
    }

    const notificationData = snapshot.data() || {};
    const userId = event.params.userId;
    const notificationId = event.params.notificationId;

    logger.info("Notification trigger fired.", { userId, notificationId, notificationData });

    const userRef = admin.firestore().collection("users").doc(userId);
    const userDoc = await userRef.get();

    if (!userDoc.exists) {
      logger.warn("User document not found.", { userId });
      return;
    }

    const fcmToken = userDoc.get("fcmToken");

    if (!fcmToken) {
      logger.warn("Missing fcmToken on user document.", { userId });
      return;
    }

    const title = String(notificationData.title || "GiftHub");
    const body = String(notificationData.message || "You have a new notification.");
    const targetRoute = String(notificationData.targetRoute || "order_history");
    const orderId = String(notificationData.orderId || "");
    const type = String(notificationData.type || "giftHubNotification");

    try {
      const response = await admin.messaging().send({
        token: fcmToken,
        data: {
          title,
          body,
          notificationId,
          targetRoute,
          orderId,
          type
        },
        android: {
          priority: "high"
        },
        notification: {
          title,
          body
        }
      });

      logger.info("Push sent successfully.", {
        userId,
        notificationId,
        response
      });
    } catch (error) {
      logger.error("Push send failed.", {
        userId,
        notificationId,
        error: error.message,
        code: error.code
      });

      if (
        error.code === "messaging/registration-token-not-registered" ||
        error.code === "messaging/invalid-registration-token"
      ) {
        await userRef.update({
          fcmToken: admin.firestore.FieldValue.delete()
        }).catch(() => null);
      }
    }
  }
);