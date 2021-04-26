package su.damirka.getwave;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class Window
{
    Button Button = null;
    TextView Text = null;

    Window(Button B, TextView T, MainActivity MA)
    {
        Update(B, T, MA);
    }

    void Update(Button B, TextView T, MainActivity MA)
    {
        this.Button = B;
        this.Text = T;
        this.Button.setOnClickListener(MA::OnClick);
    }

    boolean IdIsEqual(int ID)
    {
        return Button.getId() == ID;
    }

    abstract void Show(Resources res);
    abstract void Hide(Resources res);
}

class FindWindow extends Window
{
    private RecyclerView RView;
    private RecyclerView.LayoutManager LayoutManager;
    private ConstraintLayout Layout;

    private Songs S = null;

    FindWindow(MainActivity MA)
    {
        super(MA.findViewById(R.id.FindButton), MA.findViewById(R.id.FindText), MA);

        S = new Songs();
        RView = new RecyclerView(MA.getApplicationContext());
        RView.setHasFixedSize(true);
        LayoutManager = new LinearLayoutManager(MA.getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        RView.setLayoutManager(LayoutManager);
        RView.setAdapter(S);
        Layout = MA.findViewById(R.id.MainLayout);
    }

    Songs GetSongs()
    {
        return S;
    }

    void Update(MainActivity MA)
    {
        super.Update( MA.findViewById(R.id.FindButton), MA.findViewById(R.id.FindText), MA);
    }

    @Override
    public void Show(Resources res)
    {
        Button.setBackground(res.getDrawable(R.drawable.findclicked, null));
        Text.setTextColor(Color.parseColor("#d3d3d3"));
        Layout.addView(RView);
    }

    @Override
    public void Hide(Resources res)
    {
        Button.setBackground(res.getDrawable(R.drawable.findunclicked, null));
        Text.setTextColor(Color.parseColor("#888888"));
        Layout.removeView(RView);
    }
}

class HomeWindow extends Window
{

    HomeWindow(MainActivity MA)
    {
        super(MA.findViewById(R.id.HomeButton), MA.findViewById(R.id.HomeText), MA);
    }

    void Update(MainActivity MA)
    {
        super.Update( MA.findViewById(R.id.HomeButton), MA.findViewById(R.id.HomeText), MA);
    }

    @Override
    public void Show(Resources res)
    {
        Button.setBackground(res.getDrawable(R.drawable.homeclicked, null));
        Text.setTextColor(Color.parseColor("#d3d3d3"));
    }

    @Override
    public void Hide(Resources res)
    {
        Button.setBackground(res.getDrawable(R.drawable.homeunclicked, null));
        Text.setTextColor(Color.parseColor("#888888"));
    }
}

class LibWindow extends Window
{

    LibWindow(MainActivity MA)
    {
        super(MA.findViewById(R.id.LibButton), MA.findViewById(R.id.LibText), MA);
    }

    void Update(MainActivity MA)
    {
        super.Update(MA.findViewById(R.id.LibButton), MA.findViewById(R.id.LibText), MA);
    }

    @Override
    void Show(Resources res)
    {
        Button.setBackground(res.getDrawable(R.drawable.libclicked, null));
        Text.setTextColor(Color.parseColor("#d3d3d3"));
    }

    @Override
    void Hide(Resources res)
    {
        Button.setBackground(res.getDrawable(R.drawable.libunclicked, null));
        Text.setTextColor(Color.parseColor("#888888"));
    }
}

