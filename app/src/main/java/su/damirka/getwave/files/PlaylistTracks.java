package su.damirka.getwave.files;

import java.io.Serializable;
import java.util.ArrayList;

import su.damirka.getwave.music.Track;

public class PlaylistTracks implements Serializable
{
    private final ArrayList<Track> Tracks;

    public PlaylistTracks()
    {
        Tracks = new ArrayList<>();
    }

    public void AddTrack(Track track)
    {
        for(Track t : Tracks)
        {
            if(track.GetId() == t.GetId())
                return;
        }

        Tracks.add(track);
    }

    public void RemoveTrack(Track track)
    {
        for(int i = 0; i < Tracks.size(); i++)
        {
            if(track.GetId() == Tracks.get(i).GetId())
                Tracks.remove(i);
        }
    }

    public ArrayList<Track> GetTracks()
    {
        return Tracks;
    }
}
