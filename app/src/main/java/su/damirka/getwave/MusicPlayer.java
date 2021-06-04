package su.damirka.getwave;

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;

import java.io.IOException;

public class MusicPlayer implements MediaPlayer.OnPreparedListener
{
    private Application App;
    private MediaPlayer MP;
    private AudioManager AM;
    private boolean Ready = false;

    public boolean Playing = false;
    public boolean Repeat = false;

    private Songs.Song Song;

    MusicPlayer(Application App, MainActivity MA)
    {
        this.App = App;

        MP = new MediaPlayer();
        MP.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );

        MP.setOnCompletionListener(mp -> {
            if(!Repeat)
            {
                try
                {
                    App.PlayNext();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            else
            {
                try
                {
                    App.PlaySong(Song);
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    public void Play(Songs.Song s) throws IOException
    {
        Stop();
        this.Song = s;
        MP.setDataSource(s.GetPath());
        MP.setOnPreparedListener(this);
        MP.prepareAsync();
    }

    public Songs.Song GetSong()
    {
        return Song;
    }

    public int GetCurrPos()
    {
        return MP.getCurrentPosition();
    }

    public int GetDuration()
    {
        return MP.getDuration();
    }

    public void Play()
    {
        if(Ready)
        {
            MP.start();
            Playing = true;
        }
    }

    public void Pause()
    {
        MP.pause();
        Playing = false;
    }

    public void Stop()
    {
        MP.stop();
        MP.reset();
        Playing = false;
        Ready = false;
    }

    public void SeekTo(int Pos)
    {
        MP.seekTo(Pos);
    }

    public void finalize()
    {
        Stop();
        MP.release();
        MP = null;
    }

    @Override
    public void onPrepared(MediaPlayer mp)
    {
        Ready = true;
        Playing = true;
        mp.start();
        App.Update(Song, mp.getDuration());
    }
}
