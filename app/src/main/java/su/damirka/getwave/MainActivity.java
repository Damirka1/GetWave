package su.damirka.getwave;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.icu.util.Calendar;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements Runnable
{
    private Intent SongActivity;

    static private Window CurrWindow;
    static private MusicPlayer MP;

    static private HomeWindow HW;
    static private FindWindow FW;
    static private LibWindow LW;

    static private ConstraintLayout MainLayout;
    static private ConstraintLayout SongMenu;
    static private boolean FirstTime = true;
    static private Thread thread;
    static private ProgressBar SongBarMenu;

    static private final int REQUEST_EXTERNAL_STORAGE = 1;
    static private String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    // Used to load the 'native-lib' library on application startup.
    static
    {
        System.loadLibrary("native-lib");
    }


    static public MusicPlayer GetMusicPlayer()
    {
        return MP;
    }

    private void ChangeWindow(Window To)
    {
        Resources Rs = getResources();
        CurrWindow.Hide(Rs);
        CurrWindow = To;
        CurrWindow.Show(Rs);
    }


    public void OnClick(View v)
    {
        int Id = v.getId();

        if(CurrWindow.IdIsEqual(Id))
            return;
        else if(Id == R.id.HomeButton)
            ChangeWindow(HW);
        else if(Id == R.id.FindButton)
            ChangeWindow(FW);
        else if(Id == R.id.LibButton)
            ChangeWindow(LW);
        else if(Id == R.id.SongMenu)
            startActivity(SongActivity);
        else if(Id == R.id.PlayMenu)
        {
            if(MP.Playing)
                MP.Pause(getDrawable(android.R.drawable.ic_media_play));
            else
                MP.Play(getDrawable(android.R.drawable.ic_media_pause));
        }
    }





    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tv = findViewById(R.id.EnterText);
        Date d = new Date();
        int h = android.icu.util.Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        DisplayMetrics DM = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(DM);

        tv.setTextSize(24);

        if(h >= 0 && h <= 6)
            tv.setText("Good night");
        else if(h > 6 && h <= 12)
            tv.setText("Good morning");
        else if(h > 12 && h <= 17 )
            tv.setText("Good day");
        else if(h > 18 && h <= 23)
            tv.setText("Good evening");

        if(FirstTime)
        {
            HW = new HomeWindow(this);
            FW = new FindWindow(this);
            LW = new LibWindow(this);

            try {
                MP = new MusicPlayer(this);
            } catch (IOException e) {
                e.printStackTrace();
            }

            SongActivity = new Intent(MainActivity.this, SongActivity.class);

            MainLayout = findViewById(R.id.MainLayout);
            FirstTime = false;
            CurrWindow = HW;

            SongMenu = findViewById(R.id.SongMenu);
            SongBarMenu = SongMenu.findViewById(R.id.SongBarMenu);
            SongBarMenu.setMin(0);
        }
        else
        {
            HW.Update(this);
            FW.Update(this);
            LW.Update(this);
        }

        CurrWindow.Show(getResources());
        findViewById(R.id.SongMenu).setOnClickListener(this::OnClick);
        findViewById(R.id.PlayMenu).setOnClickListener(this::OnClick);

        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }

        thread = new Thread(this);

        thread.start();
    }

    public static Songs GetSongsFromFindWindow()
    {
        return FW.GetSongs();
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    @Override
    public void run()
    {
        while(!SongMenu.isShown())
        {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        while(true)
        {
            SongBarMenu.setMax(MP.GetDuration());
            SongBarMenu.setProgress(MP.GetCurrPos(), true);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}