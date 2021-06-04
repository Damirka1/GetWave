package su.damirka.getwave;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class SongActivity extends AppCompatActivity
{
    static class SongWindow
    {
        private final TextView SongName;
        private final TextView SongAuthor;
        private final Button PlayButton;
        private final Button NextButton;
        private final Button PrevButton;
        private final CheckBox CB;
        private final ImageView Image;
        private final SeekBar SongBar;

        private final Drawable Play;
        private final Drawable Pause;

        private final Application App;
        private final MusicPlayer MP;

        private final SongActivity SA;

        private boolean BarCaptured;


        @SuppressLint("UseCompatLoadingForDrawables")
        private SongWindow(SongActivity sa)
        {
            SA = sa;

            App = MainActivity.App;
            MP = MainActivity.App.GetMusicPlayer();

            SongName = SA.findViewById(R.id.SongName);
            SongAuthor = SA.findViewById(R.id.SongAuthor);
            PlayButton = SA.findViewById(R.id.PlayButton);
            NextButton = SA.findViewById(R.id.NextButton);
            PrevButton = SA.findViewById(R.id.PrevButton);
            Image = SA.findViewById(R.id.SongArt);
            SongBar = SA.findViewById(R.id.SongBar);
            CB = SA.findViewById(R.id.Repeat);
            CB.setChecked(MP.Repeat);

            SA.findViewById(R.id.BackButton).setOnClickListener(this::OnClick);
            PlayButton.setOnClickListener(this::OnClick);
            NextButton.setOnClickListener(this::OnClick);
            PrevButton.setOnClickListener(this::OnClick);
            CB.setOnClickListener(this::OnClick);

            BarCaptured = false;

            SongBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
            {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
                {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar)
                {
                    BarCaptured = true;
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar)
                {
                    BarCaptured = false;
                    MP.SeekTo(seekBar.getProgress());
                }
            });

            Play = SA.getDrawable(R.drawable.play);
            Pause = SA.getDrawable(R.drawable.pause);

            PlayButton.setBackground(Pause);

            Update(MP.GetSong(), MP.GetDuration());
        }

        private void Update(Songs.Song s, int MaxDuration)
        {
            if(s.HasImage())
                Image.setImageBitmap(s.GetImage());
            else
                Image.setImageResource(R.drawable.musicicon);

            String Title = s.GetTitle();

            if(Title != null)
                SongName.setText(Title);
            else
                SongName.setText(s.GetName());

            SongAuthor.setText(s.GetAuthor());

            SongBar.setMax(MaxDuration);
        }

        private void UpdateSongBar(int Duration)
        {
            if(!BarCaptured)
                SongBar.setProgress(Duration);
        }

        private void OnClick(View v)
        {
            int Id = v.getId();

            if(Id == R.id.BackButton)
            {
                SA.finish();
            }
            else if(Id == R.id.PlayButton)
            {
                if(!MP.Playing)
                {
                    App.Play();
                    PlayButton.setBackground(Pause);
                }
                else
                {
                    App.Pause();
                    PlayButton.setBackground(Play);
                }
            }
            else if (Id == R.id.Repeat)
            {
                MP.Repeat = CB.isChecked();
            }
            else if (Id == R.id.NextButton)
            {
                try
                {
                    App.PlayNext();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            else if (Id == R.id.PrevButton)
            {
                try
                {
                    App.PlayPrev();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }


    @SuppressLint("StaticFieldLeak")
    private static SongWindow SW;
    private static boolean Visible;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);

        SW = new SongWindow(this);
    }

    public static boolean IsVisible()
    {
        return Visible;
    }

    public static void Update(Songs.Song s, int MaxDuration)
    {
        SW.Update(s, MaxDuration);
    }

    public static void UpdateBar(int Duration)
    {
        SW.UpdateSongBar(Duration);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Visible = true;
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        Visible = false;
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        Visible = false;
    }
}