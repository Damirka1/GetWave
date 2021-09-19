package su.damirka.getwave.views.playlists;

import su.damirka.getwave.music.Playlist;

public interface Adapter
{
    public Playlist GetPlaylist();
    public int GetCount();
    public void SetName(String Name);
    public String GetName();
    public void Update(int Index);
}
