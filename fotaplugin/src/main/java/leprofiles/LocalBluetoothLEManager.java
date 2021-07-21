package leprofiles;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.mediatek.wearable.leprofiles.LeServer;
import com.mediatek.wearable.leprofiles.LeServerManager;


import java.util.ArrayList;

import leprofiles.basclient.BasGattClient;
import leprofiles.basclient.BatteryChangeListener;
import leprofiles.fmpclient.FmpClientStatusRegister;
import leprofiles.fmpclient.FmpGattClient;
import leprofiles.fmpserver.FmpGattServer;

/**
 * The class provide methods to control GATT profiles features
 */
public class LocalBluetoothLEManager {

    private static LocalBluetoothLEManager sInstance = null;
    private static final String TAG = "[refactorPxp][LocalBluetoothLEManager]";

    private FmpGattClient mFmpClient = null;
    private LeServer mFmpServer = null;
    private BasGattClient mBasClient = null;

    /**
     * single tone instance, get instance from this method.
     * @return the instance of the LocalBluetoothLEManager
     */
    public static synchronized LocalBluetoothLEManager getInstance() {
        if (sInstance == null) {
            sInstance = new LocalBluetoothLEManager();
        }
        return sInstance;
    }

    /**
     * Initialize the class
     *
     * @param context  Application context
     * @param profiles flags for supported profiles
     */
    public void init(Context context) {
        int profiles = BleGattConstants.GattFeaturFlags.FLAG_SUPPORT_BAS_CLIENT
                | BleGattConstants.GattFeaturFlags.FLAG_SUPPORT_FMP_CLIENT
                | BleGattConstants.GattFeaturFlags.FLAG_SUPPORT_FMP_SERVER;
        if (Build.VERSION.SDK_INT >= 18) {
            Log.d(TAG, "supported profiles = " + profiles);
            initFwk(context, profiles);
        }
    }

    private LocalBluetoothLEManager() {
    }

    private void initFwk(final Context context, final int profiles) {
        ArrayList<LeServer> leServerList = new ArrayList<LeServer>();
        if ((profiles & BleGattConstants.GattFeaturFlags.FLAG_SUPPORT_FMP_SERVER) > 0) {
            mFmpServer = FmpGattServer.getInstance(context);
            leServerList.add(mFmpServer);
        }
        LeServerManager.addLeServers(context, leServerList);

        if ((profiles & BleGattConstants.GattFeaturFlags.FLAG_SUPPORT_FMP_CLIENT) > 0) {
            mFmpClient = FmpGattClient.getInstance();
        }
        if ((profiles & BleGattConstants.GattFeaturFlags.FLAG_SUPPORT_BAS_CLIENT) > 0) {
            mBasClient = BasGattClient.getInstance();
        }
    }

    /**
     * Find target device. cann't find the target device if the device is not
     * support FMP profile, cann't find the target device if the device is
     * alerted
     *
     * @param level should only be
     *            BlePxpFmpConstants.FMP_LEVEL_NO,
     *            BlePxpFmpConstants.FMP_LEVEL_MILD,
     *            BlePxpFmpConstants.FMP_LEVEL_HIGH.
     */
    public void findTargetDevice(int level) {
        if (mFmpClient != null) {
            mFmpClient.findTarget(level);
            if (level == BleGattConstants.FMP_LEVEL_HIGH
                    || level == BleGattConstants.FMP_LEVEL_MILD) {
                FmpClientStatusRegister.getInstance()
                        .setFindMeStatus(FmpClientStatusRegister.FIND_ME_STATUS_USING);
            } else {
                FmpClientStatusRegister.getInstance()
                        .setFindMeStatus(FmpClientStatusRegister.FIND_ME_STATUS_NORMAL);
            }
        }
    }

    /**
     * register listener to listen battery change
     *
     * @param listener implement interface BatteryChangeListener
     */
    public void registerBatteryLevelListener(BatteryChangeListener listener) {
        if (mBasClient != null) {
            mBasClient.registerBatteryChangeListener(listener);
        }
    }

    /**
     * unregister listener
     */
    public void unregisterBatteryLevelListener() {
        if (mBasClient != null) {
            mBasClient.unregisterBatteryChangeListener();
        }
    }
}
