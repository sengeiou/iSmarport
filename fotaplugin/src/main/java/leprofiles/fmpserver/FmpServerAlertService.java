/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 *
 * MediaTek Inc. (C) 2014. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

package leprofiles.fmpserver;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;


import java.io.IOException;

import leprofiles.BleGattConstants;

public class FmpServerAlertService extends Service {

    private static final String TAG = FmpServerAlertService.class.getSimpleName();
    private static final boolean DBG = true;
    private static final boolean VDBG = true;

    public static final String INTENT_STATE = "state";
    private static final int UPDATE_ALERT = 0;
    private static final int STOP_RING = 1;
    private static final int ALERT_VOLUMN = 10;
    private static final String RINGTONE_NAME = "music/Alarm_Beep_03.ogg";
    private static final long[] VIBRATE_PATTERN = new long[]{
            500, 500
    };

    private MediaPlayer mMediaPlayer = null;
    private Vibrator mVibrator = null;
    private AssetFileDescriptor mRingtoneDescriptor;
    private AlertDialog mAlertDialog = null;

    private AudioManager.OnAudioFocusChangeListener mAudioListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if (DBG) {
                Log.d(TAG, "onAudioFocusChange:" + focusChange);
            }
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    pauseRingAndVib();
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    replayRingAndVib();
                    break;
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                case AudioManager.AUDIOFOCUS_REQUEST_FAILED:
                    stopRingAndVib();
                    break;
                default:
                    break;
            }
        }
    };

    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            Log.i(TAG, "PhoneStateListener, new state=" + state);
            if (state == TelephonyManager.CALL_STATE_RINGING) {
                updateAlertState(BleGattConstants.FMP_LEVEL_NO);
            }
        }
    };

    private Handler mHandler = new Handler() {

        public void handleMessage(Message msg) {
            if (DBG) {
                Log.d(TAG, "handleMessage: " + msg.what);
            }
            switch (msg.what) {
                case UPDATE_ALERT:
                    int state = msg.arg1;
                    switch (state) {
                        case BleGattConstants.FMP_LEVEL_HIGH:
                        case BleGattConstants.FMP_LEVEL_MILD:
                            if (!mAlertDialog.isShowing()) {
                                mAlertDialog.show();
                            }
                            applyRingAndVib();
                            break;
                        case BleGattConstants.FMP_LEVEL_NO:
                            if (mAlertDialog.isShowing()) {
                                mAlertDialog.dismiss();
                            }
                            stopRingAndVib();
                            break;
                        default:
                            Log.e(TAG, "Invalid level");
                            break;
                    }
                    break;
                case STOP_RING:
                    stopRingAndVib();
                    break;
                default:
                    break;
            }
        }
    };

    private void updateAlertState(int state) {
        Message msg = mHandler.obtainMessage(UPDATE_ALERT);
        msg.arg1 = state;
        msg.sendToTarget();
    }

    private void stopRingVibrateOnly() {
        Message msg = mHandler.obtainMessage(STOP_RING);
        msg.sendToTarget();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public void onCreate() {
        if (DBG) {
            Log.d(TAG, "onCreate()");
        }
        AssetManager assetManager = this.getAssets();
        try {
            mRingtoneDescriptor = assetManager.openFd(RINGTONE_NAME);
        } catch (IOException e) {
            e.printStackTrace();
        }
        TelephonyManager telephonyManager = (TelephonyManager) (getSystemService(Context.TELEPHONY_SERVICE));
        telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        initDialog();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int state = intent.getIntExtra(INTENT_STATE, BleGattConstants.FMP_LEVEL_NO);
            updateAlertState(state);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void initDialog() {
        if (DBG) {
            Log.d(TAG, "initDialog");
        }

        OnClickListener buttonListener = new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (DBG) {
                    Log.d(TAG, "Check clicked");
                }
                // to do
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        break;
                    default:
                        break;
                }
                updateAlertState(BleGattConstants.FMP_LEVEL_NO);
            }
        };

        mAlertDialog = new AlertDialog.Builder(this).setTitle("R.string.alert_dialog_title")
                .setCancelable(false).setMessage("R.string.alert_dialog_text")
                .setPositiveButton("R.string.alert_dialog_dismiss", buttonListener)
                .setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (DBG) {
                            Log.d(TAG, "onDismiss");
                        }
                    }
                }).setOnKeyListener(new OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                                || event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
                            stopRingVibrateOnly();
                            return true;
                        }
                        return false;
                    }
                }).create();
        mAlertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
    }

    private void pauseRingAndVib() {
        if (DBG) {
            Log.d(TAG, "pauseRingAndVib");
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
        }
        if (mVibrator != null) {
            mVibrator.cancel();
            mVibrator = null;
        }
    }

    private void replayRingAndVib() {
        if (DBG) {
            Log.d(TAG, "replayRingAndVib");
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
        }
        mVibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        mVibrator.vibrate(VIBRATE_PATTERN, 0);
    }

    private void stopRingAndVib() {
        if (DBG) {
            Log.d(TAG, "stopRingAndVib");
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            am.abandonAudioFocus(mAudioListener);
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        if (mVibrator != null) {
            mVibrator.cancel();
            mVibrator = null;
        }
    }

    private void applyRingAndVib() {

        if (DBG) {
            Log.d(TAG, "applyRingAndVib: ");
        }
        stopRingAndVib();

        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(mRingtoneDescriptor.getFileDescriptor(),
                    mRingtoneDescriptor.getStartOffset(), mRingtoneDescriptor.getLength());
            // mMediaPlayer.setDataSource("file:///android_asset/ +
            // RINGTONE_NAME");
            mMediaPlayer.setLooping(true);

            AudioManager aM = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            aM.setStreamVolume(AudioManager.STREAM_ALARM, ALERT_VOLUMN, 0);

            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mMediaPlayer.setOnErrorListener(new OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer player, int what, int extra) {
                    if (DBG) {
                        Log.d(TAG, "Media Player onError:" + what);
                    }
                    stopRingAndVib();
                    return false;
                }
            });
            mMediaPlayer.prepare();
            aM.requestAudioFocus(mAudioListener, AudioManager.STREAM_ALARM,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            mMediaPlayer.start();

        } catch (IllegalStateException e) {
            Log.e(TAG, "Media Player IllegalStateException");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "Media Player IOException");
            e.printStackTrace();
        }

        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mVibrator.vibrate(VIBRATE_PATTERN, 0);

    }
}
