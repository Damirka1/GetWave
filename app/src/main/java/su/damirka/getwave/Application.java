package su.damirka.getwave;

import android.content.Intent;
import android.os.Bundle;

import su.damirka.getwave.activities.MainActivity;
import su.damirka.getwave.connection.ConnectionService;
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
    private boolean Sleep;
    private boolean Running;

    private native boolean Connect();

    public Application(MainActivity MA) throws Exception {
        System.loadLibrary("Client-Connection");

        if(!Connect())
            throw new Exception("Can't connect to server\n");

        Running = true;
        Sleep = false;

        UpdateThread = new Thread(this::Update);

        ConnectionService = new ConnectionService();
        WM = new WindowManager(MA);
        this.MA = MA;

        UpdateThread.start();
    }

    public void UpdateSongMenu(long Index)
    {
        WM.Update(Index);
    }

    public void UpdateProgressbar(int Position, int Duration)
    {
        WM.UpdateProgressBar(Position, Duration);
    }

    public void UpdateAppStates(Bundle States)
    {
        WM.UpdateStates(States);
    }

    private void Update()
    {
        System.out.println("UI thread started");
        while(Running)
        {
            while(Sleep)
            {
                try
                {
                    Thread.sleep(500);
                }
                catch (InterruptedException interruptedException)
                {
                    interruptedException.printStackTrace();
                }
            }

            try
            {
                Thread.sleep(250);
            }
            catch (InterruptedException interruptedException)
            {
                interruptedException.printStackTrace();
            }

            if(WM.IsPlaying())
            {
                Bundle Msg = new Bundle();
                Msg.putString("Msg", "UpdateProgressBar");
                MainActivity.SendMsgToMusicService(Msg);
            }
        }
    }

    public void SendMsgToMusicService(Bundle Msg, Intent MusicIntent)
    {
        MusicIntent.putExtras(Msg);
        MA.startForegroundService(MusicIntent);
    }

    public Song GetSongByIndex(long Index)
    {
        return WM.GetSongByIndex(Index);
    }

    public void Resume()
    {
        if(Sleep)
        {
            Bundle Msg = new Bundle();
            Msg.putString("Msg", "UpdateUI");
            MainActivity.SendMsgToMusicService(Msg);

            Msg = new Bundle();
            Msg.putString("Msg", "GetStates");
            MainActivity.SendMsgToMusicService(Msg);
        }
        Sleep = false;
    }

    public void Pause()
    {
        Sleep = true;
    }

    public void Exit() {
        WM.Release();
        Running = false;
        Sleep = false;

        try {
            UpdateThread.join();
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }

    }

}
