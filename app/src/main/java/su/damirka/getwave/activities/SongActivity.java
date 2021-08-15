package su.damirka.getwave.activities;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
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
import su.damirka.getwave.music.MusicService;
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
                    MediaController.getTransportControls().seekTo(seekBar.getProgress());
                }
            });

            Play = SA.getDrawable(R.drawable.play);
            Pause = SA.getDrawable(R.drawable.pause);

            PlayButton.setBackground(Pause);
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

        private void UpdateSongBar(long Position, long Duration)
        {
            if(!BarCaptured)
                SongBar.setProgress((int)Position);
            SongBar.setMax((int)Duration);
        }

        private void UpdateStates(PlaybackStateCompat States)
        {
            Bundle Msg = States.getExtras();
            CB.setChecked(Msg.getBoolean("Repeat"));
            Update(Msg.getParcelable("Song"));

            this.Playing = States.getState() == PlaybackStateCompat.STATE_PLAYING;;

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
                if(!Playing)
                {
                    MediaController.getTransportControls().play();
                    Playing = true;
                    PlayButton.setBackground(Pause);
                }
                else
                {
                    MediaController.getTransportControls().pause();
                    Playing = false;
                    PlayButton.setBackground(Play);
                }
            }
            else if (Id == R.id.Repeat)
            {
                boolean flag = CB.isChecked();
                if(flag)
                    MediaController.getTransportControls().setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ONE);
                else
                    MediaController.getTransportControls().setRepeatMode(PlaybackStateCompat.REPEAT_MODE_NONE);
            }
            else if (Id == R.id.NextButton)
            {
                MediaController.getTransportControls().skipToNext();
            }
            else if (Id == R.id.PrevButton)
            {
                if(SongBar.getProgress() > 5000) // 5 seconds
                    MediaController.getTransportControls().seekTo(0);
                else
                    MediaController.getTransportControls().skipToPrevious();
            }
        }
    }

    private SongWindow SW;

    private static MusicService.PlayerServiceBinder playerServiceBinder;
    private static MediaControllerCompat MediaController;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);

        SW = new SongWindow(this);

        bindService(new Intent(this, MusicService.class), new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                playerServiceBinder = (MusicService.PlayerServiceBinder) service;
                try {
                    MediaController = new MediaControllerCompat(
                            SongActivity.this, playerServiceBinder.getMediaSessionToken());
                    MediaController.registerCallback(
                            new MediaControllerCompat.Callback() {
                                @Override
                                public void onMetadataChanged(MediaMetadataCompat metadata)
                                {
                                    super.onMetadataChanged(metadata);
                                }

                                @Override
                                public void onPlaybackStateChanged(PlaybackStateCompat state)
                                {
                                    if (state == null)
                                        return;

                                    if(Objects.nonNull(state.getExtras()))
                                    {
                                        Bundle Msg = state.getExtras();

                                        if(Objects.nonNull(Msg.getString("Msg")))
                                        {
                                            String Info = Msg.getString("Msg");
                                            if(Info.equals("UpdateProgressBar"))
                                                SW.UpdateSongBar(Msg.getLong("Position"), Msg.getLong("Duration"));
                                            return;
                                        }
                                    }

                                    SW.UpdateStates(state);
                                }
                            }
                    );
                }
                catch (RemoteException e) {
                    MediaController = null;
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                playerServiceBinder = null;
                MediaController = null;
            }
        }, BIND_AUTO_CREATE);

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        MainActivity.GetApp().Resume();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        MainActivity.GetApp().Pause();
    }

}