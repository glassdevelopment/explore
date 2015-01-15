package uy.edu.fing.proygrad.explore;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.LiveCard.PublishMode;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A {@link Service} that publishes a {@link LiveCard} in the timeline.
 */
public class LiveCardService extends Service {

    private static final String TAG = LiveCardService.class.getName();

    private LiveCard mLiveCard;

    private BroadcastReceiver mReceiver;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mLiveCard == null) {
            // get an instance of the receiver in your service
            mReceiver = new TokenReceiver();
            IntentFilter filter = new IntentFilter("uy.edu.fing.proygrad.explore.TOKEN_INTENT");
            registerReceiver(mReceiver, filter);

            mLiveCard = new LiveCard(this, TAG);

            RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.live_card);
            mLiveCard.setViews(remoteViews);

            // Display the options menu when the live card is tapped.
            Intent menuIntent = new Intent(this, LiveCardMenuActivity.class);
            mLiveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));
            mLiveCard.publish(PublishMode.REVEAL);

            // Start the recurring task of taking a picture
            ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);
            Runnable worker = new Runnable() {
                @Override
                public void run() {
                    System.out.println(Thread.currentThread().getName() + " Start. Time = " + new Date());
                    CameraManager.openCamera();
                    CameraManager.takePicture(LiveCardService.this);
                    System.out.println(Thread.currentThread().getName() + " End. Time = " + new Date());
                }

            };
            scheduledThreadPool.scheduleWithFixedDelay(worker, 0, 10, TimeUnit.SECONDS);

        } else {
            mLiveCard.navigate();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mLiveCard != null && mLiveCard.isPublished()) {
            unregisterReceiver(mReceiver);
            mLiveCard.unpublish();
            mLiveCard = null;
        }
        super.onDestroy();
    }

    public class TokenReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String token = intent.getStringExtra("token");
            RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.live_card);
            remoteViews.setTextViewText(R.id.main_text, token);
            mLiveCard.setViews(remoteViews);
        }

        // constructor
        public TokenReceiver(){

        }
    }
}
