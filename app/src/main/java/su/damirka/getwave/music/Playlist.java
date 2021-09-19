package su.damirka.getwave.music;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Playlist implements Parcelable
{
    private String Name = "";
    private final List<Song> Songs;
    private long CurrentSelected = -1;

    public Playlist()
    {
        Songs = null;
    }

    public Playlist(Song[] Songs)
    {
        this.Songs = Arrays.asList(Songs);
    }

    public Playlist(Track[] tracks)
    {
        Songs = new ArrayList<>(tracks.length);

        for(int i = 0, j = tracks.length - 1; i < tracks.length; i++, j--)
            Songs.add(new Song(tracks[j]));
    }

    public Playlist(List<Track> tracks)
    {
        Songs = new ArrayList<>(tracks.size());

        for(int i = 0, j = tracks.size() - 1; i < tracks.size(); i++, j--)
            Songs.add(new Song(tracks.get(j)));
    }

    public void SetName(String Name)
    {
        this.Name = Name;
    }

    public String GetName()
    {
        return Name;
    }

    protected Playlist(Parcel in) {
        Songs = in.createTypedArrayList(Song.CREATOR);
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
        return Songs.size();
    }

    public long GetCurrentSelected()
    {
        return CurrentSelected;
    }

    public Song GetSongByIndexWithoutSelecting(long Index)
    {
        if(Index > Songs.size() - 1)
        {
            return Songs.get(0);
        }
        else if(Index < 0)
        {
            return Songs.get(Songs.size() - 1);
        }
        return Songs.get((int) Index);
    }

    public Song GetSongByIndex(long Index)
    {
        if(Index > Songs.size() - 1)
        {
            CurrentSelected = Songs.size() - 1;
            return Songs.get((int) CurrentSelected);
        }
        else if(Index < 0)
        {
            CurrentSelected = 0;
            return Songs.get((int) CurrentSelected);
        }

        CurrentSelected = Index;
        return Songs.get((int) CurrentSelected);
    }

    public void SetSelected(long Index)
    {
        CurrentSelected = Index;
    }

    public Song GetCurrSong()
    {
        return Songs.get((int) CurrentSelected);
    }

    public Song GetNextSong()
    {
        if(++CurrentSelected > Songs.size() - 1)
            CurrentSelected = 0;
        return Songs.get((int) CurrentSelected);
    }

    public Song GetPrevSong()
    {
        if(--CurrentSelected < 0)
            CurrentSelected = Songs.size() - 1;
        return Songs.get((int) CurrentSelected);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(Songs);
        dest.writeLong(CurrentSelected);
    }
}
