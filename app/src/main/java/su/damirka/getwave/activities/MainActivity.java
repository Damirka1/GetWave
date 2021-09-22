package su.damirka.getwave.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

import su.damirka.getwave.Application;
import su.damirka.getwave.R;
import su.damirka.getwave.music.MusicService;

public class MainActivity extends AppCompatActivity
{
    private static Application App;
    private static Context ApplicationContext;

    private static MusicService.PlayerServiceBinder playerServiceBinder;
    private static MediaControllerCompat MediaController;

    private static boolean Ready = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            ApplicationContext = this.getApplicationContext();
            App = new Application(this);

            bindService(new Intent(this, MusicService.class), new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    playerServiceBinder = (MusicService.PlayerServiceBinder) service;
                    try {
                        MediaController = new MediaControllerCompat(
                                MainActivity.this, playerServiceBinder.getMediaSessionToken());
                        MediaController.registerCallback(
                                new MediaControllerCompat.Callback() {
                            @Override
                            public void onMetadataChanged(MediaMetadataCompat metadata)
                            {
                                super.onMetadataChanged(metadata);
                            }

                            @Override
                            public void onPlaybackStateChanged(PlaybackStateCompat state)
                            {
                                if (state == null || App == null)
                                    return;

                                if(Objects.nonNull(state.getExtras()))
                                {
                                    Bundle Msg = state.getExtras();

                                    if(Objects.nonNull(Msg.getString("Msg")))
                                    {
                                        String Info = Msg.getString("Msg");
                                        if(Info.equals("UpdateProgressBar"))
                                            App.UpdateProgressbar(Msg.getLong("Position"), Msg.getLong("Duration"));
//                                                else if(Info.equals("UpdatePlayList"))
//                                                    App.UpdateDefaultPlaylist(Msg);
                                        return;
                                    }
                                }
                                App.UpdateAppStates(state);
                            }
                        });
                        Ready = true;
                    }
                    catch (RemoteException e) {
                        MediaController = null;
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    playerServiceBinder = null;
                    MediaController = null;
                }
            }, BIND_AUTO_CREATE);

        } catch (Exception e) {
            onDestroy();
            e.printStackTrace();
        }
    }

    public static MediaControllerCompat GetMediaController()
    {
        return MediaController;
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        App.Resume();
    }

    public static boolean IsReady()
    {
        return Ready;
    }

    public static void StartService()
    {
        ApplicationContext.startForegroundService(new Intent(ApplicationContext, MusicService.class));
    }

    public static void StopService()
    {
        ApplicationContext.stopService(new Intent(ApplicationContext, MusicService.class));
    }

    public static Application GetApp()
    {
        return App;
    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        App.Pause();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        App.Exit();
        StopService();

        ApplicationContext = null;

        playerServiceBinder = null;
        MediaController = null;

        Ready = false;
        App = null;
    }

}