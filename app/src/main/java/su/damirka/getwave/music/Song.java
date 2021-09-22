package su.damirka.getwave.music;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import su.damirka.getwave.files.TrackCacheFile;

public class Song implements Parcelable
{
    private final StreamMediaDataSource Stream;
    private final Track TrackInfo;
    private Bitmap Art;
    private boolean HasArt;
    private boolean Initialized;

    public void SetInitialized()
    {
        Initialized = true;
    }

    public boolean GetInitialized()
    {
        return Initialized;
    }

    protected Song(Parcel in) {
        TrackInfo = new Track();
        TrackInfo.Id = in.readLong();
        TrackInfo.Path = in.readString();
        TrackInfo.Title = in.readString();
        TrackInfo.Author = in.readString();

        Art = in.readParcelable(Bitmap.class.getClassLoader());
        HasArt = in.readByte() != 0;
        Stream = new StreamMediaDataSource(TrackInfo.Path, TrackInfo.Id);
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    public void SetArt(Bitmap Art)
    {
        HasArt = true;
        this.Art = Art;
    }

    public String GetTitle()
    {
        return TrackInfo.Title;
    }

    public String GetAuthor()
    {
        return TrackInfo.Author;
    }

    public Bitmap GetArt()
    {
        return Art;
    }

    public boolean HasArt()
    {
        return HasArt;
    }

    public Song(Track track)
    {
        TrackInfo = track;
        Stream = new StreamMediaDataSource(TrackInfo.Path, track.Id);
        HasArt = false;
    }

    public StreamMediaDataSource GetStream()
    {
        return Stream;
    }

    public String GetUrl()
    {
        return TrackInfo.Path;
    }

    public Track GetTrack()
    {
        return TrackInfo;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(TrackInfo.Id);
        dest.writeString(TrackInfo.Path);
        dest.writeString(TrackInfo.Title);
        dest.writeString(TrackInfo.Author);
        dest.writeParcelable(Art, flags);
        dest.writeByte((byte) (HasArt ? 1 : 0));
    }
}
