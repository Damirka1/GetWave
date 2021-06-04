package su.damirka.getwave;

import java.io.IOException;

public class Application implements Runnable
{
    private WindowManager WM;
    private MusicPlayer MP;
    private Playlist PL;

    private boolean Execute;
    private Thread AppThread;

    public Application(MainActivity MA) throws IOException
    {
        WM = new WindowManager(this, MA);
        MP = new MusicPlayer(this, MA);

        Execute = true;
        AppThread = new Thread(this);
        AppThread.start();
    }

    public void PlaySong(Songs.Song s) throws IOException
    {
        PL.PlaySong(s, MP);
    }

    public void PlayNext() throws IOException
    {
        PL.PlayNext(MP);
    }

    public void PlayPrev() throws IOException
    {
        PL.PlayPrev(MP);
    }

    public void SetPlaylist(Playlist PL)
    {
        this.PL = PL;
    }

    public void Pause()
    {
        MP.Pause();
    }

    public void Stop()
    {
        MP.Stop();
    }

    public void Play()
    {
        MP.Play();
    }

    public void Exit()
    {
        Execute = false;
        WM.Release();
        try
        {
            AppThread.join();
        }
        catch (InterruptedException ex)
        {
            ex.printStackTrace();
        }
    }

    public void ShowSongMenu(Songs.Song s)
    {
        WM.ShowSongMenu(s);
    }

    public void Update(Songs.Song s, int MaxDuration)
    {
        PL.Update();
        WM.UpdateSongMenu(s, MaxDuration);

        if(SongActivity.IsVisible())
            SongActivity.Update(s, MaxDuration);
    }

    public MusicPlayer GetMusicPlayer()
    {
        return MP;
    }

    @Override
    public void run()
    {
        while (Execute)
        {
            if(MP.Playing)
            {
                int Duration = MP.GetCurrPos();
                WM.UpdateSongBar(Duration);

                if(SongActivity.IsVisible())
                    SongActivity.UpdateBar(Duration);
            }
            Sleep(100);
        }
    }

    private void Sleep(int ms)
    {
        try
        {
            Thread.sleep(ms);
        }
        catch (InterruptedException ex)
        {
            ex.printStackTrace();
        }
    }
}
