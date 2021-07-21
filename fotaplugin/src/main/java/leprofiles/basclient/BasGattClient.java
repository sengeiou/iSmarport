package leprofiles.basclient;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import com.mediatek.wearableProfiles.GattRequestManager;
import com.mediatek.wearableProfiles.WearableClientProfile;
import com.mediatek.wearableProfiles.WearableClientProfileRegister;

import java.util.List;
import java.util.TreeSet;
import java.util.UUID;

import leprofiles.BleGattUuid;

public class BasGattClient {

    private static final String TAG = "Gatt";
    private boolean DBG = true;
    private GattCallbackImpl mGattCallbackImpl;
    private BluetoothGatt mGatt = null;
    private BluetoothGattCharacteristic mBatteryLevel = null;
    private BluetoothGattDescriptor mClientCharConfig = null;
    private BatteryChangeListener mBatteryChangeListener = null;
    private byte[] CLIENT_CHAR_VALUE = {
            1, 0
    };
    private int mCurrentBatteryLevel = -1;

    private static BasGattClient sInstance = null;

    public static BasGattClient getInstance() {
        if (sInstance == null) {
            sInstance = new BasGattClient();
        }
        return sInstance;
    }

    private BasGattClient() {
        mGattCallbackImpl = new GattCallbackImpl();
        TreeSet<UUID> uuidSet = new TreeSet<UUID>();
        uuidSet.add(BleGattUuid.Char.BATTERY_LEVEL);
        uuidSet.add(BleGattUuid.Desc.CLIENT_CHARACTERISTIC_CONFIG);
        mGattCallbackImpl.addUuids(uuidSet);
        WearableClientProfileRegister
                .registerWearableClientProfile(mGattCallbackImpl, null);
    }

    public void registerBatteryChangeListener(BatteryChangeListener listener) {
        mBatteryChangeListener = listener;
    }

    public void unregisterBatteryChangeListener() {
        mBatteryChangeListener = null;
    }

    private class GattCallbackImpl extends WearableClientProfile {
        // Broadcast Response
        private static final String TAG = BasGattClient.TAG + ".GattCallbackImpl";

        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status,
                                            final int newState) {
            if (DBG)
                Log.d(TAG, "onConnectionStateChange, status = " + status + ", newState = "
                        + newState);
            if (BluetoothGatt.STATE_CONNECTED == newState) {
                mGatt = gatt;
                Log.i(TAG, "connect success");
            } else if (BluetoothGatt.STATE_DISCONNECTED == newState) {
                mGatt = null;
                notifyBatteryChanged(-1, false);
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            if (DBG)
                Log.d(TAG, "onServicesDiscovered");

            // Cache GATT services within DeviceManagerService until new
            // services are discovered
            BluetoothGattService bas = gatt.getService(BleGattUuid.Service.BATTERY_SERVICE);
            if (bas != null) {
                mBatteryLevel = bas.getCharacteristic(BleGattUuid.Char.BATTERY_LEVEL);
            } else {
                Log.e(TAG, "bas = null");
                return;
            }
            if (mBatteryLevel != null) {
                Log.d(TAG, "mBatteryLevel = " + mBatteryLevel.getUuid());
                mClientCharConfig = mBatteryLevel
                        .getDescriptor(BleGattUuid.Desc.CLIENT_CHARACTERISTIC_CONFIG);
                List<BluetoothGattDescriptor> list = mBatteryLevel.getDescriptors();
                if (list != null) {
                    Log.d(TAG, "list = " + list.size());
                    for (BluetoothGattDescriptor desc : list) {
                        Log.d(TAG, "desc = " + desc.getUuid());
                    }
                }
            }
            if (mClientCharConfig != null) {
                setNotifyEnabled();
            } else {
                Log.e(TAG, "mClientCharConfig = null");
            }
            if (mBatteryLevel != null) {
                if (mGatt != null) {
                    Log.d(TAG, "setCharacteristicNotification" + mBatteryLevel.getUuid());
                    mGatt.setCharacteristicNotification(mBatteryLevel, true);
                }
                readBatteryLevel();
            } else {
                Log.e(TAG, "mBatteryLevel = null");
            }
        }

        // 1 to 1 Response
        @Override
        public void onCharacteristicChanged(final BluetoothGatt gatt,
                                            final BluetoothGattCharacteristic characteristic) {
            if (DBG) {
                Log.d(TAG, "onCharacteristicChanged: ");
            }
            if (characteristic != null) {
                Log.d(TAG, "onCharacteristicChanged ID = " + characteristic.getUuid());
                onBatteryLevelChange(characteristic);
            }

        }

        @Override
        public void onCharacteristicRead(final BluetoothGatt gatt,
                                         final BluetoothGattCharacteristic characteristic, final int status) {
            // Get waiting client
            if (DBG) {
                Log.d(TAG, "onCharacteristicRead: ");
            }
            if (characteristic != null) {
                onBatteryLevelChange(characteristic);
            }

        }

        @Override
        public void onCharacteristicWrite(final BluetoothGatt gatt,
                                          final BluetoothGattCharacteristic characteristic, final int status) {
            if (DBG) {
                Log.d(TAG, "onCharacteristicWrite: ");
            }

        }

        @Override
        public void onDescriptorRead(final BluetoothGatt gatt,
                                     final BluetoothGattDescriptor descriptor, final int status) {
            if (DBG) {
                Log.d(TAG, "onDescriptorRead:");
            }

        }

        @Override
        public void onDescriptorWrite(final BluetoothGatt gatt,
                                      final BluetoothGattDescriptor descriptor, final int status) {
            if (DBG) {
                Log.d(TAG, "onDescriptorWrite: ");
            }

        }

        @Override
        public void onReadRemoteRssi(final BluetoothGatt gatt, final int rssi, final int status) {
            // Get waiting client
            if (DBG) {
                Log.d(TAG, "onReadRemoteRssi: ");
            }

        }

        @Override
        public void onReliableWriteCompleted(final BluetoothGatt gatt, final int status) {
            if (DBG) {
                Log.d(TAG, "onReliableWriteCompleted: ");
            }

        }
    }

    private void readBatteryLevel() {
        if (mGatt != null && mBatteryLevel != null) {
            GattRequestManager.getInstance().readCharacteristic(mGatt, mBatteryLevel);
        }
    }

    private void setNotifyEnabled() {
        if (mGatt != null && mClientCharConfig != null) {
            mClientCharConfig.setValue(CLIENT_CHAR_VALUE);
            GattRequestManager.getInstance().writeDescriptor(mGatt, mClientCharConfig);
        }
    }

    private void onBatteryLevelChange(BluetoothGattCharacteristic characteristic) {
        if (characteristic != null) {
            BluetoothGattService service = characteristic.getService();
            if (service != null
                    && service.getUuid().equals(BleGattUuid.Service.BATTERY_SERVICE)
                    && characteristic.getUuid().equals(BleGattUuid.Char.BATTERY_LEVEL)) {
                byte[] bytes = characteristic.getValue();
                if (bytes != null && bytes.length > 0) {
                    int level = bytes[0];
                    notifyBatteryChanged(level, true);
                }
            }
        }
    }

    private void notifyBatteryChanged(int level, boolean needNotify) {
        if (mBatteryChangeListener != null) {
            if (mCurrentBatteryLevel == level) {
                needNotify = false;
            }
            mCurrentBatteryLevel = level;
            mBatteryChangeListener.onBatteryValueChanged(level, needNotify);
        }
    }
}
