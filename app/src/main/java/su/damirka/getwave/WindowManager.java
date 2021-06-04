package su.damirka.getwave;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WindowManager
{
    private final Application App;
    private MainActivity MA;
    private final Button[] MenuButtons;


    private Playlist PL;

    private Window CurWindow;
    private SongMenu SM;

    public WindowManager(Application App, MainActivity ma)
    {
        this.App = App;
        this.MA = ma;
        CurWindow = new HomeWindow(MA);
        CurWindow.Show();

        MenuButtons = new Button[]{MA.findViewById(R.id.HomeButton), MA.findViewById(R.id.FindButton), MA.findViewById(R.id.LibButton)};

        MenuButtons[0].setOnClickListener(this::HomeClick);
        MenuButtons[1].setOnClickListener(this::FindClick);
        MenuButtons[2].setOnClickListener(this::LibClick);

        SM = new SongMenu();
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
        HidePlaylist();
        CurWindow.Hide();
        CurWindow = new HomeWindow(MA);
        CurWindow.Show();
    }

    public void FindClick(View v)
    {
        CurWindow.Hide();
        CurWindow = new FindWindow(MA);
        CurWindow.Show();

        if(PL == null)
        {
            PL = new Playlist(MA.getApplicationContext(), MA.findViewById(R.id.MainLayout), FindAllMusic());
            App.SetPlaylist(PL);
        }

        PL.Show();

    }

    private void HidePlaylist()
    {
        if(PL != null)
            PL.Hide();
    }

    public void LibClick(View v)
    {
        HidePlaylist();
        CurWindow.Hide();
        CurWindow = new LibWindow(MA);
        CurWindow.Show();
    }

    public String[] FindAllMusic()
    {
        return GetAllMusic(Environment.getExternalStorageDirectory().toString() + "/Music");

//        File Directory = new File(Environment.getExternalStorageDirectory().toString() + "/Music");
//        File[] Files = Directory.listFiles();
//        List<String> S = new ArrayList<>();
//
//        for (File File : Files)
//        {
//            String Name = File.getName();
//            String extension = Name.substring(Name.lastIndexOf("."));
//
//            if(extension.contentEquals(".mp3"))
//                S.add(File.getAbsolutePath());
//        }
//
//        return S;
    }

    public native String[] GetAllMusic(String Path);

    public void ShowSongMenu(Songs.Song s)
    {
        SM.Show();
    }

    public void UpdateSongMenu(Songs.Song s, int MaxDuration)
    {
        SM.Update(s, MaxDuration);
    }

    public void UpdateSongBar(int Duration)
    {
        SM.UpdateBar(Duration);
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
            Intent SongActivity = new Intent(MA, SongActivity.class);
            MA.startActivity(SongActivity);
        }

        public void OnClick(View v)
        {
            if(Playing)
            {
                PlayBtn.setBackground(Play);
                Playing = false;
                App.Pause();
            }
            else
            {
                PlayBtn.setBackground(Pause);
                Playing = true;
                App.Play();
            }
        }

        public void UpdateBar(int Dur)
        {
            SongBar.setProgress(Dur);
        }

        public void Update(Songs.Song s, int Duration)
        {
            if(!Visible)
            {
                Show();
                Visible = true;
            }

            SongBar.setMax(Duration);

            if(s.GetTitle() != null)
                Title.setText(s.GetTitle());
            else
                Title.setText(s.GetName());

            Author.setText(s.GetAuthor());

            if(s.HasImage())
                Image.setImageBitmap(s.GetImage());
            else
                Image.setImageResource(R.drawable.musicicon);
        }

    }
}
