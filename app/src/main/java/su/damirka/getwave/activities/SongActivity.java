package su.damirka.getwave.activities;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.Objects;

import su.damirka.getwave.Application;
import su.damirka.getwave.R;
import su.damirka.getwave.music.Song;

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

        private final SongActivity SA;

        private boolean BarCaptured;

        private boolean Playing;

        @SuppressLint("UseCompatLoadingForDrawables")
        private SongWindow(SongActivity sa)
        {
            SA = sa;

            App = MainActivity.GetApp();

            SongName = SA.findViewById(R.id.SongName);
            SongAuthor = SA.findViewById(R.id.SongAuthor);
            PlayButton = SA.findViewById(R.id.PlayButton);
            NextButton = SA.findViewById(R.id.NextButton);
            PrevButton = SA.findViewById(R.id.PrevButton);
            Image = SA.findViewById(R.id.SongArt);
            SongBar = SA.findViewById(R.id.SongBar);
            CB = SA.findViewById(R.id.Repeat);
            CB.setChecked(false);

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
                    Bundle Msg = new Bundle();
                    Msg.putString("Msg", "SeekTo");
                    Msg.putInt("Position", seekBar.getProgress());
                    MainActivity.SendMsgToMusicService(Msg);
                }
            });

            Play = SA.getDrawable(R.drawable.play);
            Pause = SA.getDrawable(R.drawable.pause);

            PlayButton.setBackground(Pause);

            Bundle Data = sa.getIntent().getExtras();

            Playing = Data.getByte("Playing") == 1;
            UpdateSongBar(Data.getInt("Position"), Data.getInt("Duration"));
        }

        private void Update(Song s)
        {
            if(s.HasArt())
                Image.setImageBitmap(s.GetArt());
            else
                Image.setImageResource(R.drawable.musicicon);

            String Title = s.GetTitle();

            if(Title != null)
                SongName.setText(Title);
            else
                SongName.setText(R.string.UndefinedTitle);

            if(s.GetAuthor() != null)
                SongAuthor.setText(s.GetAuthor());
            else
                SongAuthor.setText(R.string.UndefinedAuthor);
        }

        private void UpdateSongBar(int Position, int Duration)
        {
            if(!BarCaptured)
                SongBar.setProgress(Position);
            SongBar.setMax(Duration);
        }

        private void UpdateStates(boolean Playing, boolean Repeat)
        {
            CB.setChecked(Repeat);

            this.Playing = Playing;

            if(!Playing)
                PlayButton.setBackground(Play);
            else
                PlayButton.setBackground(Pause);
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
                Bundle Msg = new Bundle();
                if(!Playing)
                {
                    Msg.putString("Msg", "Play");
                    MainActivity.SendMsgToMusicService(Msg);
                    PlayButton.setBackground(Pause);
                    Playing = true;
                }
                else
                {
                    Msg.putString("Msg", "Pause");
                    MainActivity.SendMsgToMusicService(Msg);
                    PlayButton.setBackground(Play);
                    Playing = false;
                }
            }
            else if (Id == R.id.Repeat)
            {
                Bundle Msg = new Bundle();
                Msg.putString("Msg", "Repeat");
                Msg.putBoolean("Repeat", CB.isChecked());
                MainActivity.SendMsgToMusicService(Msg);
            }
            else if (Id == R.id.NextButton)
            {
                Bundle Msg = new Bundle();
                Msg.putString("Msg", "PlayNext");
                MainActivity.SendMsgToMusicService(Msg);
            }
            else if (Id == R.id.PrevButton)
            {
                Bundle Msg = new Bundle();
                if(SongBar.getProgress() > 5000) // 5 seconds
                {
                    Msg.putString("Msg", "SeekTo");
                    Msg.putInt("Position", 0);
                }
                else
                    Msg.putString("Msg", "PlayPrev");
                MainActivity.SendMsgToMusicService(Msg);
            }
        }
    }

    private SongWindow SW;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);

        SW = new SongWindow(this);
    }

    BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle Msg = intent.getExtras();

            if(Objects.isNull(Msg))
                return;

            try {
                switch (Msg.getString("Msg"))
                {
                    case "UpdateUI":
                        SW.Update(MainActivity.GetApp().GetSongByIndex(Msg.getInt("Index")));
                        break;
                    case "Playing":
                        SW.UpdateSongBar(Msg.getInt("Position"), Msg.getInt("Duration"));
                        break;
                    case "CurrentState":
                        SW.UpdateStates(Msg.getBoolean("Playing"), Msg.getBoolean("Repeat"));
                        break;
                }
            } catch (NullPointerException nullPointerException)
            {
                nullPointerException.printStackTrace();
            }
        }
    };

    @Override
    protected void onResume()
    {
        super.onResume();
        IntentFilter recv = new IntentFilter("UpdateUI");
        this.registerReceiver(br, recv);
        MainActivity.GetApp().Resume();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(br);
        MainActivity.GetApp().Pause();
    }

}