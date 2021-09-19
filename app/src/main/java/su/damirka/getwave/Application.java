package su.damirka.getwave;

import android.os.Bundle;
import android.support.v4.media.session.PlaybackStateCompat;

import java.util.Objects;

import su.damirka.getwave.activities.MainActivity;
import su.damirka.getwave.connection.ConnectionService;
import su.damirka.getwave.files.FileManager;
import su.damirka.getwave.views.playlists.Adapter;
import windows.WindowManager;

public class Application
{
//    public String ip = "192.168.1.30";
//    public String port = "25565";

    private final FileManager FM;
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

        FM = new FileManager(MA);

        ConnectionService = new ConnectionService();
        WM = new WindowManager(MA);
        this.MA = MA;

        Sleep = false;
        Running = true;
        UpdateThread = new Thread(this::Update);
        UpdateThread.start();
    }

    public FileManager GetFileManager()
    {
        return FM;
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

    private Adapter CurrentPlaylist;

    public void SetPlaylist(Adapter playlist)
    {
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

    public void Exit() {
        WM.Release();
        Running = false;
        Sleep = false;
        try {
            UpdateThread.join();
        } catch (InterruptedException ignore) {}

    }

}
