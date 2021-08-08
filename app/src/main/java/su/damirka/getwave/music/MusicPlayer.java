package su.damirka.getwave.music;

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaDataSource;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.os.Bundle;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import su.damirka.getwave.activities.MainActivity;

public class MusicPlayer implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {

    private MediaPlayer MP;
    private AudioManager AM;
    private MediaExtractor ME;

    private boolean Repeat;
    private boolean Ready;
    private boolean Playing;
    private long Duration;

    private StreamMediaDataSource CurrentStream;

    private Playlist Playlist;

    public MusicPlayer()
    {
        Repeat = false;
        Ready = false;
        Playing = false;

        ME = new MediaExtractor();

        MP = new MediaPlayer();
        MP.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );

        MP.setOnCompletionListener(this);
        MP.setOnPreparedListener(this);
    }

    public int GetPosition() {
        if(Objects.nonNull(CurrentStream))
        {
            try {
                MediaFormat mf = ME.getTrackFormat(0);
                if (Objects.nonNull(mf)) {
                    int Position = MP.getCurrentPosition();
                    int BitRate = mf.getInteger(MediaFormat.KEY_BIT_RATE) / 8 / 1000;
                    return Position * BitRate;
                }
            } catch (IllegalArgumentException ignored) {}
        }
        return 0;
    }

    public int GetDuration()
    {
        return (int) CurrentStream.GetFileSize();
    }

    public long GetCurrentIndex() { return Playlist.GetCurrentSelected(); }

    public void SetPlaylist(Playlist Playlist)
    {
        this.Playlist = Playlist;
    }

    public void PlayAt(int Index)
    {
        Song song = Playlist.GetSongByIndex(Index);
        PlaySong(song);
    }

    public boolean IsReady()
    {
        return Ready;
    }

    public boolean IsPlaying() { return Playing; }

    private void PlaySong(Song song){

        StreamMediaDataSource another = song.GetStream();

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

        try {
            MP.setDataSource(CurrentStream);
            MP.prepareAsync();
            ME.setDataSource(CurrentStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public void SeekTo(int Pos)
    {
        MP.seekTo(Pos);
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

        Bundle Msg = new Bundle();
        if(Repeat)
            PlayAt((int) Playlist.GetCurrentSelected());
        else
            Msg.putString("Msg", "PlayNext");

        MainActivity.SendMsgToMusicService(Msg);
    }

    @Override
    public void onPrepared(MediaPlayer mp)
    {
        Ready = true;
        Playing = true;
        mp.start();
    }
}
