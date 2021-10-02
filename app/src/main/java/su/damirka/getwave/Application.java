package su.damirka.getwave;

import android.os.Bundle;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.Toast;

import java.util.Objects;

import su.damirka.getwave.activities.MainActivity;
import su.damirka.getwave.connection.ConnectionService;
import su.damirka.getwave.files.CacheFile;
import su.damirka.getwave.files.CacheManager;
import su.damirka.getwave.files.FileManager;
import su.damirka.getwave.views.playlists.Adapter;
import windows.WindowManager;

public class Application
{
//    public String ip = "192.168.1.30";
//    public String port = "25565";

    private final FileManager FM;
    private final CacheManager CM;
    private final WindowManager WM;
    private final ConnectionService ConnectionService;

    private final Thread UpdateThread;
    private boolean Running;
    private boolean Sleep;



    public Application(MainActivity MA) throws Exception {
        System.loadLibrary("Client-Connection");

        FM = new FileManager(MA);
        CM = new CacheManager(FM);

        ConnectionService = new ConnectionService(MA);
        WM = new WindowManager(MA);

        Sleep = false;
        Running = true;
        UpdateThread = new Thread(this::Update);
        UpdateThread.start();
    }

    public FileManager GetFileManager()
    {
        return FM;
    }
    public CacheManager GetCacheManager() { return CM; }

    private void Update()
    {
        while(!MainActivity.IsReady())
        {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {}
        }

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
        if(Objects.nonNull(MainActivity.GetMediaController()) && MainActivity.IsReady())
            MainActivity.GetMediaController().getTransportControls().sendCustomAction("UpdateStates", null);
    }

    public void UpdateProgressbar(long Position, long Duration)
    {
        WM.UpdateProgressBar((int) Position, (int) Duration);
    }

    private Adapter CurrentPlaylist;

    public void SetPlaylist(Adapter playlist)
    {
        MainActivity.StartService();
        if(Objects.isNull(CurrentPlaylist) || !playlist.GetName().equals(CurrentPlaylist.GetName()) || playlist.GetCount() != CurrentPlaylist.GetCount())
        {
            Bundle Msg = new Bundle();
            Msg.putParcelable("Playlist", playlist.GetPlaylist());
            MainActivity.GetMediaController().getTransportControls().prepareFromMediaId("Playlist", Msg);
            CurrentPlaylist = playlist;
        }
    }

    public void UpdateAppStates(PlaybackStateCompat States)
    {
        WM.UpdateStates(States);
        if(Objects.nonNull(CurrentPlaylist))
            CurrentPlaylist.Update((int)States.getExtras().getLong("Index"));
    }

    public void Exit()
    {
        WM.Release();
        Running = false;
        Sleep = false;
        try {
            UpdateThread.join();
            Thread.sleep(2000);
        } catch (InterruptedException ignore) {}

    }

}
