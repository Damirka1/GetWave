package windows;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.icu.util.Calendar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import su.damirka.getwave.R;
import su.damirka.getwave.activities.MainActivity;

public abstract class WindowView {
    protected HeaderView Header;
    protected Drawable Clicked;
    protected Drawable Unclicked;

    protected Button Button;
    protected TextView Text;
    protected MainActivity MA;
    protected Context C;


    WindowView(Button B, TextView T, MainActivity MA) {
        this.Button = B;
        this.Text = T;
        this.MA = MA;
        this.C = MA.getApplicationContext();
        Header = new HeaderView(MA);
    }


    abstract void Show();

    abstract void Hide();
}

class FindWindowView extends WindowView
{
    @SuppressLint("UseCompatLoadingForDrawables")
    FindWindowView(MainActivity MA)
    {
        super(MA.findViewById(R.id.FindButton), MA.findViewById(R.id.FindText), MA);

        Clicked = MA.getDrawable(R.drawable.findclicked);
        Unclicked = MA.getDrawable(R.drawable.findunclicked);
    }
    @Override
    public void Show()
    {
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

class HomeWindowView extends WindowView
{
    @SuppressLint({"UseCompatLoadingForDrawables"})
    HomeWindowView(MainActivity MA)
    {
        super(MA.findViewById(R.id.HomeButton), MA.findViewById(R.id.HomeText), MA);

        Clicked = MA.getDrawable(R.drawable.homeclicked);
        Unclicked = MA.getDrawable(R.drawable.homeunclicked);
        Header.SetViewById(R.layout.home_header_view);
    }

    @SuppressLint("SetTextI18n")
    private void CalculateTimeForTextView(TextView tv)
    {
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
        View view = Header.GetCurrentView();

        TextView textView = view.findViewById(R.id.EnterText);

        CalculateTimeForTextView(textView);
        Header.Show();
    }

    @Override
    public void Hide()
    {
        Button.setBackground(Unclicked);
        Text.setTextColor(Color.parseColor("#888888"));
        Header.Hide();
    }
}

class LibWindowView extends WindowView
{
    @SuppressLint("UseCompatLoadingForDrawables")
    LibWindowView(MainActivity MA) {
        super(MA.findViewById(R.id.LibButton), MA.findViewById(R.id.LibText), MA);

        Clicked = MA.getDrawable(R.drawable.libclicked);
        Unclicked = MA.getDrawable(R.drawable.libunclicked);
    }

    @Override
    void Show()
    {
        Button.setBackground(Clicked);
        Text.setTextColor(Color.parseColor("#e5e5e5"));
    }

    @Override
    void Hide()
    {
        Button.setBackground(Unclicked);
        Text.setTextColor(Color.parseColor("#888888"));
    }
}

