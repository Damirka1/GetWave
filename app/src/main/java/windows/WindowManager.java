package windows;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Optional;

import su.damirka.getwave.Application;
import su.damirka.getwave.R;
import su.damirka.getwave.activities.MainActivity;
import su.damirka.getwave.connection.ConnectionService;
import su.damirka.getwave.music.Playlist;
import su.damirka.getwave.music.Song;
import su.damirka.getwave.views.PlaylistView;

public class WindowManager
{
    private MainActivity MA;
    private final Button[] MenuButtons;
    private final ConstraintLayout MainLayout;
    private final SongMenu SM;

    private WindowView CurWindow;

    private PlaylistView PlaylistView;

    public WindowManager(MainActivity MA)
    {
        this.MA = MA;
        CurWindow = new HomeWindowView(MA);
        CurWindow.Show();

        MenuButtons = new Button[]{MA.findViewById(R.id.HomeButton), MA.findViewById(R.id.FindButton), MA.findViewById(R.id.LibButton)};

        MenuButtons[0].setOnClickListener(this::HomeClick);
        MenuButtons[1].setOnClickListener(this::FindClick);
        MenuButtons[2].setOnClickListener(this::LibClick);
        SM = new SongMenu();
        MainLayout = MA.findViewById(R.id.MainLayout);

        PlaylistView = new PlaylistView(null, MA.getApplicationContext(), MainLayout);
    }

    public void Release()
    {
        MA = null;

        for (int i = 0; i < MenuButtons.length; i++)
        {
            MenuButtons[i].setOnClickListener(null);
            MenuButtons[i] = null;
        }

        CurWindow = null;
    }

    public boolean IsPlaying()
    {
        return SM.Playing;
    }

    public void HomeClick(View v)
    {
        HidePlaylist();
        CurWindow.Hide();
        CurWindow = new HomeWindowView(MA);
        CurWindow.Show();
    }

    public void FindClick(View v)
    {
        CurWindow.Hide();
        CurWindow = new FindWindowView(MA);
        CurWindow.Show();

        if(!PlaylistView.IsInitialized())
        {
            int Index = ConnectionService.StartDownloadingPlaylistWithAllMusic();

            Optional<Playlist> Value = ConnectionService.GetDownloadedPlaylist(Index);
            while(!Value.isPresent())
            {
                try
                {
                    Thread.sleep(100);
                    Value = ConnectionService.GetDownloadedPlaylist(Index);
                }
                catch (InterruptedException interruptedException)
                {
                    interruptedException.printStackTrace();
                }
            }

            PlaylistView.SetPlaylist(Value.get());

            Bundle Msg = new Bundle();
            Msg.putString("Msg", "Playlist");
            Msg.putParcelable("Playlist", Value.get());
            MainActivity.SendMsgToMusicService(Msg);
        }
        PlaylistView.Show();

    }

    private void HidePlaylist()
    {
        if(PlaylistView.IsInitialized())
            PlaylistView.Hide();
    }

    public void LibClick(View v)
    {
        HidePlaylist();
        CurWindow.Hide();
        CurWindow = new LibWindowView(MA);
        CurWindow.Show();
    }

    public Song GetSongByIndex(long Index)
    {
        return PlaylistView.GetSongByIndex(Index);
    }

    public void UpdateProgressBar(int Position, int Duration)
    {
        SM.UpdateBar(Position, Duration);
    }

    public void Update(long Index)
    {
        SM.Update(PlaylistView.GetSongByIndex(Index));
        PlaylistView.Update();
    }

    public void UpdateStates(Bundle States)
    {
        SM.UpdateStates(States.getBoolean("Playing"));
    }

    private class SongMenu
    {
        private final ConstraintLayout SongMenu;
        private final TextView Title;
        private final TextView Author;
        private final ImageView Image;
        private final Button PlayBtn;
        private final ProgressBar SongBar;

        private final Drawable Play;
        private final Drawable Pause;

        private boolean Playing;
        private boolean Visible;

        private Song CurrentSong;

        @SuppressLint("UseCompatLoadingForDrawables")
        SongMenu()
        {
            SongMenu = MA.findViewById(R.id.SongMenu);
            Title = SongMenu.findViewById(R.id.TitleMenu);
            Author = SongMenu.findViewById(R.id.AuthorMenu);
            Image = SongMenu.findViewById(R.id.ImageMenu);

            SongBar = SongMenu.findViewById(R.id.SongBarMenu);

            PlayBtn = SongMenu.findViewById(R.id.PlayMenu);

            Play = MA.getDrawable(R.drawable.play);
            Pause = MA.getDrawable(R.drawable.pause);

            Playing = false;
            Visible = false;
        }

        public void UpdateStates(boolean Playing)
        {
            this.Playing = Playing;
            if(!Playing)
                PlayBtn.setBackground(Play);
            else
                PlayBtn.setBackground(Pause);
        }

        public void Show()
        {
            SongMenu.setVisibility(View.VISIBLE);
            PlayBtn.setOnClickListener(this::OnClick);
            SongMenu.setOnClickListener(this::OpenSongActivity);
            Visible = true;
            Playing = true;
        }

        public void Hide()
        {
            SongMenu.setVisibility(View.GONE);
            Visible = false;
            Playing = false;
        }

        public void OpenSongActivity(View v)
        {
            Intent SongActivity = new Intent(MA, su.damirka.getwave.activities.SongActivity.class);

            Bundle Data = new Bundle();

            Data.putByte("Playing", (byte) (Playing ? 1 : 0));
            Data.putInt("Position", SongBar.getProgress());
            Data.putInt("Duration", SongBar.getMax());
            SongActivity.putExtras(Data);

            MA.startActivity(SongActivity);
        }

        public void OnClick(View v)
        {
            if(Playing)
            {
                PlayBtn.setBackground(Play);
                Playing = false;
                Bundle Msg = new Bundle();
                Msg.putString("Msg", "Pause");
                MainActivity.SendMsgToMusicService(Msg);
            }
            else
            {
                PlayBtn.setBackground(Pause);
                Playing = true;
                Bundle Msg = new Bundle();
                Msg.putString("Msg", "Play");
                MainActivity.SendMsgToMusicService(Msg);
            }
        }

        public void UpdateBar(int Position, int Duration)
        {
            SongBar.setMax(Duration);
            SongBar.setProgress(Position);
        }

        public void Update(Song s)
        {
            CurrentSong = s;
            if(!Visible)
            {
                Show();
                Visible = true;
            }

            if(s.GetTitle() != null)
                Title.setText(s.GetTitle());
            else
                Title.setText(R.string.UndefinedTitle);

            if(s.GetAuthor() != null)
                Author.setText(s.GetAuthor());
            else
                Author.setText(R.string.UndefinedAuthor);

            if(s.HasArt())
                Image.setImageBitmap(s.GetArt());
            else
                Image.setImageResource(R.drawable.musicicon);
        }

    }
}
