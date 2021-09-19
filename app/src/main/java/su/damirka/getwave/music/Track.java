package su.damirka.getwave.music;

import java.io.Serializable;

public class Track implements Serializable
{
    protected int Id;
    protected String Path;
    protected String Title;
    protected String Author;

    public int GetId()
    {
        return Id;
    }
}
