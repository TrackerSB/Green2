package bayern.steinbrecher.green2.memberManagement.generator.sepa;

/**
 * Representing all sequence types of SEPA direct debits.
 *
 * @author Stefan Huber
 */
public enum SequenceType {
    /**
     * First dd sequence.
     */
    FRST,
    /**
     * Recurrent dd sequence.
     */
    RCUR,
    /**
     * One-off dd sequence.
     */
    OOFF,
    /**
     * Final dd sequence.
     */
    FNAL
}
