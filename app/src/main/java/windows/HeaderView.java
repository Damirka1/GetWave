package windows;

import android.view.LayoutInflater;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Objects;

import su.damirka.getwave.R;
import su.damirka.getwave.activities.MainActivity;

public class HeaderView {

    private MainActivity MA;
    private ConstraintLayout HeaderLayout;
    private View CurrentView;
    private boolean Showing;

    public HeaderView(MainActivity MA)
    {
        this.MA  = MA;
        HeaderLayout = MA.findViewById(R.id.HeaderLayout);
    }

    public View SetViewById(int id)
    {
        if(Objects.nonNull(CurrentView))
        {
            HeaderLayout.removeView(CurrentView);
            CurrentView = null;
        }

        try {
            CurrentView = LayoutInflater.from(HeaderLayout.getContext()).inflate(id, HeaderLayout, false);
            Showing = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CurrentView;
    }

    public View GetCurrentView()
    {
        return CurrentView;
    }

    public void Show()
    {
        if(!Showing)
        {
            HeaderLayout.addView(CurrentView);
            Showing = true;
        }
    }

    public void Hide()
    {
        if(Showing)
        {
            HeaderLayout.removeView(CurrentView);
            Showing = false;
        }
    }

}
