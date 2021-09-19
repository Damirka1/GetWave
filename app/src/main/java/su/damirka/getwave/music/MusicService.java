package su.damirka.getwave.music;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.browse.MediaBrowser;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.service.media.MediaBrowserService;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Builder;
import androidx.media.session.MediaButtonReceiver;

import java.util.List;
import java.util.Objects;

import su.damirka.getwave.R;
import su.damirka.getwave.activities.SongActivity;

public class MusicService extends MediaBrowserService implements AudioManager.OnAudioFocusChangeListener
{
    private MusicPlayer _MusicPlayer;

    private Notification _Notification;
    private int NotificationId = 1337;
    private NotificationManager _NotificationManager;

    private final PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
            .setActions(
                              PlaybackStateCompat.ACTION_PLAY
                            | PlaybackStateCompat.ACTION_STOP
                            | PlaybackStateCompat.ACTION_PAUSE
                            | PlaybackStateCompat.ACTION_PLAY_PAUSE
                            | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                            | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);

    private static MediaSessionCompat _MediaSession;
    private final MediaSessionCompat.Callback _MediaSessionCallback = new MediaSessionCompat.Callback()
    {

        private void SetMetaData()
        {
            Song s = _MusicPlayer.GetCurrentSong();
            MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();
            metadataBuilder.putString(MediaMetadata.METADATA_KEY_TITLE, s.GetTitle())
                            .putString(MediaMetadata.METADATA_KEY_ALBUM, s.GetAuthor())
                            .putString(MediaMetadata.METADATA_KEY_ARTIST, s.GetAuthor())
                            .putLong(MediaMetadata.METADATA_KEY_DURATION, _MusicPlayer.GetDuration());

            MediaMetadataCompat metadata = metadataBuilder.build();
            _MediaSession.setMetadata(metadata);
            GenerateNotification(s, _MusicPlayer.IsPlaying());
        }

        private void UpdateState()
        {
            Bundle Msg = new Bundle();
            Msg.putParcelable("Song", _MusicPlayer.GetCurrentSong());
            Msg.putBoolean("Repeat", _MusicPlayer.IsRepeat());
            Msg.putLong("Index", _MusicPlayer.GetCurrentIndex());

            _MediaSession.setPlaybackState(
                    stateBuilder.setState(_MusicPlayer.IsPlaying() ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED,
                            _MusicPlayer.GetPosition(), 1)
                            .setExtras(Msg).build());
        }

        @Override
        public void onCustomAction(String action, Bundle extras)
        {
            super.onCustomAction(action, extras);

            if(action.equals("UpdateStates"))
            {
                UpdateState();
            }
            else if (action.equals("UpdatePlayList"))
            {
                Bundle Msg = new Bundle();
                Msg.putString("Msg", "UpdatePlayList");
                Msg.putParcelable("Playlist", _MusicPlayer.GetPlaylist());

                _MediaSession.setPlaybackState(
                        stateBuilder.setState(_MusicPlayer.IsPlaying() ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED,
                                _MusicPlayer.GetPosition(), 1)
                                .setExtras(Msg).build());
            }
            else if(action.equals("UpdateProgressBar"))
            {
                if(!_MusicPlayer.IsPlaying())
                    return;
                Bundle Msg = new Bundle();
                Msg.putString("Msg", "UpdateProgressBar");
                Msg.putLong("Duration", _MusicPlayer.GetDuration());
                Msg.putLong("Position", _MusicPlayer.GetPosition());
                _MediaSession.setPlaybackState(
                        stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                                _MusicPlayer.GetPosition(), 1)
                                .setExtras(Msg).build());
            }
        }

        @Override
        public boolean onMediaButtonEvent(Intent mediaButtonEvent)
        {
            return super.onMediaButtonEvent(mediaButtonEvent);
        }

        @Override
        public void onSetRepeatMode(int repeatMode)
        {
            super.onSetRepeatMode(repeatMode);

            if(repeatMode == 0)
                _MusicPlayer.SetRepeat(false);
            else
                _MusicPlayer.SetRepeat(true);
        }

        @Override
        public void onPlay()
        {
            super.onPlay();

            if(!successfullyRetrievedAudioFocus())
                return;

            _MusicPlayer.Play();
            SetMetaData();

            _MediaSession.setPlaybackState(
                    stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                            _MusicPlayer.GetPosition(), 1).build());
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras)
        {
            super.onPlayFromMediaId(mediaId, extras);
            _MusicPlayer.PlayAt(extras.getInt("Index"));
            SetMetaData();
            UpdateState();
        }

        @Override
        public void onSkipToNext()
        {
            super.onSkipToNext();
            _MusicPlayer.PlayNext();
            SetMetaData();
            UpdateState();
        }

        @Override
        public void onSkipToPrevious()
        {
            super.onSkipToPrevious();
            _MusicPlayer.PlayPrev();
            SetMetaData();
            UpdateState();
        }

        @Override
        public void onPause()
        {
            super.onPause();
            _MusicPlayer.Pause();
            SetMetaData();

            _MediaSession.setPlaybackState(
                    stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                            _MusicPlayer.GetPosition(), 1).build());
        }

        @Override
        public void onStop()
        {
            super.onStop();
            _MusicPlayer.Stop();

            _MediaSession.setPlaybackState(
                    stateBuilder.setState(PlaybackStateCompat.STATE_STOPPED,
                            0, 1).build());
        }

        @Override
        public void onPrepareFromMediaId(String mediaId, Bundle extras)
        {
            super.onPrepareFromMediaId(mediaId, extras);
            _MusicPlayer.SetPlaylist(extras.getParcelable("Playlist"));
        }

        @Override
        public void onSeekTo(long pos)
        {
            super.onSeekTo(pos);
            _MusicPlayer.SeekTo(pos);
        }

        @Override
        public void onCommand(String command, Bundle extras, ResultReceiver cb)
        {
            super.onCommand(command, extras, cb);
        }
    };

    private boolean successfullyRetrievedAudioFocus() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        AudioFocusRequest audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setOnAudioFocusChangeListener(this)
                .setAcceptsDelayedFocusGain(false)
                .setWillPauseWhenDucked(true)
                .setAudioAttributes(audioAttributes)
                .build();

        return audioManager.requestAudioFocus(audioFocusRequest) == AudioManager.AUDIOFOCUS_GAIN;
    }

    @Override
    public void onCreate()
    {
        _MediaSession = new MediaSessionCompat(getApplicationContext(), "Music Session Tag");
        _MediaSession.setCallback(_MediaSessionCallback);
        _MediaSession.setActive(true);

        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setClass(this, MediaButtonReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
        _MediaSession.setMediaButtonReceiver(pendingIntent);

        _MusicPlayer = new MusicPlayer(getApplicationContext(), _MediaSession.getController().getTransportControls());
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void GenerateNotification(Song Song, boolean Playing)
    {
        if (_NotificationManager == null) {
            _NotificationManager = (android.app.NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

            _NotificationManager.createNotificationChannelGroup(new NotificationChannelGroup("chats_group", "Chats"));
            NotificationChannel NotificationChannel = new NotificationChannel("service_channel", "Service Notifications",
                    NotificationManager.IMPORTANCE_LOW);

            NotificationChannel.enableLights(false);
            NotificationChannel.setShowBadge(false);
            NotificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            _NotificationManager.createNotificationChannel(NotificationChannel);
        }

        Intent intentMainLanding = new Intent(this, SongActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, intentMainLanding, 0);

        Builder builder = new NotificationCompat.Builder(this, "service_channel");

        builder.setContentTitle(Song.GetTitle())
                .setContentText(Song.GetAuthor())
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setShowWhen(false)
                .setWhen(0)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setColorized(true);

        Intent PlayPrevIntent = new Intent(getApplicationContext(), PlayPrevButton.class);
        PendingIntent PrevButton = PendingIntent.getBroadcast(getApplicationContext(), 0, PlayPrevIntent, 0);

        Intent PlayIntent = new Intent(getApplicationContext(), PlayButton.class);
        PendingIntent PlayButton = PendingIntent.getBroadcast(getApplicationContext(), 0, PlayIntent, 0);

        Intent PauseIntent = new Intent(getApplicationContext(), PauseButton.class);
        PendingIntent PauseButton = PendingIntent.getBroadcast(getApplicationContext(), 0, PauseIntent, 0);

        Intent PlayNextIntent = new Intent(getApplicationContext(), PlayNextButton.class);
        PendingIntent PlayNextButton = PendingIntent.getBroadcast(getApplicationContext(), 0, PlayNextIntent, 0);

        NotificationCompat.Action Play;
        if(Playing)
            Play = new NotificationCompat.Action.Builder(R.drawable.pause32x32, null, PauseButton).build();
        else
            Play = new NotificationCompat.Action.Builder(R.drawable.play32x32, null, PlayButton).build();

        builder.addAction(R.drawable.playprev32x32, null,  PrevButton)      // #0
                .addAction(Play)           // #1
                .addAction(R.drawable.playnext32x32, null, PlayNextButton);       // #2

        builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0,1,2)
                        .setMediaSession(_MediaSession.getSessionToken())
        );

        if(Song.HasArt())
            builder.setLargeIcon(Song.GetArt());
        else
            builder.setLargeIcon(((BitmapDrawable)getDrawable(R.drawable.musicicon)).getBitmap());


        _Notification = builder.build();
        startForeground(NotificationId, _Notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        _MusicPlayer.Release();
        _MediaSession.setActive(false);
        _MediaSession.release();
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new PlayerServiceBinder();
    }

    public class PlayerServiceBinder extends Binder {
        public MediaSessionCompat.Token getMediaSessionToken() {
            return _MediaSession.getSessionToken();
        }
    }

    public static class PlayNextButton extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            _MediaSession.getController().getTransportControls().skipToNext();
        }
    }

    public static class PauseButton extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            _MediaSession.getController().getTransportControls().pause();
        }
    }

    public static class PlayButton extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            _MediaSession.getController().getTransportControls().play();
        }
    }

    public static class PlayPrevButton extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            _MediaSession.getController().getTransportControls().skipToPrevious();
        }
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints)
    {
        return null;
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowser.MediaItem>> result)
    {

    }

    @Override
    public void onAudioFocusChange(int focusChange)
    {
        switch( focusChange ) {
            case AudioManager.AUDIOFOCUS_LOSS: {
                if( _MusicPlayer.IsPlaying() ) {
                    _MusicPlayer.Stop();
                }
                break;
            }
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: {
                _MusicPlayer.Pause();
                break;
            }
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: {
                if( _MusicPlayer != null ) {
                    _MusicPlayer.SetVolume(0.3f, 0.3f);
                }
                break;
            }
            case AudioManager.AUDIOFOCUS_GAIN: {
                if( _MusicPlayer != null ) {
                    if( !_MusicPlayer.IsPlaying() ) {
                        _MusicPlayer.Play();
                    }
                    _MusicPlayer.SetVolume(1.0f, 1.0f);
                }
                break;
            }
        }
    }
}
