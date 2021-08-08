package su.damirka.getwave.views;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

import su.damirka.getwave.BuildConfig;
import su.damirka.getwave.R;
import su.damirka.getwave.activities.MainActivity;
import su.damirka.getwave.music.MusicService;
import su.damirka.getwave.music.Playlist;
import su.damirka.getwave.music.Song;
import su.damirka.getwave.music.StreamMediaDataSource;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.SongViewHolder>
{
    private final Playlist Playlist;

    public PlaylistAdapter(Playlist Playlist)
    {
        this.Playlist = Playlist;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View View = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_view, parent, false);
        return new SongViewHolder(View);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position)
    {
        holder.Initialize(Playlist.GetSongByIndexWithoutSelecting(position), (int) Playlist.GetCurrentSelected(), position);
    }

    @Override
    public int getItemCount()
    {
        return Playlist.GetSize();
    }

    protected static class SongViewHolder extends RecyclerView.ViewHolder
    {
        private final TextView ViewName, ViewAuthor;
        private final ImageView ViewImage;

        public void Initialize(Song s, int CurrPlaying, int Position)
        {
            android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(s.GetMetaStream());

            String Title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            String Author = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);

            s.SetTitle(Title);
            s.SetAuthor(Author);

            ViewName.setText(Title);
            ViewAuthor.setText(Author);

            byte[] data = mmr.getEmbeddedPicture();
            if(data != null)
            {
                Bitmap Art = BitmapFactory.decodeByteArray(data, 0, data.length);
                s.SetArt(Art);
                ViewImage.setImageBitmap(Art);
            }
            else
                ViewImage.setImageResource(R.drawable.musicicon);

            if(CurrPlaying == Position)
                Select();
            else
                DeSelect();
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
            Bundle Msg = new Bundle();
            Msg.putString("Msg", "PlayOther");
            Msg.putInt("Index", getLayoutPosition());
            MainActivity.SendMsgToMusicService(Msg);
        }

        public SongViewHolder(@NonNull View itemView)
        {
            super(itemView);

            ViewName = itemView.findViewById(R.id.SongNameSmall);
            //ViewName.setTextColor(Color.parseColor("#f1f1f1"));
            ViewAuthor = itemView.findViewById(R.id.SongAuthorSmall);
            //ViewAuthor.setTextColor(Color.parseColor("#f1f1f1"));
            ViewImage = itemView.findViewById(R.id.SongImageSmall);

            itemView.setOnClickListener(this::OnClick);
        }
    }

}
