package su.damirka.getwave.files;

public class CacheManager
{
    private final int TrackCacheMaxSize = 262144; // 256 Mb.
    private final int ArtCacheMaxSize = 262144; // 256 Mb.
    private FileManager FM;

    public CacheManager(FileManager FM)
    {
        this.FM  = FM;
    }

    public void SaveAsTrackCache(TrackCacheFile file)
    {
        FM.SaveObj(file, "Track" + file.GetId());
    }

    public TrackCacheFile LoadTrackCache(long Id)
    {
        return (TrackCacheFile) FM.ReadObj("Track" + Id);
    }

    public void SaveAsArtCache(CacheFile file)
    {
        FM.SaveObj(file, "Art" + file.GetId());
    }

    public CacheFile LoadArtCache(long Id)
    {
        return (CacheFile) FM.ReadObj("Art" + Id);
    }

}
