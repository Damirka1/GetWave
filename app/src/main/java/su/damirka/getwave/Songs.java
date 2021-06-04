package su.damirka.getwave;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class Songs extends RecyclerView.Adapter<Songs.ViewHolder>
{
    static class Song
    {
        private String PathToFile;
        private String Name;
        private String Title, Author;
        private Bitmap Image = null;
        private boolean HasImage = false;

        //private String Duration;

        public Song(String PathToFile, String Name)
        {
            try
            {
                this.PathToFile = PathToFile;
                this.Name = Name;
                android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(PathToFile);

                Title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                Author = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                //Duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

                byte [] data = mmr.getEmbeddedPicture();
                //coverart is an Imageview object

                // conveart the byte array to a bitmap
                if(data != null)
                {
                    Image = BitmapFactory.decodeByteArray(data, 0, data.length);
                    HasImage = true;
                }
            }
            catch (IllegalArgumentException e)
            {
                e.printStackTrace();
            }
        }

        public String GetPath()
        {
            return PathToFile;
        }
        public String GetName()
        {
            return Name;
        }
        public String GetTitle()
        {
            return Title;
        }
        public String GetAuthor()
        {
            return Author;
        }
        public Bitmap GetImage() { return Image; }
        public boolean HasImage() {return HasImage;}
        //public String GetDuration() {return Duration;}
    }

    private final ArrayList<Song> Songs;
    private int CurrPlaying = -1;


    public Songs(String[] Files)
    {
        Songs = new ArrayList<>();

        for (String file : Files)
            Songs.add(new Song(file, null));
    }

    public int GetCurIndex()
    {
        return CurrPlaying;
    }

    class ViewHolder extends RecyclerView.ViewHolder
    {
        private final TextView ViewName, ViewAuthor;
        private final ImageView ViewImage;

        public ViewHolder(@NonNull View itemView, Song s)
        {
            super(itemView);

            itemView.setOnClickListener(v -> {
                try
                {
                    CurrPlaying = getAdapterPosition();
                    MainActivity.App.PlaySong(Songs.get(CurrPlaying));
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            });

            ViewName = itemView.findViewById(R.id.SongNameSmall);
            ViewName.setTextColor(Color.parseColor("#f1f1f1"));
            ViewAuthor = itemView.findViewById(R.id.SongAuthorSmall);
            ViewAuthor.setTextColor(Color.parseColor("#f1f1f1"));
            ViewImage = itemView.findViewById(R.id.SongImageSmall);
        }

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View View = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_view, parent, false);
        return new ViewHolder(View, Songs.get(viewType));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        Song s = Songs.get(position);
        if(s.HasImage)
            holder.ViewImage.setImageBitmap(s.Image);
        else
            holder.ViewImage.setImageResource(R.drawable.musicicon);

        if(s.Title != null)
            holder.ViewName.setText(s.Title);
        else
            holder.ViewName.setText(s.Name);
        holder.ViewAuthor.setText(s.Author);

        if(CurrPlaying == position)
            holder.ViewName.setTextColor(Color.parseColor("#50c878"));
        else
            holder.ViewName.setTextColor(Color.parseColor("#f1f1f1"));
    }

    @Override
    public int getItemCount()
    {
        return Songs.size();
    }

    public Song GetNextSong()
    {
        if((CurrPlaying + 1) >= Songs.size())
        {
            CurrPlaying = 0;
            return Songs.get(CurrPlaying);
        }

        return Songs.get(++CurrPlaying);
    }

    public Song GetPrevSong()
    {
        if((CurrPlaying - 1) < 0)
        {
            CurrPlaying = Songs.size() - 1;
            return Songs.get(CurrPlaying);
        }

        return Songs.get(--CurrPlaying);
    }
}