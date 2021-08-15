package su.damirka.getwave;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import java.util.Objects;

import su.damirka.getwave.activities.MainActivity;
import su.damirka.getwave.connection.ConnectionService;
import su.damirka.getwave.music.Playlist;
import su.damirka.getwave.music.Song;
import windows.WindowManager;

public class Application
{
//    public String ip = "192.168.1.30";
//    public String port = "25565";

    private final WindowManager WM;
    private final MainActivity MA;
    private final ConnectionService ConnectionService;

    private final Thread UpdateThread;
    private boolean Running;
    private boolean Sleep;

    private native boolean Connect();

    public Application(MainActivity MA) throws Exception {
        System.loadLibrary("Client-Connection");

        if(!Connect())
            throw new Exception("Can't connect to server\n");

        ConnectionService = new ConnectionService();
        WM = new WindowManager(MA);
        this.MA = MA;

        Sleep = false;
        Running = true;
        UpdateThread = new Thread(this::Update);
        UpdateThread.start();
    }

    private void Update()
    {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {}

        while(Running)
        {
            while(Sleep)
            {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {}
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {}

            MainActivity.GetMediaController().getTransportControls().sendCustomAction("UpdateProgressBar", null);
        }
    }

    public void Pause()
    {
        Sleep = true;
    }

    public void Resume()
    {
        Sleep = false;
        if(Objects.nonNull(MainActivity.GetMediaController()))
            MainActivity.GetMediaController().getTransportControls().sendCustomAction("UpdateStates", null);
    }

    public void UpdateProgressbar(long Position, long Duration)
    {
        WM.UpdateProgressBar((int) Position, (int) Duration);
    }

    public void UpdateDefaultPlaylist(Bundle Msg)
    {
        WM.UpdateDefaultPlaylist(Msg);
    }

    public void UpdateAppStates(PlaybackStateCompat States)
    {
        WM.UpdateStates(States);
    }

    public void Exit() {
        WM.Release();
        Running = false;
        Sleep = false;
        try {
            UpdateThread.join();
        } catch (InterruptedException ignore) {}

    }

}
