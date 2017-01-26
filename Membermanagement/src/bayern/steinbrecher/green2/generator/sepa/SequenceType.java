/*
 * Copyright (c) 2017. Stefan Huber
 * This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/4.0/.
 */

package bayern.steinbrecher.green2.generator.sepa;

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
