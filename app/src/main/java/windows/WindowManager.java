package windows;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Objects;

import su.damirka.getwave.R;
import su.damirka.getwave.activities.MainActivity;
import su.damirka.getwave.connection.ConnectionService;
import su.damirka.getwave.music.Song;
import su.damirka.getwave.views.playlists.find.FindLibManager;
import su.damirka.getwave.views.playlists.PlaylistManager;
import su.damirka.getwave.views.playlists.home.HomeLibManager;
import su.damirka.getwave.views.playlists.mylibrary.MyLibManager;

public class WindowManager
{
    private MainActivity MA;
    private final Button[] MenuButtons;
    private final ConstraintLayout MainLayout;
    private final SongMenu SM;

    private WindowView CurWindow;
    private PlaylistManager CurrManager;

    private HomeLibManager Home;
    private FindLibManager Find;
    private MyLibManager Lib;

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

        Home = new HomeLibManager(MainLayout);
        CurrManager = Home;
        CurrManager.Show();
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

    public void HomeClick(View v)
    {
        CurrManager.Hide();
        CurWindow.Hide();
        CurWindow = new HomeWindowView(MA);
        CurrManager = Home;
        CurWindow.Show();
        CurrManager.Show();
    }

    public void FindClick(View v)
    {
        CurrManager.Hide();
        CurWindow.Hide();
        CurWindow = new FindWindowView(MA);

        CurWindow.SetViewToHeader(R.layout.default_header_view);
        View HeaderView = CurWindow.GetHeaderView();

        TextView TextView = HeaderView.findViewById(R.id.Text);
        TextView.setTextSize(24);
        TextView.setText(R.string.MusicFromServer);

        CurWindow.Show();

        if(ConnectionService.CanConnect())
        {
            if(Objects.isNull(Find))
                Find = new FindLibManager(MainLayout);

            CurrManager = Find;
            CurrManager.Show();
        }
    }

    public void LibClick(View v)
    {
        CurrManager.Hide();
        CurWindow.Hide();
        CurWindow = new LibWindowView(MA);

        if(Objects.isNull(Lib))
            Lib = new MyLibManager(MainLayout);

        CurrManager = Lib;
        CurWindow.Show();
        CurrManager.Show();
    }

    public void UpdateProgressBar(int Position, int Duration)
    {
        SM.UpdateBar(Position, Duration);
    }

    public void UpdateStates(PlaybackStateCompat States)
    {
        SM.UpdateStates(States);
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

        public void UpdateStates(PlaybackStateCompat States)
        {
            this.Playing = States.getState() == PlaybackStateCompat.STATE_PLAYING;
            if(!Playing)
                PlayBtn.setBackground(Play);
            else
                PlayBtn.setBackground(Pause);

            Song Song = States.getExtras().getParcelable("Song");
            if(Objects.nonNull(Song))
                Update(Song);
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
            MA.startActivity(SongActivity);
        }

        public void OnClick(View v)
        {
            if(Playing)
            {
                PlayBtn.setBackground(Play);
                Playing = false;
                MainActivity.GetMediaController().getTransportControls().pause();
            }
            else
            {
                PlayBtn.setBackground(Pause);
                Playing = true;
                MainActivity.GetMediaController().getTransportControls().play();
            }
        }

        public void UpdateBar(int Position, int Duration)
        {
            SongBar.setMax(Duration);
            SongBar.setProgress(Position);
        }

        private void Update(Song s)
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
