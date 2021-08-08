package su.damirka.getwave.music;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class Song implements Parcelable
{
    private StreamMediaDataSource Stream;
    private MetaMediaDataSource MetaStream;
    private String Url;

    private String Title;
    private String Author;
    private Bitmap Art;
    private boolean HasArt;


    protected Song(Parcel in) {
        Url = in.readString();
        Title = in.readString();
        Author = in.readString();
        Art = in.readParcelable(Bitmap.class.getClassLoader());
        HasArt = in.readByte() != 0;
        Stream = new StreamMediaDataSource(Url);
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

    public void SetTitle(String Title)
    {
        this.Title = Title;
    }

    public void SetAuthor(String Author)
    {
        this.Author = Author;
    }

    public void SetArt(Bitmap Art)
    {
        HasArt = true;
        this.Art = Art;
    }

    public String GetTitle()
    {
        return Title;
    }

    public String GetAuthor()
    {
        return Author;
    }

    public Bitmap GetArt()
    {
        return Art;
    }

    public boolean HasArt()
    {
        return HasArt;
    }

    public Song(String Url)
    {
        this.Url = Url;
        Stream = new StreamMediaDataSource(Url);
        MetaStream = new MetaMediaDataSource(Url);
        HasArt = false;
    }

    public StreamMediaDataSource GetStream()
    {
        return Stream;
    }

    public MetaMediaDataSource GetMetaStream()
    {
        return MetaStream;
    }

    public String GetUrl()
    {
        return Url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Url);
        dest.writeString(Title);
        dest.writeString(Author);
        dest.writeParcelable(Art, flags);
        dest.writeByte((byte) (HasArt ? 1 : 0));
    }
}
