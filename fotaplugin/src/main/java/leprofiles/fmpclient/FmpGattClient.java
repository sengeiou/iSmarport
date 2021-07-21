package leprofiles.fmpclient;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import com.mediatek.wearableProfiles.GattRequestManager;
import com.mediatek.wearableProfiles.WearableClientProfile;
import com.mediatek.wearableProfiles.WearableClientProfileRegister;


import java.util.TreeSet;
import java.util.UUID;

import leprofiles.BleGattConstants;
import leprofiles.BleGattUuid;

public class FmpGattClient {

    private static final String TAG = "FmpGattClient";
    private boolean DBG = true;
    private static final int ALERT_LEVEL_OFFSET = 0;

    // Cache GATT Services
    private BluetoothGattCharacteristic mAlertLevelChar = null;
    private GattCallbackImpl mGattCallbackImpl = null;
    private BluetoothGatt mGatt = null;

    private static FmpGattClient sInstance = null;

    public static FmpGattClient getInstance() {
        if (sInstance == null) {
            sInstance = new FmpGattClient();
        }
        return sInstance;
    }

    private FmpGattClient() {
        mGattCallbackImpl = new GattCallbackImpl();
        TreeSet<UUID> uuidSet = new TreeSet<UUID>();
        uuidSet.add(BleGattUuid.Char.ALERT_LEVEL);
        uuidSet.add(BleGattUuid.Char.ALERT_STATUS);
        // Add interesting Characteristic UUIDs
        mGattCallbackImpl.addUuids(uuidSet);
        WearableClientProfileRegister
                .registerWearableClientProfile(mGattCallbackImpl, null);
        Log.d(TAG, "init finished");
    }

    private class GattCallbackImpl extends WearableClientProfile {

        private String TAG = FmpGattClient.TAG;

        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status,
                                            final int newState) {
            if (DBG)
                Log.d(TAG, "onConnectionStateChange, status = " + status + ", newState = "
                        + newState + ", gatt = " + gatt);
            if (BluetoothGatt.STATE_CONNECTED == newState) {
                mGatt = gatt;
                Log.i(TAG, "connect success");
            } else if (BluetoothGatt.STATE_DISCONNECTED == newState) {
                FmpClientStatusRegister.getInstance().setFindMeStatus(
                        FmpClientStatusRegister.FIND_ME_STATUS_DISABLED);
                mGatt = null;
                mAlertLevelChar = null;
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            if (DBG)
                Log.d(TAG, "onServicesDiscovered");
            // Cache GATT services within DeviceManagerService until new
            // services are discovered
            BluetoothGattService alertService = gatt
                    .getService(BleGattUuid.Service.IMMEDIATE_ALERT);
            if (alertService != null) {
                mAlertLevelChar = alertService.getCharacteristic(BleGattUuid.Char.ALERT_LEVEL);
            } else {
                Log.e(TAG, "not support IMMEDIATE_ALERT service");
            }
            if (mAlertLevelChar == null) {
                Log.e(TAG, "not support Immediate_alert, alert level");
                FmpClientStatusRegister.getInstance().setFindMeStatus(
                        FmpClientStatusRegister.FIND_ME_STATUS_DISABLED);
            } else {
                FmpClientStatusRegister.getInstance().setFindMeStatus(
                        FmpClientStatusRegister.FIND_ME_STATUS_NORMAL);
            }
            BluetoothGattCharacteristic alerStatusChar = null;
            if (alertService != null) {
                alerStatusChar = alertService.getCharacteristic(BleGattUuid.Char.ALERT_STATUS);
            }
            if (alerStatusChar != null) {
                gatt.setCharacteristicNotification(alerStatusChar, true);
            } else {
                Log.e(TAG, "not support ALERT_STATUS");
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
                if (BleGattUuid.Char.ALERT_STATUS.equals(characteristic.getUuid())) {
                    onRemoteStopAlert();
                }
            }
        }

        @Override
        public void onCharacteristicRead(final BluetoothGatt gatt,
                                         final BluetoothGattCharacteristic characteristic, final int status) {
            // Get waiting client
            if (DBG) {
                Log.d(TAG, "onCharacteristicRead: ");
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

    public void findTarget(int level) {
        if (mGatt == null) {
            Log.e(TAG, "findTarget::mGatt is null,return");
            return;
        }
        if (mAlertLevelChar != null) {
            FmpClientStatusRegister register = FmpClientStatusRegister.getInstance();
            if (level == BleGattConstants.FMP_LEVEL_NO) {
                register.setFindMeStatus(FmpClientStatusRegister.FIND_ME_STATUS_NORMAL);
            } else {
                register.setFindMeStatus(FmpClientStatusRegister.FIND_ME_STATUS_USING);
            }
            mAlertLevelChar.setValue(level, BluetoothGattCharacteristic.FORMAT_UINT8,
                    ALERT_LEVEL_OFFSET);
            GattRequestManager.getInstance().writeCharacteristic(mGatt, mAlertLevelChar);
        } else {
            Log.e(TAG, "findTarget, mAlertLevelChar == null");
        }
    }

    private void onRemoteStopAlert() {
        FmpClientStatusRegister register = FmpClientStatusRegister.getInstance();
        if (register.getFindMeStatus() != FmpClientStatusRegister.FIND_ME_STATUS_DISABLED) {
            register.setFindMeStatus(FmpClientStatusRegister.FIND_ME_STATUS_NORMAL);
        }
    }
}
