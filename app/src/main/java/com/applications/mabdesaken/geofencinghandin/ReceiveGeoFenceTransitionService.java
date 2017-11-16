package dk.au.cs.is2017.banegaardfence;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.applications.mabdesaken.geofencinghandin.MainActivity;
import com.applications.mabdesaken.geofencinghandin.R;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class ReceiveGeoFenceTransitionService extends IntentService {

    // Notification channel ID, needed for API 26 and higher
    public final static String CHANNEL_ID = "default";

    public ReceiveGeoFenceTransitionService() {
        super("ReceiveGeoFenceTransitionService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);

        if (event.hasError()) {
            // TODO: Handle error
        } else {
            int transition = event.getGeofenceTransition();

            if (transition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                String transitionType = getTransitionString(transition);

                Log.d(MainActivity.TAG, getString(R.string.geofence_transition_notification_title, transitionType));

                // Send a notification, when clicked, open website
                String url = "https://www.rejseplanen.dk/webapp/index.html?language=en_EN&#!S|Aarhus%20H!Z|%C3%85bogade%2034%2C%208200%20Aarhus%20N%2C%20Aarhus%20Kommune!start|1";
                Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
                notificationIntent.setData(Uri.parse(url));

                PendingIntent contentIntent = PendingIntent.getActivity(this.getApplicationContext(), 0, notificationIntent, 0);

                // Create a notification channel if on API 26 (Android O) and above
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "Default Notifications", NotificationManager.IMPORTANCE_DEFAULT);

                    // Configure the notification channel.
                    notificationChannel.setDescription("Channel description");
                    notificationChannel.enableLights(true);
                    notificationChannel.setLightColor(Color.RED);
                    notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
                    notificationChannel.enableVibration(true);
                    notificationManager.createNotificationChannel(notificationChannel);
                }

                Notification notification = new NotificationCompat.Builder(this.getApplicationContext(), CHANNEL_ID)
                        .setContentTitle("Near Aarhus H!")
                        .setContentText("Check out how to get to the Datalogi building!")
                        .setTicker("You're near the Hovedbanegaard. Do you want to see how to get to the CS building?")
                        .setContentIntent(contentIntent)
                        .setSmallIcon(R.drawable.stat_sys_gps_on)
                        .setAutoCancel(true)
                        .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.rejse))
                        .build();

                Log.d(MainActivity.TAG, "Notification created");

                NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
                manager.notify(1, notification);

                Log.d(MainActivity.TAG, "Notified!");
            } else {
                // TODO: Handle invalid transition
            }
        }
    }

    /**
     * Maps geofence transition types to their human-readable equivalents.
     *
     * @param transitionType A transition type constant defined in Geofence
     * @return A String indicating the type of transition
     */
    private String getTransitionString(int transitionType) {
        switch (transitionType) {

            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return getString(R.string.geofence_transition_entered);

            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return getString(R.string.geofence_transition_exited);

            default:
                return getString(R.string.geofence_transition_unknown);
        }
    }
}