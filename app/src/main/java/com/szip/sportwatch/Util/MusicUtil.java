package com.szip.sportwatch.Util;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.service.notification.StatusBarNotification;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.raizlabs.android.dbflow.list.IFlowCursorIterator;
import com.szip.sportwatch.BLE.BleClient;
import com.szip.sportwatch.Notification.MyNotificationReceiver;
import com.szip.sportwatch.Notification.NotificationReceiver;
import com.szip.sportwatch.Service.MainService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import static android.media.AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE;
import static android.media.AudioManager.FLAG_SHOW_UI;
import static android.media.AudioManager.STREAM_MUSIC;

public class MusicUtil{
    private static MusicUtil musicUtil;

    private String packageName = "";
    private Context mContext;
    private String musicTitle="";
    private String musicSinger="";
    private boolean playState,isRegister = false;
    private AudioManager audioManager;
    private NotifyReceiver mNotifyReceiver = new NotifyReceiver();
    private MediaSessionManager mediaSessionManager;
    private ComponentName mNotifyReceiveService;

    public static MusicUtil getSingle() {
        if (musicUtil == null) {
            synchronized (MusicUtil.class) {
                if (musicUtil == null) {
                    musicUtil = new MusicUtil();
                }
            }
        }
        return musicUtil;
    }

    public void init(Context mContext) {
        this.mContext = mContext;
        audioManager = (AudioManager)this.mContext.getSystemService(Context.AUDIO_SERVICE);
        if(Build.VERSION.SDK_INT >= 21)
            mediaSessionManager = (MediaSessionManager) mContext.getSystemService(Context.MEDIA_SESSION_SERVICE);
        mNotifyReceiveService = new ComponentName(mContext, MyNotificationReceiver.class);
    }

    public void setVoice(int voiceValue){
        audioManager.adjustStreamVolume(STREAM_MUSIC, voiceValue, FLAG_SHOW_UI|FLAG_REMOVE_SOUND_AND_VIBRATE);
    }

    public void setVoiceValue(int value){
        int max = audioManager.getStreamMaxVolume(STREAM_MUSIC);
        Log.i("data******","max volume = "+max);
        value = max/15*value;
        audioManager.setStreamVolume(STREAM_MUSIC, value, FLAG_SHOW_UI|FLAG_REMOVE_SOUND_AND_VIBRATE);
    }

    public void controlMusic(int i) {
        switch (i){
            case 126:
                playOrPause(false);
                break;
            case 127:
                playOrPause(true);
                break;
            case 87:
                nexMusic();
                break;
            case 88:
                lastMusic();
                break;
        }
    }

    private void nexMusic(){
        if(isUseAudioManagerKey()){
            AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            audioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT));
            audioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT));
        }else{
            MediaControllerCompat controllerCompat = findMediaControl();
            if(controllerCompat != null){
                boolean isDown = controllerCompat.dispatchMediaButtonEvent(new KeyEvent(KeyEvent.KEYCODE_MEDIA_NEXT, KeyEvent.ACTION_DOWN));
                boolean isUp = controllerCompat.dispatchMediaButtonEvent(new KeyEvent(KeyEvent.KEYCODE_MEDIA_NEXT, KeyEvent.ACTION_UP));
                boolean isSucc = isDown && isUp;
                if(!isSucc){
                    MediaControllerCompat.TransportControls transportControls = controllerCompat.getTransportControls();
                    if(transportControls != null)
                        transportControls.skipToNext();
                }
            }
        }
    }


    private void lastMusic(){
        if(isUseAudioManagerKey()){
            AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            audioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS));
            audioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PREVIOUS));
        }else{
            MediaControllerCompat controllerCompat = findMediaControl();
            if(controllerCompat != null){
                boolean isDown = controllerCompat.dispatchMediaButtonEvent(new KeyEvent(KeyEvent.KEYCODE_MEDIA_PREVIOUS, KeyEvent.ACTION_DOWN));
                boolean isUp = controllerCompat.dispatchMediaButtonEvent(new KeyEvent(KeyEvent.KEYCODE_MEDIA_PREVIOUS, KeyEvent.ACTION_UP));
                boolean isSucc = isDown && isUp;
                if(!isSucc){
                    MediaControllerCompat.TransportControls transportControls = controllerCompat.getTransportControls();
                    if(transportControls != null)
                        transportControls.skipToPrevious();
                }
            }
        }

    }


    private void playOrPause(boolean isPlay){
        if(isUseAudioManagerKey()){
            AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            audioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
            audioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
        }else{
            MediaControllerCompat controllerCompat = findMediaControl();
            if(controllerCompat != null){
                boolean isDown = controllerCompat.dispatchMediaButtonEvent(new KeyEvent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.ACTION_DOWN));
                boolean isUp = controllerCompat.dispatchMediaButtonEvent(new KeyEvent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.ACTION_UP));
                boolean isSucc = isDown && isUp;
                if(!isSucc){
                    MediaControllerCompat.TransportControls transportControls = controllerCompat.getTransportControls();
                    if(transportControls != null){
                        if(isPlay)
                            transportControls.pause();
                        else
                            transportControls.play();
                    }

                }
            }
        }

    }


    private boolean isUseAudioManagerKey(){
        switch (packageName){
            case "com.tencent.qqmusic":
                return true;
        }
        return false;
    }


    public MediaControllerCompat findMediaControl(){
        try{
            List<MediaController> mediaControllers = mediaSessionManager.getActiveSessions(mNotifyReceiveService);
            for(MediaController controller : mediaControllers){
                MediaControllerCompat controllerCompat = new MediaControllerCompat(mContext, MediaSessionCompat.Token.fromToken(controller.getSessionToken()));
                if(packageName.equals(controllerCompat.getPackageName()))
                    return controllerCompat;

            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    private long time = 0;

    private void updataMusicInfo() {

        long preTime = Calendar.getInstance().getTimeInMillis();
        if (preTime-time>1500){
            time = preTime;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(Build.VERSION.SDK_INT >= 21){
                        try{
                            List<MediaController> mediaControllers = mediaSessionManager.getActiveSessions(mNotifyReceiveService);
                            if(mediaControllers.size() > 0){
                                for(MediaController controller : mediaControllers){
                                    MediaControllerCompat controllerCompat = new MediaControllerCompat(mContext, MediaSessionCompat.Token.fromToken(controller.getSessionToken()));
                                    String pkgName = controllerCompat.getPackageName();
                                    packageName = pkgName;
                                    PlaybackStateCompat playbackStateCompat = controllerCompat.getPlaybackState();
                                    playState = playbackStateCompat != null && playbackStateCompat.getState() == PlaybackStateCompat.STATE_PLAYING;
                                    MediaMetadataCompat mediaMetadataCompat = controllerCompat.getMetadata();
                                    if(mediaMetadataCompat != null){
                                        MediaDescriptionCompat descriptionCompat = mediaMetadataCompat.getDescription();
                                        if(descriptionCompat != null){
                                            CharSequence title = descriptionCompat.getTitle();
                                            CharSequence subTitle = descriptionCompat.getSubtitle();
                                            if (!TextUtils.isEmpty(title))
                                                musicTitle = title.toString();
                                            if (!TextUtils.isEmpty(title))
                                                musicSinger = subTitle.toString();
                                        }
                                    }
                                }
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        Log.i("data******","musicTitle = "+musicTitle);
                        if (musicTitle==null||"".equals(musicTitle.trim()))
                            return;
                        BleClient.getInstance().writeForSendMusicInfo(musicTitle,musicSinger,playState);

                    }
                }
            },1000);

        }

    }

    public void registerNotify(){
        isRegister = true;
        IntentFilter notifyFilter = new IntentFilter();
        notifyFilter.addAction("notify_posted");
        notifyFilter.addAction("notify_removed");
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mNotifyReceiver, notifyFilter);
    }

    public void unRegisterNotify(){
        if (isRegister){
            isRegister = false;
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mNotifyReceiver);
        }

    }

    class NotifyReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("DATA******", "NotifyReceiver->收到广播:" + intent.getAction());
            updataMusicInfo();
        }
    }

}
