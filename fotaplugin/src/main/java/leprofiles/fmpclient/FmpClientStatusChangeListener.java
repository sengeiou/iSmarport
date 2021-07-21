package leprofiles.fmpclient;

/**
 * An interface for monitoring PXP/FMP status change event
 */
public interface FmpClientStatusChangeListener {
    /**
     * The method will be called when Pxp/Fmp status changed
     */
    void onStatusChange();
}
