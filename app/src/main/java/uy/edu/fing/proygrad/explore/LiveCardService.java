package uy.edu.fing.proygrad.explore;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.LiveCard.PublishMode;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
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
            final String token = intent.getStringExtra("token");
            Log.d(TAG, token);

            final Handler handler = new Handler();

            final Runnable r = new Runnable() {
                private Runnable me = this;

                public void run() {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url("https://camfind.p.mashape.com/image_responses/" + token)
                            .header("X-Mashape-Key", "t1ohJF4J0tmsh3ZinpXxzpqwel0Sp1QHKaUjsnz1rKK7wVbzaR")
                            .header("Accept", "application/json")
                            .build();

                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Request request, IOException e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(Response response) throws IOException {
                            String responseString = response.body().string();

                            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                            try {
                                JSONObject json = new JSONObject(responseString);
                                String status = json.getString("status");

                                if (status.equals("completed")) {
                                    String name = json.getString("name");

                                    RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.live_card);
                                    remoteViews.setTextViewText(R.id.main_text, name);
                                    mLiveCard.setViews(remoteViews);
                                } else {
                                    handler.postDelayed(me, 2000);
                                }

                            } catch (JSONException e) {
                                Log.e(TAG, e.getMessage());
                            }
                        }
                    });
                }
            };

            handler.postDelayed(r, 8001);
        }

        // constructor
        public TokenReceiver(){

        }
    }
}
