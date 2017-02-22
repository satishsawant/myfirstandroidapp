package realizer.com.chat;

/**
 * Created by Win on 05/12/2016.
 */
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import realizer.com.chat.chat.ChatMessageCenterActicity;
import realizer.com.chat.chat.ChatThreadListActivity;
import realizer.com.chat.utils.Singleton;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //Displaying data in log
        //It is optional
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());

        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String threadId=remoteMessage.getData().get("threadId");
        String participentId=remoteMessage.getData().get("participantId");
        String initiateId=remoteMessage.getData().get("initiateId");
        String ReceiverURL=remoteMessage.getData().get("senderThumbanilUrl");
        String ReceiverTime=remoteMessage.getData().get("timeStamp");

        //Calling method to generate notification

        String ActiveThred=sharedpreferences.getString("ActiveThread", "");
        String LogCheck=sharedpreferences.getString("Login", "");
        if (ActiveThred.equals(threadId))
        {
            Singleton obj = Singleton.getInstance();
            if(obj.getResultReceiver() != null)
            {
                Bundle b=new Bundle();
                b.putString("ReceiverURL",ReceiverURL);
                b.putString("ReceiveTime",ReceiverTime);
                b.putString("ReceiveMSG",remoteMessage.getNotification().getBody());
                b.putString("ReceiverNAME",remoteMessage.getNotification().getTitle());
                obj.getResultReceiver().send(100, b);
            }
        }
        else if (LogCheck.equals("true"))
        {
            Singleton obj = Singleton.getInstance();
            if(obj.getResultReceiver() != null)
            {
                obj.getResultReceiver().send(200, null);
            }
            sendNotification(remoteMessage.getNotification().getBody(), remoteMessage.getNotification().getTitle(), threadId, participentId, initiateId);
           /* SharedPreferences.Editor edit = sharedpreferences.edit();
            edit.putString("ActiveThread", threadId);
            edit.commit();*/

        }
        else
        {

        }
    }

    //This method is only generating push notification
    //It is same as we did in earlier posts
    private void sendNotification(String messageBody,String Title,String threadId,String participentId,String InitiateId) {
        Intent intent = new Intent(this, ChatMessageCenterActicity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Bundle bundle = new Bundle();
        bundle.putString("THREADID",threadId );
        bundle.putString("RECEIVERID",participentId);
        bundle.putString("InitiatedID", InitiateId);
        bundle.putString("ActionBarTitle",Title);
        intent.putExtras(bundle);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.noti_ico)
                .setContentTitle(Title)
                .setColor(Color.rgb(107,178,211))
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }
}