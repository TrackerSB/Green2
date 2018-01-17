/*
 * Copyright (C) 2018 Steinbrecher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bayern.steinbrecher.green2;

/**
 * Represents a wrapper for any {@link Exception} thrown when calling {@link View#start(javafx.stage.Stage)}. The
 * intention is to provide a more precise Exception than {@link Exception}.
 *
 * @author Stefan Huber
 */
public class ViewStartException extends RuntimeException {

    /**
     * Wraps the given cause into a {@link ViewStartException}.
     *
     * @param cause The cause to wrap.
     */
    public ViewStartException(Throwable cause) {
        super(cause);
    }
}
