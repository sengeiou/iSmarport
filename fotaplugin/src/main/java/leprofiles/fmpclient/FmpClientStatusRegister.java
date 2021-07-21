package leprofiles.fmpclient;


import java.util.ArrayList;

/**
 * A manager for monitoring PXP/FMP status change event.
 */
public class FmpClientStatusRegister {

    /**
     * Indicate current FMP is unusable.
     */
    public static final int FIND_ME_STATUS_DISABLED = 0;
    /**
     * Indicate current FMP is usable.
     */
    public static final int FIND_ME_STATUS_NORMAL = 1;
    /**
     * Indicate current FMP is being using.
     */
    public static final int FIND_ME_STATUS_USING = 2;

    private static FmpClientStatusRegister sInstance = null;
    private ArrayList<FmpClientStatusChangeListener> mFmStatusChangeListeners = new ArrayList<FmpClientStatusChangeListener>();
    private int mFindMeStatus = FIND_ME_STATUS_DISABLED;

    /**
     * single tone instance, get instance from this method.
     * @return the instance of the PxpFmStatusRegister
     */
    public static FmpClientStatusRegister getInstance() {
        if (sInstance == null) {
            sInstance = new FmpClientStatusRegister();
        }
        return sInstance;
    }

    private FmpClientStatusRegister() {
    }

    /**
     * register a listener of FMP status change
     *
     * @param listener for listen FMP status change
     */
    public void registerFmListener(FmpClientStatusChangeListener listener) {
        if (!mFmStatusChangeListeners.contains(listener)) {
            mFmStatusChangeListeners.add(listener);
        }
    }

    /**
     * unregister the listener of FMP status change
     *
     * @param listener for listen FMP status change
     */
    public void unregisterFmListener(FmpClientStatusChangeListener listener) {
        mFmStatusChangeListeners.remove(listener);
    }

    /**
     * set FMP status, and notify registered PxpFmStatusChangeListener
     *
     * @param status should be only
     *               FIND_ME_STATUS_DISABLED,
     *               FIND_ME_STATUS_NORMAL,
     *               FIND_ME_STATUS_USING
     */
    public void setFindMeStatus(int status) {
        mFindMeStatus = status;
        notifyFmChange();
    }

    /**
     * get FMP status
     *
     * @return should be only
     *            FIND_ME_STATUS_DISABLED,
     *            FIND_ME_STATUS_NORMAL,
     *            FIND_ME_STATUS_USING
     */
    public int getFindMeStatus() {
        return mFindMeStatus;
    }

    private void notifyFmChange() {
        for (FmpClientStatusChangeListener listener : mFmStatusChangeListeners) {
            listener.onStatusChange();
        }
    }

}
