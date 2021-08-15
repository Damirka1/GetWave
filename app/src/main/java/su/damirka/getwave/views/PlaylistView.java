package su.damirka.getwave.views;

import android.content.Context;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

import su.damirka.getwave.music.Playlist;
import su.damirka.getwave.music.Song;
import su.damirka.getwave.views.PlaylistAdapter.SongViewHolder;

public class PlaylistView
{
    private RecyclerView RView;
    private RecyclerView.LayoutManager LayoutManager;
    private ConstraintLayout Layout;

    private Playlist Playlist;
    private PlaylistAdapter PlaylistAdapter;
    private boolean Initialized;
    private boolean Visible;

    public PlaylistView(Playlist Playlist, Context Context, ConstraintLayout Lay)
    {
        Initialized = false;
        Visible = false;
        RView = new RecyclerView(Context);
        RView.setHasFixedSize(true);
        LayoutManager = new LinearLayoutManager(Context, LinearLayoutManager.VERTICAL, false);
        RView.setLayoutManager(LayoutManager);

        SetPlaylist(Playlist);

        this.Layout = Lay;
    }

    public boolean IsInitialized()
    {
        return Initialized;
    }

    public void Update(long Index)
    {
        Playlist.SetSelected(Index);
        for(int i = 0; i < Playlist.GetSize(); i++)
        {
            SongViewHolder View = (SongViewHolder)RView.findViewHolderForLayoutPosition(i);
            if(Objects.nonNull(View))
            {
                if(i == Playlist.GetCurrentSelected())
                    View.Select();
                else
                    View.DeSelect();
            }
        }
    }

    public Song GetSongByIndex(long Index)
    {
        return Playlist.GetSongByIndex(Index);
    }

    public Song GetCurrentSong()
    {
        return Playlist.GetCurrSong();
    }

    public void SetPlaylist(Playlist Playlist)
    {
        if(Objects.nonNull(Playlist))
        {
            this.Playlist = Playlist;
            PlaylistAdapter = new PlaylistAdapter(Playlist);
            Initialized = true;
            RView.setAdapter(PlaylistAdapter);
        }
    }

    public void Show()
    {
        if(!Visible)
        {
            Layout.addView(RView);
            Visible = true;
        }
    }

    public void Hide()
    {
        if(Visible)
        {
            Layout.removeView(RView);
            Visible = false;
        }
    }

    public void Release()
    {
        Hide();
        RView.setAdapter(null);
        RView = null;
        LayoutManager = null;
        Layout = null;
    }
}
