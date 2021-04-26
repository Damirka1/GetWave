package su.damirka.getwave;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

public class SongActivity extends AppCompatActivity implements Runnable
{

    static private TextView SongName;
    static private TextView SongAuthor;
    static private Button PlayButton;
    static private Button NextButton;
    static private Button PrevButton;
    static private ImageView Image;
    static private SeekBar SongBar;
    static private MusicPlayer MP;
    static private Thread thread;

    private void OnClick(View v)
    {
        int Id = v.getId();

        if(Id == R.id.BackButton)
        {
            finish();
        }
        else if(Id == R.id.PlayButton)
        {
            if(!MP.Playing)
            {
                Drawable d = getResources().getDrawable(android.R.drawable.ic_media_pause, null);
                ((Button)v).setBackground(d);
                MP.Play(d);
            }
            else
            {
                Drawable d = getResources().getDrawable(android.R.drawable.ic_media_play, null);
                ((Button)v).setBackground(d);
                MP.Pause(d);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);

        findViewById(R.id.BackButton).setOnClickListener(this::OnClick);
        findViewById(R.id.PlayButton).setOnClickListener(this::OnClick);

        SongName = findViewById(R.id.SongName);
        SongAuthor = findViewById(R.id.SongAuthor);
        PlayButton = findViewById(R.id.PlayButton);
        NextButton = findViewById(R.id.NextButton);
        PrevButton = findViewById(R.id.PrevButton);
        Image = findViewById(R.id.SongArt);
        SongBar = findViewById(R.id.SongBar);


        MP = MainActivity.GetMusicPlayer();

        if(MP.S.GetTitle() != null)
            SongName.setText(MP.S.GetTitle());
        else
            SongName.setText(MP.S.GetName());
        SongAuthor.setText(MP.S.GetAuthor());
        if(MP.S.HasImage())
            Image.setImageBitmap(MP.S.GetImage());
        else
            Image.setImageResource(R.drawable.musicicon);

        if(MP.Playing)
        {
            PlayButton.setBackground(getResources().getDrawable(android.R.drawable.ic_media_pause, null));
        }

        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run()
    {
        while(true)
        {
            SongBar.setMax(MP.GetDuration());
            SongBar.setProgress(MP.GetCurrPos(), true);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}