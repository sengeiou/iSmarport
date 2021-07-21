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

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import com.mediatek.wearable.leprofiles.LeServer;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import leprofiles.BleGattConstants;
import leprofiles.BleGattUuid;

/**
 * Provides a service which implements Find Me Profile.
 * This class is a subclass of Android Service, and communicates with GATT interface.
 */
public class FmpGattServer implements LeServer {
    private static final String TAG = "FmpGattServer";
    private static final boolean DBG = true;
    private static final boolean VDBG = true;

    private static final int ALERT_LEVEL_OFFSET = 0;
    private BluetoothGattServer mBluetoothGattServer;

    private FmpServerAlerter mAlerter;
    private static FmpGattServer sInstance = null;
    private Context mContext;

    public static FmpGattServer getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new FmpGattServer(context);
        }
        return sInstance;
    }

    private FmpGattServer(Context context) {
        mContext = context;
        mAlerter = makeAlerter();
    }

    public void setCustomizedAlerter(FmpServerAlerter alerter) {
        mAlerter = alerter;
    }

    private final BluetoothGattServerCallback mGattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onCharacteristicReadRequest(final BluetoothDevice device, final int requestId,
                                                final int offset, final BluetoothGattCharacteristic characteristic) {
            if (characteristic != null) {
                BluetoothGattService service = characteristic.getService();
                UUID serviceUuid = service.getUuid();
                if (!BleGattUuid.Service.IMMEDIATE_ALERT.equals(serviceUuid)) {
                    return;
                }
            }
            final byte[] data = characteristic.getValue();

            if (DBG) {
                Log.d(TAG, "onCharacteristicReadRequest - incoming request: " + device.getName());
                Log.d(TAG, "onCharacteristicReadRequest -        requestId: " + requestId);
                Log.d(TAG, "onCharacteristicReadRequest -           offset: " + offset);
                Log.d(TAG, "onCharacteristicReadRequest -             uuid: "
                        + characteristic.getUuid().toString());
            }

            if (mBluetoothGattServer != null) {
                mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS,
                        offset, Arrays.copyOfRange(data, offset, data.length));
            }
        }

        @Override
        public void onCharacteristicWriteRequest(final BluetoothDevice device, final int requestId,
                                                 final BluetoothGattCharacteristic characteristic, final boolean preparedWrite,
                                                 final boolean responseNeeded, final int offset, final byte[] value) {
            if (characteristic != null) {
                BluetoothGattService service = characteristic.getService();
                UUID serviceUuid = service.getUuid();
                if (!BleGattUuid.Service.IMMEDIATE_ALERT.equals(serviceUuid)) {
                    return;
                }
            }
            byte[] newValue = null;
            final byte[] oldValue = characteristic.getValue();

            if (DBG) Log.d(TAG, "onCharacteristicWriteRequest - offset:" + offset + " "
                    + "value.length:" + value.length + " "
                    + "preparedWrite:" + preparedWrite + " "
                    + "responseNeeded:" + responseNeeded);

            if (null != oldValue && oldValue.length >= offset + value.length) {
                newValue = new byte[offset + value.length];
                System.arraycopy(oldValue, 0, newValue, 0, offset);
                System.arraycopy(value, 0, newValue, offset, value.length);
            } else {
                newValue = new byte[offset + value.length];
                if (null != oldValue) {
                    System.arraycopy(oldValue, 0, newValue, 0, oldValue.length);
                }
                System.arraycopy(value, 0, newValue, offset, value.length);
            }

            if (VDBG) Log.v(TAG, "onCharacteristicWriteRequest- preparedWrite:" + preparedWrite);

            if (preparedWrite) {
                if (VDBG) Log.v(TAG, "onCharacteristicWriteRequest - preparedWrite write\n");
            } else {
                if (VDBG) Log.v(TAG, "onCharacteristicWriteRequest - a normal write\n");
                if (null != characteristic) {
                    characteristic.setValue(newValue);
                    final Integer level = characteristic.getIntValue(
                            BluetoothGattCharacteristic.FORMAT_UINT8, ALERT_LEVEL_OFFSET);
                    if (null != level) {
                        if (DBG) Log.d(TAG, "level = " + level.intValue()
                                + ", mAlerter = " + mAlerter);
                        mAlerter.alert(level.intValue());
                    }
                }
            }

            if (VDBG) Log.v(TAG, "onCharacteristicWriteRequest- responseNeeded:"
                    + responseNeeded);
            if (responseNeeded && mBluetoothGattServer != null) {
                mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS,
                        offset, value);
            }

        }

        @Override
        public void onConnectionStateChange(final BluetoothDevice device, final int status,
                                            final int newState) {
            if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                mAlerter.alert(BleGattConstants.FMP_LEVEL_NO);
            }
        }

        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
        }

        @Override
        public void onDescriptorReadRequest(final BluetoothDevice device, final int requestId,
                                            final int offset, final BluetoothGattDescriptor descriptor) {
        }

        @Override
        public void onDescriptorWriteRequest(final BluetoothDevice device, final int requestId,
                                             final BluetoothGattDescriptor descriptor, final boolean preparedWrite,
                                             final boolean responseNeeded, final int offset, final byte[] value) {

        }

        @Override
        public void onExecuteWrite(final BluetoothDevice device, final int requestId,
                                   final boolean execute) {
        }
    };

    @Override
    public List<BluetoothGattService> getHardCodeProfileServices() {
        List<BluetoothGattService> listGattService = new ArrayList<BluetoothGattService>();

        // service 1
        BluetoothGattService alertService = new BluetoothGattService(
                BleGattUuid.Service.IMMEDIATE_ALERT, BluetoothGattService.SERVICE_TYPE_PRIMARY);

        BluetoothGattCharacteristic alertLevelChar = new BluetoothGattCharacteristic(
                BleGattUuid.Char.ALERT_LEVEL,
                BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                BluetoothGattCharacteristic.PERMISSION_WRITE);
        alertLevelChar.setValue(BleGattConstants.FMP_LEVEL_NO,
                BluetoothGattCharacteristic.FORMAT_UINT8, ALERT_LEVEL_OFFSET);

        alertService.addCharacteristic(alertLevelChar);
        listGattService.add(alertService);

        return listGattService;
    }

    @Override
    public BluetoothGattServerCallback getGattServerCallback() {
        return mGattServerCallback;
    }

    @Override
    public void setBluetoothGattServer(BluetoothGattServer server) {
        mBluetoothGattServer = server;
    }

    private DefaultAlerter makeAlerter() {
        if (DBG) {
            Log.d(TAG, "makeAlerter: alerterType");
        }
        return new DefaultAlerter(mContext);

    }
}
