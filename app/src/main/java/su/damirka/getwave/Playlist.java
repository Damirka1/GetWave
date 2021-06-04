package su.damirka.getwave;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.List;

public class Playlist
{
    private RecyclerView RView;
    private RecyclerView.LayoutManager LayoutManager;
    private ConstraintLayout Layout;
    private Songs Songs;
    private TextView Selected;

    private boolean Visible;

    public Playlist(Context C, ConstraintLayout Lay, Songs s)
    {
        this.Songs = s;
        RView = new RecyclerView(C);
        RView.setHasFixedSize(true);
        LayoutManager = new LinearLayoutManager(C, LinearLayoutManager.VERTICAL, false);
        RView.setLayoutManager(LayoutManager);

        RView.setAdapter(Songs);

        this.Layout = Lay;
        Visible = false;
    }

    public Playlist(Context C, ConstraintLayout Lay, String[] Files)
    {
        this(C, Lay, new Songs(Files));
    }

    public void PlaySong(su.damirka.getwave.Songs.Song s, MusicPlayer MP) throws IOException
    {
        MP.Play(s);
    }

    public void PlayNext(MusicPlayer MP) throws IOException
    {
        MP.Play(Songs.GetNextSong());
    }

    public void PlayPrev(MusicPlayer MP) throws IOException
    {
        MP.Play(Songs.GetPrevSong());
    }

    public Songs GetSongs()
    {
        return Songs;
    }

    public void SetSongs(Songs s)
    {
        this.Songs = s;
        RView.setAdapter(Songs);
    }

    public void ClearPlaylist()
    {
        this.Songs = null;
        RView.setAdapter(null);
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

    public void Update()
    {
        if(Visible)
        {
            if(Selected != null)
                Selected.setTextColor(Color.parseColor("#f1f1f1"));

            View V = LayoutManager.findViewByPosition(Songs.GetCurIndex());

            if(V != null)
            {
                Selected = V.findViewById(R.id.SongNameSmall);
                Selected.setTextColor(Color.parseColor("#50c878"));
            }
        }
    }

    public void Release()
    {
        Hide();
        ClearPlaylist();
        RView = null;
        LayoutManager = null;
        Layout = null;
    }
}
