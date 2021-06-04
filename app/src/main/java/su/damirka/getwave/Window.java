package su.damirka.getwave;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.icu.util.Calendar;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public abstract class Window {
    protected Drawable Clicked;
    protected Drawable Unclicked;

    Button Button;
    TextView Text;
    MainActivity M;
    Context C;


    Window(Button B, TextView T, MainActivity MA) {
        this.Button = B;
        this.Text = T;
        this.M = MA;
        this.C = MA.getApplicationContext();
    }


    abstract void Show();

    abstract void Hide();
}

class FindWindow extends Window
{
    @SuppressLint("UseCompatLoadingForDrawables")
    FindWindow(MainActivity MA)
    {
        super(MA.findViewById(R.id.FindButton), MA.findViewById(R.id.FindText), MA);

        Clicked = MA.getDrawable(R.drawable.findclicked);
        Unclicked = MA.getDrawable(R.drawable.findunclicked);
    }
    @Override
    public void Show()
    {
        M.findViewById(R.id.EnterText).setVisibility(View.GONE);
        Button.setBackground(Clicked);
        Text.setTextColor(Color.parseColor("#e5e5e5"));
    }

    @Override
    public void Hide()
    {
        Button.setBackground(Unclicked);
        Text.setTextColor(Color.parseColor("#888888"));
    }
}

class HomeWindow extends Window
{
    @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n"})
    HomeWindow(MainActivity MA)
    {
        super(MA.findViewById(R.id.HomeButton), MA.findViewById(R.id.HomeText), MA);

        Clicked = MA.getDrawable(R.drawable.homeclicked);
        Unclicked = MA.getDrawable(R.drawable.homeunclicked);

        TextView tv = MA.findViewById(R.id.EnterText);
        int h = android.icu.util.Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        DisplayMetrics DM = new DisplayMetrics();
        MA.getWindowManager().getDefaultDisplay().getMetrics(DM);

        tv.setTextSize(24);

        if (h >= 0 && h <= 6)
            tv.setText("Good night");
        else if (h > 6 && h <= 12)
            tv.setText("Good morning");
        else if (h > 12 && h <= 17)
            tv.setText("Good day");
        else if (h > 18 && h <= 23)
            tv.setText("Good evening");
    }

    @Override
    public void Show()
    {
        Button.setBackground(Clicked);
        Text.setTextColor(Color.parseColor("#e5e5e5"));
        M.findViewById(R.id.EnterText).setVisibility(View.VISIBLE);
    }

    @Override
    public void Hide()
    {
        Button.setBackground(Unclicked);
        Text.setTextColor(Color.parseColor("#888888"));
    }
}

class LibWindow extends Window
{
    @SuppressLint("UseCompatLoadingForDrawables")
    LibWindow(MainActivity MA) {
        super(MA.findViewById(R.id.LibButton), MA.findViewById(R.id.LibText), MA);

        Clicked = MA.getDrawable(R.drawable.libclicked);
        Unclicked = MA.getDrawable(R.drawable.libunclicked);
    }

    @Override
    void Show()
    {
        Button.setBackground(Clicked);
        Text.setTextColor(Color.parseColor("#e5e5e5"));
        M.findViewById(R.id.EnterText).setVisibility(View.GONE);
    }

    @Override
    void Hide()
    {
        Button.setBackground(Unclicked);
        Text.setTextColor(Color.parseColor("#888888"));
    }
}

