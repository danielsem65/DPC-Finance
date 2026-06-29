const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

const db = admin.firestore();

/**
 * Triggered when a new command document is written to the 'commands' collection.
 * Sends the command via FCM to the target device.
 */
exports.onCommandWrite = functions.firestore
    .document('commands/{commandId}')
    .onCreate(async (snap, context) => {
        const command = snap.data();
        const deviceId = command.deviceId;
        const commandType = command.type;

        functions.logger.info(`Command received: ${commandType} for device ${deviceId}`);

        try {
            // Get the device's FCM token
            const deviceDoc = await db.collection('devices').doc(deviceId).get();
            if (!deviceDoc.exists) {
                functions.logger.error(`Device ${deviceId} not found`);
                await snap.ref.update({ status: 'failed', error: 'Device not found' });
                return;
            }

            const deviceData = deviceDoc.data();
            const fcmToken = deviceData.fcmToken;

            if (!fcmToken) {
                functions.logger.error(`No FCM token for device ${deviceId}`);
                await snap.ref.update({ status: 'failed', error: 'No FCM token' });
                return;
            }

            // Build FCM message
            const message = {
                token: fcmToken,
                data: {
                    command: commandType
                }
            };

            // Send message
            const response = await admin.messaging().send(message);
            functions.logger.info(`FCM sent to ${deviceId}: ${response}`);

            // Update command status
            await snap.ref.update({
                status: 'sent',
                fcmMessageId: response,
                sentAt: admin.firestore.FieldValue.serverTimestamp()
            });

        } catch (error) {
            functions.logger.error(`Error sending FCM to ${deviceId}:`, error);
            await snap.ref.update({
                status: 'failed',
                error: error.message,
                failedAt: admin.firestore.FieldValue.serverTimestamp()
            });
        }
    });

/**
 * Health check endpoint
 */
exports.health = functions.https.onRequest((req, res) => {
    res.status(200).json({ status: 'ok', service: 'TouchBase Functions' });
});
