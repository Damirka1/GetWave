package su.damirka.getwave.views.playlists.mylibrary;

import android.view.LayoutInflater;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.gridlayout.widget.GridLayout;

import java.util.Objects;

import su.damirka.getwave.R;
import su.damirka.getwave.activities.MainActivity;
import su.damirka.getwave.files.FileManager;
import su.damirka.getwave.files.PlaylistTracks;
import su.damirka.getwave.music.Playlist;
import su.damirka.getwave.views.PlaylistView;

public class LikedSongs
{
    private final String Filename = "LikedSongs";
    private PlaylistView PlaylistView;
    private Playlist Playlist;
    private ConstraintLayout MainLayout;
    private GridLayout GridLayout;
    private LibMenuPlaylistAdapter Adapter;
    private View V;
    private boolean Visible;

    public LikedSongs(ConstraintLayout MainLayout, GridLayout GridLayout)
    {
        this.MainLayout = MainLayout;
        this.GridLayout = GridLayout;
        V = LayoutInflater.from(GridLayout.getContext()).inflate(R.layout.playlist_view, GridLayout, false);
        Visible = false;
        PlaylistView = new PlaylistView(MainLayout);
    }

    void Show()
    {
        if(!Visible)
        {
            V.setOnClickListener(this::Click);
            GridLayout.addView(V);
            Visible = true;
        }
    }

    void Hide()
    {
        if(Visible)
        {
            PlaylistView.Hide();
            V.setOnClickListener(null);
            GridLayout.removeView(V);
            Visible = false;
        }
    }

    void Click(View v)
    {
        FileManager FM = MainActivity.GetApp().GetFileManager();
        Object data = FM.ReadObj(Filename);

        if(Objects.isNull(data))
            return;

        PlaylistTracks tracks = (PlaylistTracks) data;
        if(Objects.isNull(Playlist) || tracks.GetTracks().size() != Playlist.GetSize())
        {
            Playlist = new Playlist(tracks.GetTracks());
            Playlist.SetName(Filename);
            Adapter = new LibMenuPlaylistAdapter(Playlist, PlaylistView.GetRecyclerView());
            PlaylistView.SetAdapter(Adapter);
        }

        MainLayout.removeAllViews();
        PlaylistView.Show();
    }

}
