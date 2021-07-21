package leprofiles.basclient;

public interface BatteryChangeListener {

    /**
     * When get the BatteryValue changed event, use this method notify UI
     *
     * @param currentValue a percent value means current battery level, 100
     *                     means 100% battery level, 0 means 0% battery level
     */
    void onBatteryValueChanged(int currentValue, boolean needNotify);
}
