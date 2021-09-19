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
import android.widget.GridLayout;
import android.widget.TextView;

import su.damirka.getwave.R;
import su.damirka.getwave.activities.MainActivity;
import su.damirka.getwave.views.PlaylistView;

public abstract class WindowView {
    private HeaderView Header;
    private Drawable Clicked;
    private Drawable Unclicked;

    private Button Button;
    private TextView Text;
    private MainActivity MA;
    private Context C;

    WindowView(Button B, TextView T, MainActivity MA) {
        this.Button = B;
        this.Text = T;
        this.MA = MA;
        this.C = MA.getApplicationContext();
        Header = new HeaderView(MA);
    }

    protected void SetClickedDrawable(Drawable dr)
    {
        Clicked = dr;
    }

    protected void SetUnClickedDrawable(Drawable dr)
    {
        Unclicked = dr;
    }

    protected Drawable GetClicked()
    {
        return Clicked;
    }

    protected Drawable GetUnclicked()
    {
        return Unclicked;
    }

    protected void SetButtonClicked()
    {
        Button.setBackground(Clicked);
    }

    protected void SetButtonUnclicked()
    {
        Button.setBackground(Unclicked);
    }

    protected TextView GetText()
    {
        return Text;
    }

    protected Context GetContext()
    {
        return C;
    }

    protected void HideHeader()
    {
        Header.Hide();
    }

    protected void ShowHeader()
    {
        Header.Show();
    }

    protected void SetViewToHeader(int id)
    {
        Header.SetViewById(id);
    }

    protected View GetHeaderView()
    {
        return Header.GetCurrentView();
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

        SetClickedDrawable(MA.getDrawable(R.drawable.findclicked));
        SetUnClickedDrawable(MA.getDrawable(R.drawable.findunclicked));
    }
    @Override
    public void Show()
    {
        SetButtonClicked();
        GetText().setTextColor(Color.parseColor("#e5e5e5"));
        ShowHeader();
    }

    @Override
    public void Hide()
    {
        SetButtonUnclicked();
        GetText().setTextColor(Color.parseColor("#888888"));
        HideHeader();
    }
}

class HomeWindowView extends WindowView
{
    @SuppressLint({"UseCompatLoadingForDrawables"})
    HomeWindowView(MainActivity MA)
    {
        super(MA.findViewById(R.id.HomeButton), MA.findViewById(R.id.HomeText), MA);

        SetClickedDrawable(MA.getDrawable(R.drawable.homeclicked));
        SetUnClickedDrawable(MA.getDrawable(R.drawable.homeunclicked));
        SetViewToHeader(R.layout.home_header_view);
    }

    private void CalculateTimeForTextView(TextView tv)
    {
        int h = android.icu.util.Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        tv.setTextSize(24);

        if (h >= 0 && h <= 6)
            tv.setText(R.string.GoodNight);
        else if (h >= 7 && h <= 12)
            tv.setText(R.string.GoodMorning);
        else if (h >= 13 && h <= 17)
            tv.setText(R.string.GoodDay);
        else if (h >= 18 && h <= 23)
            tv.setText(R.string.GoodEvening);
    }

    @Override
    public void Show()
    {
        SetButtonClicked();
        GetText().setTextColor(Color.parseColor("#e5e5e5"));

        View view = GetHeaderView();
        TextView textView = view.findViewById(R.id.EnterText);
        CalculateTimeForTextView(textView);

        ShowHeader();
    }

    @Override
    public void Hide()
    {
        SetButtonUnclicked();
        GetText().setTextColor(Color.parseColor("#888888"));
        HideHeader();
    }
}

class LibWindowView extends WindowView
{
    @SuppressLint("UseCompatLoadingForDrawables")
    LibWindowView(MainActivity MA) {
        super(MA.findViewById(R.id.LibButton), MA.findViewById(R.id.LibText), MA);

        SetClickedDrawable(MA.getDrawable(R.drawable.libclicked));
        SetUnClickedDrawable(MA.getDrawable(R.drawable.libunclicked));

        SetViewToHeader(R.layout.home_header_view);
    }

    @Override
    void Show()
    {
        SetButtonClicked();
        GetText().setTextColor(Color.parseColor("#e5e5e5"));

        View view = GetHeaderView();
        TextView textView = view.findViewById(R.id.EnterText);
        textView.setTextSize(24);
        textView.setText(R.string.YourLibrary);

        ShowHeader();
    }

    @Override
    void Hide()
    {
        SetButtonUnclicked();
        GetText().setTextColor(Color.parseColor("#888888"));
        HideHeader();
    }
}

