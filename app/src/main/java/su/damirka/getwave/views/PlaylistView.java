package su.damirka.getwave.views;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

import su.damirka.getwave.music.Playlist;
import su.damirka.getwave.views.playlists.find.FindMenuPlaylistAdapter.FindMenuSongViewHolder;

public class PlaylistView
{
    private RecyclerView RView;
    private RecyclerView.LayoutManager LayoutManager;
    private ConstraintLayout Layout;

    private RecyclerView.Adapter PlaylistAdapter;
    private boolean Initialized;
    private boolean Visible;

    public PlaylistView(ConstraintLayout Lay)
    {
        Initialized = false;
        Visible = false;
        RView = new RecyclerView(Lay.getContext());
        RView.setHasFixedSize(true);
        LayoutManager = new LinearLayoutManager(Lay.getContext(), LinearLayoutManager.VERTICAL, false);
        RView.setLayoutManager(LayoutManager);
        this.Layout = Lay;
    }

    public RecyclerView GetRecyclerView()
    {
        return RView;
    }

    public boolean IsInitialized()
    {
        return Initialized;
    }

//    public void Update(long Index)
//    {
//        Playlist.SetSelected(Index);
//        for(int i = 0; i < Playlist.GetSize(); i++)
//        {
//            FindMenuSongViewHolder View = (FindMenuSongViewHolder) RView.findViewHolderForLayoutPosition(i);
//            if(Objects.nonNull(View))
//            {
//                if(i == Playlist.GetCurrentSelected())
//                    View.Select();
//                else
//                    View.DeSelect();
//            }
//        }
//    }

    public void SetAdapter(RecyclerView.Adapter adapter)
    {
        PlaylistAdapter = adapter;
        RView.setAdapter(PlaylistAdapter);
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
