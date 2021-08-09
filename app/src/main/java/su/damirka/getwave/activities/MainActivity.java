package su.damirka.getwave.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

import su.damirka.getwave.Application;
import su.damirka.getwave.R;
import su.damirka.getwave.music.MusicService;

public class MainActivity extends AppCompatActivity
{
    private static Application App;
    private static Context ApplicationContext;
    private static MusicService _MusicService;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            _MusicService = new MusicService();
            ApplicationContext = this.getApplicationContext();
            //startForegroundService(_MusicServiceIntent);
            App = new Application(this);
        } catch (Exception e) {
            onDestroy();
            e.printStackTrace();
        }
    }

    BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle Msg = intent.getExtras();

            if(Objects.isNull(Msg))
                return;

            try {
                switch (Msg.getString("Msg"))
                {
                    case "UpdateUI":
                        App.UpdateSongMenu(Msg.getInt("Index"));
                        break;
                    case "Playing":
                        App.UpdateProgressbar(Msg.getInt("Position"), Msg.getInt("Duration"));
                        break;
                    case "CurrentState":
                        App.UpdateAppStates(Msg);
                        break;
                }
            } catch (NullPointerException nullPointerException)
            {
                nullPointerException.printStackTrace();
            }
        }
    };

    @Override
    protected void onResume()
    {
        super.onResume();
        IntentFilter recv = new IntentFilter("UpdateUI");
        this.registerReceiver(br, recv);
        App.Resume();
    }

    public static Application GetApp()
    {
        return App;
    }

    public static void SendMsgToMusicService(Bundle Msg)
    {
        Intent MusicServiceIntent = new Intent(ApplicationContext, _MusicService.getClass());
        App.SendMsgToMusicService(Msg, MusicServiceIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent MusicServiceIntent = new Intent(ApplicationContext, _MusicService.getClass());
        startForegroundService(MusicServiceIntent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(br);
        App.Pause();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Intent MusicServiceIntent = new Intent(ApplicationContext, _MusicService.getClass());
        stopService(MusicServiceIntent);
        App.Exit();
    }

}