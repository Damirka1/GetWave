package su.damirka.getwave.files;

import java.io.Serializable;

public class CacheFile implements Serializable
{
    private final long Id;
    private final byte[] pData;

    public CacheFile(long Id, byte[] pData)
    {
        this.Id = Id;
        this.pData = pData;
    }

    public long GetId()
    {
        return Id;
    }

    public byte[] GetData()
    {
        return pData;
    }

}
