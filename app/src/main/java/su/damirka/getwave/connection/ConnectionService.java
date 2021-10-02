package su.damirka.getwave.connection;

import android.widget.Toast;

import java.util.Objects;
import java.util.Optional;
import java.util.Vector;

import su.damirka.getwave.activities.MainActivity;
import su.damirka.getwave.music.Playlist;
import su.damirka.getwave.music.Track;

public class ConnectionService implements Runnable
{
    private static boolean Running;
    private static boolean CanConnect = true;

    private static Vector<Playlist> PlaylistDownloadingVector;

    private static native Track[] LoadAllMusicFromServer();
    private static native byte[] GetFileFromServer(String Path);

    private static boolean ErrorFlag = false;
    private static String ErrorMsg = "";

    private native boolean Connect();

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

    public static boolean CheckError()
    {
        return ErrorFlag;
    }

    public static String GetErrorMsg()
    {
        return ErrorMsg;
    }

    public static boolean CanConnect() { return CanConnect; }

    public static int StartDownloadingPlaylistWithAllMusic()
    {
        int Size = PlaylistDownloadingVector.size();
        new Thread(() -> {
            Track[] Tracks = LoadAllMusicFromServer();

            if(Tracks.length == 1)
            {
                Track err = Tracks[0];

                if(err.GetId() == -1)
                {
                    ErrorFlag = true;
                    ErrorMsg = "Can't download playlist from server\n";
                    return;
                }
            }

            Playlist nPlaylist = new Playlist(Tracks);
            PlaylistDownloadingVector.add(nPlaylist);
        }).start();
        return Size;
    }

    public ConnectionService(MainActivity MA)
    {
        if(!Connect())
        {
            Toast.makeText(MA, "Can't connect to the Server", Toast.LENGTH_LONG).show();
            CanConnect = false;
        }

        Initialize();
    }

    private void Initialize()
    {
        Running = true;
        PlaylistDownloadingVector = new Vector<>();
    }

    public static byte[] DownloadFileFromServer(String Path)
    {
        if(CanConnect)
        {
            byte[] data = GetFileFromServer(Path);
            if(data[0] == -1 && data.length == 1)
                return null;
            return data;
        }
        return null;
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
