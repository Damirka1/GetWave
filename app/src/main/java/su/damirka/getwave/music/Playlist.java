package su.damirka.getwave.music;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Playlist implements Parcelable
{
    private final Song[] Songs;
    private long CurrentSelected = -1;

    public Playlist()
    {
        Songs = null;
    }

    public Playlist(Song[] Songs)
    {
        this.Songs = Songs;
    }

    public Playlist(Track[] tracks)
    {
        Songs = new Song[tracks.length];

        for(int i = 0, j = tracks.length - 1; i < tracks.length; i++, j--)
            Songs[i] = new Song(tracks[j]);
    }

    protected Playlist(Parcel in) {
        Songs = in.createTypedArray(Song.CREATOR);
        CurrentSelected = in.readLong();
    }

    public static final Creator<Playlist> CREATOR = new Creator<Playlist>() {
        @Override
        public Playlist createFromParcel(Parcel in) {
            return new Playlist(in);
        }

        @Override
        public Playlist[] newArray(int size) {
            return new Playlist[size];
        }
    };

    public int GetSize()
    {
        return Songs.length;
    }

    public long GetCurrentSelected()
    {
        return CurrentSelected;
    }

    public Song GetSongByIndexWithoutSelecting(long Index)
    {
        if(Index > Songs.length - 1)
        {
            return Songs[0];
        }
        else if(Index < 0)
        {
            return Songs[Songs.length - 1];
        }
        return Songs[(int) Index];
    }

    public Song GetSongByIndex(long Index)
    {
        if(Index > Songs.length - 1)
        {
            CurrentSelected = Songs.length - 1;
            return Songs[(int) CurrentSelected];
        }
        else if(Index < 0)
        {
            CurrentSelected = 0;
            return Songs[(int) CurrentSelected];
        }

        CurrentSelected = Index;
        return Songs[(int) CurrentSelected];
    }

    public void SetSelected(long Index)
    {
        CurrentSelected = Index;
    }

    public Song GetCurrSong()
    {
        return Songs[(int) CurrentSelected];
    }

    public Song GetNextSong()
    {
        if(++CurrentSelected > Songs.length - 1)
            CurrentSelected = 0;
        return Songs[(int)CurrentSelected];
    }

    public Song GetPrevSong()
    {
        if(--CurrentSelected < 0)
            CurrentSelected = Songs.length - 1;
        return Songs[(int)CurrentSelected];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedArray(Songs, flags);
        dest.writeLong(CurrentSelected);
    }
}
