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

package leprofiles;

import java.util.UUID;

/**
 * Definition of GATT-related UUIDs
 *
 * @hide
 */

public final class BleGattUuid {

    /**
     * Definition of GATT services UUIDs
     */
    public static final class Service {

        public static final UUID IMMEDIATE_ALERT = UUID
                .fromString("00001802-0000-1000-8000-00805f9b34fb");

        public static final UUID ALERT_NOTIFICATION = UUID
                .fromString("00001811-0000-1000-8000-00805f9b34fb");

        public static final UUID BATTERY_SERVICE = UUID
                .fromString("0000180f-0000-1000-8000-00805f9b34fb");
    }

    /**
     * Definition of GATT characteristic UUIDs
     */
    public static final class Char {

        public static final UUID ALERT_LEVEL = UUID
                .fromString("00002a06-0000-1000-8000-00805f9b34fb");

        public static final UUID ALERT_STATUS = UUID
                .fromString("00002abc-0000-1000-8000-00805f9b34fb");

        public static final UUID BATTERY_LEVEL = UUID
                .fromString("00002a19-0000-1000-8000-00805f9b34fb");
    }

    /**
     * Definition of GATT characteristic descriptor UUIDs
     */
    public static final class Desc {

        public static final UUID CLIENT_CHAR_CONFIG = UUID
                .fromString("00002902-0000-1000-8000-00805f9b34fb");

        public static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID
                .fromString("00002902-0000-1000-8000-00805f9b34fb");
    }

}
