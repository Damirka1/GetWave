package su.damirka.getwave.files;

public class TrackCacheFile extends CacheFile
{
    private final long Iterator;

    public TrackCacheFile(long Id, byte[] pData, long Iterator)
    {
        super(Id, pData);
        this.Iterator = Iterator;
    }

    public long GetIterator()
    {
        return Iterator;
    }
}
