package su.damirka.getwave.views.playlists.find;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Objects;

import su.damirka.getwave.R;
import su.damirka.getwave.activities.MainActivity;
import su.damirka.getwave.connection.ConnectionService;
import su.damirka.getwave.files.CacheFile;
import su.damirka.getwave.files.FileManager;
import su.damirka.getwave.files.PlaylistTracks;
import su.damirka.getwave.music.MusicService;
import su.damirka.getwave.music.Playlist;
import su.damirka.getwave.music.Song;
import su.damirka.getwave.music.Track;
import su.damirka.getwave.views.PlaylistView;
import su.damirka.getwave.views.playlists.Adapter;

public class FindMenuPlaylistAdapter extends RecyclerView.Adapter<FindMenuPlaylistAdapter.FindMenuSongViewHolder> implements Adapter
{
    private final Playlist Playlist;
    private final RecyclerView RView;

    private ArrayList<Track> LikedTracks = null;

    private int SongViewId;

    public FindMenuPlaylistAdapter(Playlist Playlist, RecyclerView recyclerView)
    {
        this.Playlist = Playlist;
        this.RView = recyclerView;
        SongViewId = R.layout.find_menu_song_view;
        Object data = MainActivity.GetApp().GetFileManager().ReadObj("LikedSongs");
        if(Objects.nonNull(data))
            LikedTracks = ((PlaylistTracks) data).GetTracks();
    }

    @NonNull
    @Override
    public FindMenuSongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View View = LayoutInflater.from(parent.getContext()).inflate(SongViewId, parent, false);
        return new FindMenuSongViewHolder(View, (int) Playlist.GetCurrentSelected(), Playlist, this);
    }

    @Override
    public void onBindViewHolder(@NonNull FindMenuSongViewHolder holder, int position)
    {
        boolean Liked = false;
        Song s = Playlist.GetSongByIndexWithoutSelecting(position);
        if(Objects.nonNull(LikedTracks))
        {
            for(Track t : LikedTracks)
            {
                if (s.GetTrack().GetId() == t.GetId())
                {
                    Liked = true;
                    break;
                }
            }
        }
        holder.Initialize(s, (int) Playlist.GetCurrentSelected(), position, Liked);
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
            FindMenuSongViewHolder View = (FindMenuSongViewHolder) RView.findViewHolderForLayoutPosition(i);
            if(Objects.nonNull(View))
            {
                if(i == Playlist.GetCurrentSelected())
                    View.Select();
                else
                    View.DeSelect();
            }
        }
    }

    public static class FindMenuSongViewHolder extends RecyclerView.ViewHolder
    {
        private final TextView ViewName, ViewAuthor;
        private final ImageView ViewImage;
        private final ImageView HeartImage;
        private final Playlist pPlaylist;
        private final Adapter Parent;
        private Song s;
        private boolean Liked;

        public void Initialize(Song s, int CurrPlaying, int Position, boolean Liked)
        {
            ViewName.setText(s.GetTitle());
            ViewAuthor.setText(s.GetAuthor());

            String ArtPath = s.GetTrack().GetArtPath();
            if(ArtPath.length() > 0 && !s.HasArt())
            {
                CacheFile file = MusicService.GetCacheManager().LoadArtCache(s.GetTrack().GetId());
                byte[] Data;
                if(Objects.nonNull(file))
                    Data = file.GetData();
                else
                {
                    Data = ConnectionService.DownloadFileFromServer(ArtPath);
                    if(Objects.nonNull(Data))
                    {
                        CacheFile art = new CacheFile(s.GetTrack().GetId(), Data);
                        MusicService.GetCacheManager().SaveAsArtCache(art);
                    }
                }
                if(Objects.nonNull(Data))
                {
                    Bitmap bmp = BitmapFactory.decodeByteArray(Data, 0, Data.length);
                    s.SetArt(bmp);
                    ViewImage.setImageBitmap(s.GetArt());
                }
            }

            if(s.HasArt())
                ViewImage.setImageBitmap(s.GetArt());
            else
                ViewImage.setImageResource(R.drawable.musicicon);

            if(CurrPlaying == Position)
                Select();
            else
                DeSelect();

            this.Liked = Liked;

            if(Liked)
                HeartImage.setImageResource(R.drawable.heart_clicked_like_song_);
            else
                HeartImage.setImageResource(R.drawable.heart_like_song_);


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

        private void HeartOnClick(@NonNull View view)
        {
            ImageView image = (ImageView) view;

            FileManager FM = MainActivity.GetApp().GetFileManager();
            Object data = FM.ReadObj("LikedSongs");
            PlaylistTracks playlistTracks;

            if(Objects.isNull(data))
                playlistTracks = new PlaylistTracks();
            else
                playlistTracks = (PlaylistTracks) data;

            if(!Liked)
            {
                playlistTracks.AddTrack(s.GetTrack());
                image.setImageResource(R.drawable.heart_clicked_like_song_);
                Liked = true;
            }
            else
            {
                playlistTracks.RemoveTrack(s.GetTrack());
                image.setImageResource(R.drawable.heart_like_song_);
                Liked = false;
            }

            FM.SaveObj(playlistTracks, "LikedSongs");
        }

        public void SetLiked(boolean value)
        {
            Liked = value;
        }

        public FindMenuSongViewHolder(@NonNull View itemView, int CurrPlaying, Playlist playlist, Adapter parent)
        {
            super(itemView);

            ViewName = itemView.findViewById(R.id.SongNameSmall);
            ViewAuthor = itemView.findViewById(R.id.SongAuthorSmall);
            ViewImage = itemView.findViewById(R.id.SongImageSmall);
            HeartImage = itemView.findViewById(R.id.Heart);
            Liked = false;

            pPlaylist = playlist;
            Parent = parent;

            if(CurrPlaying == getAbsoluteAdapterPosition())
                Select();
            else
                DeSelect();

            itemView.setOnClickListener(this::OnClick);
            HeartImage.setOnClickListener(this::HeartOnClick);
        }
    }

}
