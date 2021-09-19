package su.damirka.getwave.views.playlists.mylibrary;

import android.view.LayoutInflater;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.gridlayout.widget.GridLayout;

import su.damirka.getwave.R;
import su.damirka.getwave.views.playlists.PlaylistManager;

public class MyLibManager extends PlaylistManager
{
    private GridLayout GridLayout;
    private View PlaylistsView;

    private LikedSongs LPlaylist;
    private CreatedPlaylist OPlaylist;

    private boolean Visible;

    public MyLibManager(ConstraintLayout layout)
    {
        super(layout);
        PlaylistsView = LayoutInflater.from(MainLayout.getContext()).inflate(R.layout.grid_playlists_view, MainLayout, false);
        GridLayout = PlaylistsView.findViewById(R.id.GridLayout);
        LPlaylist = new LikedSongs(MainLayout, GridLayout);
    }

    @Override
    public void Show()
    {
        if(!Visible)
        {
            LPlaylist.Show();
            MainLayout.addView(GridLayout);
            Visible = true;
        }
    }

    @Override
    public void Hide()
    {
        if(Visible)
        {
            LPlaylist.Hide();
            MainLayout.removeAllViews();
            Visible = false;
        }
    }

    public void Release()
    {

    }
}
