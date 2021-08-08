package su.damirka.getwave.music;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Builder;

import java.util.Objects;

import su.damirka.getwave.R;
import su.damirka.getwave.activities.MainActivity;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

public class MusicService extends Service
{
    private MusicPlayer MusicPlayer;

    private Notification _Notification;
    private int NotificationId = 123;
    private NotificationManager _NotificationManager;

    private Looper serviceLooper;
    private ServiceHandler serviceHandler;

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {

            Bundle bundle = msg.getData();
            String Message = "";
            if(Objects.nonNull(bundle.getString("Msg")))
                Message = bundle.getString("Msg");

            Intent intent = new Intent();
            Bundle Msg = new Bundle();

            switch (Message)
            {
                case "Play":
                    MusicPlayer.Play();
                    break;
                case "PlayNext":
                    MusicPlayer.PlayNext();
                    Msg.putString("Msg", "UpdateUI");
                    Msg.putInt("Index",(int) MusicPlayer.GetCurrentIndex());
                    Msg.putInt("Duration", MusicPlayer.GetDuration());
                    intent.setAction("UpdateUI");
                    break;
                case "PlayPrev":
                    MusicPlayer.PlayPrev();
                    Msg.putString("Msg", "UpdateUI");
                    Msg.putInt("Index",(int) MusicPlayer.GetCurrentIndex());
                    Msg.putInt("Duration", MusicPlayer.GetDuration());
                    intent.setAction("UpdateUI");
                    break;
                case "PlayOther":
                {
                    int index = bundle.getInt("Index");
                    MusicPlayer.PlayAt(index);
                    Msg.putString("Msg", "UpdateUI");
                    Msg.putInt("Duration", MusicPlayer.GetDuration());
                    Msg.putInt("Index", index);
                    intent.setAction("UpdateUI");
                }
                    break;
                case "Stop":
                    MusicPlayer.Stop();
                    break;
                case "Pause":
                    MusicPlayer.Pause();
                    break;
                case "Playlist":
                    MusicPlayer.SetPlaylist(bundle.getParcelable("Playlist"));
                    break;
                case "Release":
                    MusicPlayer.Release();
                    break;
                case "UpdateProgressBar":
                    Msg.putString("Msg", "Playing");
                    Msg.putInt("Position", MusicPlayer.GetPosition());
                    intent.setAction("UpdateUI");
                    break;

            }
            intent.putExtras(Msg);

            sendBroadcast(intent);
        }
    }
    
    private void Update()
    {
        System.out.println("UI thread started");
        while(true)
        {
            try {
                Thread.sleep(100);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }


        }
    }

    private void Initialize()
    {
        MusicPlayer = new MusicPlayer();
    }

    @Override
    public void onCreate()
    {
        Initialize();

        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                THREAD_PRIORITY_BACKGROUND);
        thread.start();

        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);
    }

    private void GenerateNotification()
    {
        Intent intentMainLanding = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, intentMainLanding, 0);
        if (_NotificationManager == null) {
            _NotificationManager = (android.app.NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        _NotificationManager.createNotificationChannelGroup(new NotificationChannelGroup("chats_group", "Chats"));
        NotificationChannel NotificationChannel = new NotificationChannel("service_channel", "Service Notifications",
                NotificationManager.IMPORTANCE_MIN);

        NotificationChannel.enableLights(false);
        NotificationChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
        _NotificationManager.createNotificationChannel(NotificationChannel);


        Builder builder = new NotificationCompat.Builder(this, "service_channel");

        builder.setContentTitle(R.string.app_name + " service is running")
                .setTicker(R.string.app_name + "service is running")
                .setContentText("Touch to open")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setWhen(0)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .setOngoing(true);

        builder.setColor(getResources().getColor(R.color.purple_200, null));
        _Notification = builder.build();
        startForeground(NotificationId, _Notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Message msg = serviceHandler.obtainMessage();
        msg.arg1 = startId;
        Bundle bundle = intent.getExtras();
        msg.setData(bundle);

        serviceHandler.sendMessage(msg);
        try
        {
            if(Objects.nonNull(bundle))
            {
                if(Objects.nonNull(bundle.getString("Msg")))
                {
                    if(bundle.getString("Msg").equals("Release"))
                        onDestroy();
                }
            }
            else
                GenerateNotification();
        } catch (NullPointerException nullPointerException)
        {
            nullPointerException.printStackTrace();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        serviceLooper.quit();
        MusicPlayer.Release();
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
