package su.damirka.getwave.music;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaDataSource;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.media.session.MediaControllerCompat;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import su.damirka.getwave.activities.MainActivity;

public class MusicPlayer implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {

    private MediaPlayer MP;
    private AudioManager AM;

    private boolean Repeat;
    private boolean Ready;
    private boolean Playing;

    private Song CurrentSong;
    private StreamMediaDataSource CurrentStream;

    private Playlist Playlist;

    private final MediaControllerCompat.TransportControls Controller;

    public MusicPlayer(Context context, MediaControllerCompat.TransportControls Controller)
    {
        Repeat = false;
        Ready = false;
        Playing = false;

        this.Controller = Controller;

        MP = new MediaPlayer();
        MP.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );

        MP.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);

        MP.setOnCompletionListener(this);
        MP.setOnPreparedListener(this);
    }

    public Playlist GetPlaylist()
    {
        return Playlist;
    }

    public void SetVolume(float Left, float Right)
    {
        MP.setVolume(Left, Right);
    }

    public int GetPosition()
    {
        return MP.getCurrentPosition();
    }

    public int GetDuration()
    {
        return MP.getDuration();
    }

    public long GetCurrentIndex() { return Playlist.GetCurrentSelected(); }

    public void SetPlaylist(Playlist Playlist)
    {
        this.Playlist = Playlist;
    }

    public void SetRepeat(boolean Flag)
    {
        Repeat = Flag;
    }

    public Song GetCurrentSong()
    {
        return CurrentSong;
    }

    public void PlayAt(int Index)
    {
        PlaySong(Playlist.GetSongByIndex(Index));
    }

    public boolean IsRepeat() { return Repeat; }

    public boolean IsReady()
    {
        return Ready;
    }

    public boolean IsPlaying() { return Playing; }

    private void PlaySong(Song song)  {

        CurrentSong = song;
        StreamMediaDataSource another = CurrentSong.GetStream();

        if(Ready)
            Stop();

        if(Objects.nonNull(CurrentStream))
        {
            if(!CurrentStream.Equals(another))
            {
                CurrentStream.CloseAsync();
                CurrentStream = another;
            }
        }
        else
            CurrentStream = another;

        CurrentStream.PrepareASync();
        MP.setDataSource(CurrentStream);
        MP.prepareAsync();
    }

    public void Play()
    {
        if(!Playing)
        {
            Playing = true;
            MP.start();
        }
    }

    public void PlayNext()
    {
        PlaySong(Playlist.GetNextSong());
    }

    public void PlayPrev()
    {
        PlaySong(Playlist.GetPrevSong());
    }

    public void Pause()
    {
        Playing = false;
        MP.pause();
    }

    public void Stop()
    {
        Playing = false;
        Ready = false;
        MP.stop();
        MP.reset();
    }

    public void SeekTo(long Pos)
    {
        if(Pos < CurrentStream.GetIterator())
            MP.seekTo((int) Pos);
    }

    public void Release()
    {
        Stop();

        if(Objects.nonNull(CurrentStream))
            CurrentStream.CloseAsync();

        MP.release();
        MP = null;
        AM = null;
        Playlist = null;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(!Playing)
            return;

        if(Repeat)
            PlayAt((int) Playlist.GetCurrentSelected());
        else
            Controller.skipToNext();
    }

    @Override
    public void onPrepared(MediaPlayer mp)
    {
        Ready = true;
        Controller.play();
    }

}
