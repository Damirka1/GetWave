package su.damirka.getwave.views.playlists;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.gridlayout.widget.GridLayout;

public abstract class PlaylistManager
{
    protected ConstraintLayout MainLayout;

    protected PlaylistManager(ConstraintLayout MainLayout)
    {
        this.MainLayout = MainLayout;
    }

    public abstract void Show();
    public abstract void Hide();
}
