package su.damirka.getwave.connection;

import java.util.Objects;
import java.util.Optional;
import java.util.Vector;

import su.damirka.getwave.music.Playlist;
import su.damirka.getwave.music.Track;

public class ConnectionService implements Runnable
{
    private static boolean Running;

    private static Vector<Playlist> PlaylistDownloadingVector;

    private static native Track[] LoadAllMusicFromServer();

    public static Optional<Playlist> GetDownloadedPlaylist(int Index)
    {
        try
        {
            Playlist nPlaylist = PlaylistDownloadingVector.elementAt(Index);
            if(Objects.nonNull(nPlaylist))
            {
                PlaylistDownloadingVector.remove(nPlaylist);
                return Optional.of(nPlaylist);
            }
        }
        catch (ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException)
        {
            return Optional.empty();
        }
        return Optional.empty();
    }

    public static int StartDownloadingPlaylistWithAllMusic()
    {
        int Size = PlaylistDownloadingVector.size();
        new Thread(() -> {
            Track[] Tracks = LoadAllMusicFromServer();
            Playlist nPlaylist = new Playlist(Tracks);
            PlaylistDownloadingVector.add(nPlaylist);
        }).start();
        return Size;
    }

    public ConnectionService()
    {
        Initialize();
    }

    private void Initialize()
    {
        Running = true;
        PlaylistDownloadingVector = new Vector<>();
    }

    @Override
    public void run()
    {
        Initialize();

        while(Running)
        {
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException interruptedException)
            {
                interruptedException.printStackTrace();
            }
        }
    }
}
