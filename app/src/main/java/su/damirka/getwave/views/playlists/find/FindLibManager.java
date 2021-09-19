package su.damirka.getwave.views.playlists.find;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Optional;

import su.damirka.getwave.connection.ConnectionService;
import su.damirka.getwave.music.Playlist;
import su.damirka.getwave.views.PlaylistView;
import su.damirka.getwave.views.playlists.PlaylistManager;

public class FindLibManager extends PlaylistManager
{
    private final String Filename = "ServerPlaylist";
    private Playlist Playlist;
    private PlaylistView PlaylistView;
    private boolean Visible;

    public FindLibManager(ConstraintLayout MainLayout)
    {
        super(MainLayout);

        int Index = ConnectionService.StartDownloadingPlaylistWithAllMusic();

        Optional<Playlist> Value = ConnectionService.GetDownloadedPlaylist(Index);
        while(!Value.isPresent())
        {
            try
            {
                Thread.sleep(100);
                Value = ConnectionService.GetDownloadedPlaylist(Index);
            }
            catch (InterruptedException interruptedException)
            {
                interruptedException.printStackTrace();
            }
        }

        Playlist = Value.get();
        Playlist.SetName(Filename);
        PlaylistView = new PlaylistView(MainLayout);
        PlaylistView.SetAdapter(new FindMenuPlaylistAdapter(Value.get(), PlaylistView.GetRecyclerView()));
        Visible = false;
    }

    @Override
    public void Show()
    {
        if(!Visible)
        {
            PlaylistView.Show();
            Visible = true;
        }
    }

    @Override
    public void Hide()
    {
        if(Visible)
        {
            PlaylistView.Hide();
            MainLayout.removeAllViews();
            Visible = false;
        }
    }
}
