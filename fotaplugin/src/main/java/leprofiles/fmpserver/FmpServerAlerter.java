package leprofiles.fmpserver;

/**
 * An interface for implementing customized FMP alert.
 */
public interface FmpServerAlerter {

    /**
     * Notify the event that the alert level is changed.
     *
     * @param level Current alert level
     * @return Is alert level change event effective
     */
    boolean alert(final int level);
}
