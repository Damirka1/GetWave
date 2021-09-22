package su.damirka.getwave.music;

import java.io.Serializable;

public class Track implements Serializable
{
    protected long Id;
    protected String Path;
    protected String Title;
    protected String Author;

    public long GetId()
    {
        return Id;
    }
}
