package su.damirka.getwave.views.playlists.mylibrary;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

import su.damirka.getwave.R;
import su.damirka.getwave.activities.MainActivity;
import su.damirka.getwave.music.Playlist;
import su.damirka.getwave.music.Song;
import su.damirka.getwave.views.playlists.Adapter;
import su.damirka.getwave.views.playlists.find.FindMenuPlaylistAdapter;

public class LibMenuPlaylistAdapter extends RecyclerView.Adapter<LibMenuPlaylistAdapter.LibMenuSongViewHolder> implements Adapter
{
    private final Playlist Playlist;
    private final RecyclerView RView;

    private int SongViewId;

    public LibMenuPlaylistAdapter(Playlist Playlist, RecyclerView recyclerView)
    {
        this.Playlist = Playlist;
        this.RView = recyclerView;
        SongViewId = R.layout.lib_menu_song_view;
    }

    @NonNull
    @Override
    public LibMenuSongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View View = LayoutInflater.from(parent.getContext()).inflate(SongViewId, parent, false);
        return new LibMenuSongViewHolder(View, (int) Playlist.GetCurrentSelected(), Playlist, this);
    }

    @Override
    public void onBindViewHolder(@NonNull LibMenuSongViewHolder holder, int position)
    {
        holder.Initialize(Playlist.GetSongByIndexWithoutSelecting(position), (int) Playlist.GetCurrentSelected(), position);
    }

    @Override
    public int getItemCount()
    {
        return Playlist.GetSize();
    }

    @Override
    public Playlist GetPlaylist()
    {
        return Playlist;
    }

    @Override
    public int GetCount()
    {
        return Playlist.GetSize();
    }

    @Override
    public void SetName(String Name)
    {
        Playlist.SetName(Name);
    }

    @Override
    public String GetName()
    {
        return Playlist.GetName();
    }

    @Override
    public void Update(int Index)
    {
        Playlist.SetSelected(Index);
        for(int i = 0; i < Playlist.GetSize(); i++)
        {
            LibMenuSongViewHolder View = (LibMenuSongViewHolder) RView.findViewHolderForLayoutPosition(i);
            if(Objects.nonNull(View))
            {
                if(i == Playlist.GetCurrentSelected())
                    View.Select();
                else
                    View.DeSelect();
            }
        }
    }

    protected static class LibMenuSongViewHolder extends RecyclerView.ViewHolder
    {
        private final TextView ViewName, ViewAuthor;
        private final ImageView ViewImage;
        private Playlist pPlaylist;
        private Adapter Parent;
        private Song s;

        public void Initialize(Song s, int CurrPlaying, int Position)
        {
            ViewName.setText(s.GetTitle());
            ViewAuthor.setText(s.GetAuthor());

            if(s.HasArt())
                ViewImage.setImageBitmap(s.GetArt());
            else
                ViewImage.setImageResource(R.drawable.musicicon);

            if(CurrPlaying == Position)
                Select();
            else
                DeSelect();

            this.s = s;
        }

        public void Select()
        {
            ViewName.setTextColor(Color.parseColor("#50c878"));
        }

        public void DeSelect()
        {
            ViewName.setTextColor(Color.parseColor("#f1f1f1"));
        }

        private void OnClick(@NonNull View view)
        {
            MainActivity.GetApp().SetPlaylist(Parent);
            Bundle Msg = new Bundle();
            Msg.putInt("Index", getLayoutPosition());
            MainActivity.GetMediaController().getTransportControls().playFromMediaId("Index", Msg);
            pPlaylist.SetSelected(getAbsoluteAdapterPosition());
            Select();
        }

        public LibMenuSongViewHolder(@NonNull View itemView, int CurrPlaying, Playlist playlist, Adapter parent)
        {
            super(itemView);

            ViewName = itemView.findViewById(R.id.SongNameSmall);
            ViewAuthor = itemView.findViewById(R.id.SongAuthorSmall);
            ViewImage = itemView.findViewById(R.id.SongImageSmall);
            pPlaylist = playlist;
            Parent = parent;

            if(CurrPlaying == getAbsoluteAdapterPosition())
                Select();
            else
                DeSelect();

            itemView.setOnClickListener(this::OnClick);
        }
    }

}
