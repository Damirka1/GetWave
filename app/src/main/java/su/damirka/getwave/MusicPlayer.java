package su.damirka.getwave;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


class Songs extends RecyclerView.Adapter<Songs.ViewHolder>
{

    class Song
    {
        private String PathToFile;
        private String Name;
        private String Title, Author;
        private Bitmap Image = null;
        private boolean HasImage = false;

        private String Duration;

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
                Duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

                byte [] data = mmr.getEmbeddedPicture();
                //coverart is an Imageview object

                // convert the byte array to a bitmap
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
    }

    private ArrayList<Song> Songs = null;
    private int CurrPlaying = 0;
    private TextView PrevView = null;


    // Check all musics in music folder.
    public Songs()
    {
        Songs = new ArrayList<Song>();

        File Directory = new File(Environment.getExternalStorageDirectory().toString() + "/Music");
        File[] Files = Directory.listFiles();
        for (int i = 0; i < Files.length; i++)
        {
            String Name = Files[i].getName();
            String extension = Name.substring(Name.lastIndexOf("."));

            if(extension.contentEquals(".mp3"))
                Songs.add(new Song(Files[i].getAbsolutePath(), Name.substring(0, Name.lastIndexOf('.'))));
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder
    {
        private TextView ViewName, ViewAuthor;
        private ImageView ViewImage;
        private ConstraintLayout SongLayout;

        public ViewHolder(@NonNull View itemView, Song s)
        {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    try
                    {
                        if(PrevView != null)
                            PrevView.setTextColor(Color.parseColor("#fafafa"));

                        ViewName.setTextColor(Color.parseColor("#50c878"));
                        PrevView = ViewName;
                        MusicPlayer MP = MainActivity.GetMusicPlayer();
                        CurrPlaying = getAdapterPosition();
                        MP.SetSong(Songs.get(CurrPlaying));
                    }
                    catch (Exception e)
                    {
                        Toast.makeText(v.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

            ViewName = itemView.findViewById(R.id.SongNameSmall);
            ViewName.setTextColor(Color.parseColor("#f1f1f1"));
            ViewAuthor = itemView.findViewById(R.id.SongAuthorSmall);
            ViewAuthor.setTextColor(Color.parseColor("#f1f1f1"));
            ViewImage = itemView.findViewById(R.id.SongImageSmall);
        }

        public TextView GeViewName()
        {
            return ViewName;
        }
        public TextView GeViewAuthor()
        {
            return ViewAuthor;
        }
        public ImageView GetImage()
        {
            return ViewImage;
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
}



public class MusicPlayer implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener
{
    private MediaPlayer MP;
    private AudioManager AM;
    private boolean Ready = false;
    private ConstraintLayout SongMenu;

    public boolean Playing = false;
    public boolean Repeat = true;
    public Songs.Song S;



    MusicPlayer(MainActivity MA) throws IOException
    {
        MP = new MediaPlayer();
        MP.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );

        SongMenu = MA.findViewById(R.id.SongMenu);
    }

    void SetSong(Songs.Song s) throws IOException
    {
        this.S = s;
        Stop();
        Ready = false;
        MP.setDataSource(s.GetPath());
        MP.setOnPreparedListener(this);
        MP.prepareAsync();
        SongMenu.setVisibility(View.VISIBLE);
        if(s.GetTitle() != null)
            ((TextView)SongMenu.findViewById(R.id.TitleMenu)).setText(s.GetTitle());
        else
            ((TextView)SongMenu.findViewById(R.id.TitleMenu)).setText(s.GetName());

        ((TextView)SongMenu.findViewById(R.id.AuthorMenu)).setText(s.GetAuthor());

        if(s.HasImage())
            ((ImageView)SongMenu.findViewById(R.id.ImageMenu)).setImageBitmap(s.GetImage());
        else
            ((ImageView)SongMenu.findViewById(R.id.ImageMenu)).setImageResource(R.drawable.musicicon);
    }

    public int GetCurrPos()
    {
        return MP.getCurrentPosition();
    }

    public int GetDuration()
    {
        return MP.getDuration();
    }

    void Play(Drawable d)
    {
        if(Ready)
        {
            MP.start();
            Playing = true;
            ((Button)SongMenu.findViewById(R.id.PlayMenu)).setBackground(d);
        }
    }

    void Pause(Drawable d)
    {
        MP.pause();
        Playing = false;
        ((Button)SongMenu.findViewById(R.id.PlayMenu)).setBackground(d);
    }

    void Stop()
    {
        MP.stop();
        MP.reset();
        Playing = false;
    }

    public void finalize()
    {
        Stop();
        MP.release();
        Playing = true;
        MP = null;
    }

    @Override
    public void onPrepared(MediaPlayer mp)
    {
        Ready = true;
        Playing = true;
        mp.start();
    }

    @Override
    public void onCompletion(MediaPlayer mp)
    {
        if(Repeat)
        {
            try {
                SetSong(S);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
        {
            try
            {
                SetSong(MainActivity.GetSongsFromFindWindow().GetNextSong());
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

}
