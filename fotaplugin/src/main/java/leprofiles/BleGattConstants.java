package leprofiles;

/**
 * A class for listing some constants
 */
public class BleGattConstants {

    public class GattFeaturFlags {
        public static final int FLAG_SUPPORT_FMP_CLIENT = 1 << 0;
        public static final int FLAG_SUPPORT_FMP_SERVER = 1 << 1;
        public static final int FLAG_SUPPORT_BAS_CLIENT = 1 << 2;
    }

    /**
     * FMP(FMP server) alert status is no alert
     */
    public static final int STATE_FIND_ME_NO_ALERT = 10;
    /**
     * FMP(FMP server) alert status is high alert
     */
    public static final int STATE_FIND_ME_ALERT = 11;

    /**
     * Indicates the FMP alert level is 0. Used to send to remote side.
     */
    public static final int FMP_LEVEL_NO = 0;
    /**
     * Indicates the FMP alert level is 1. Used to send to remote side.
     */
    public static final int FMP_LEVEL_MILD = 1;
    /**
     * Indicates the FMP alert level is 2. Used to send to remote side.
     */
    public static final int FMP_LEVEL_HIGH = 2;

    /**
     * battery level thresholds
     */
    public static final int BATTERY_LEVEL_1 = 33;
    public static final int BATTERY_LEVEL_2 = 66;
    public static final int BATTERY_LEVEL_3 = 100;
}
