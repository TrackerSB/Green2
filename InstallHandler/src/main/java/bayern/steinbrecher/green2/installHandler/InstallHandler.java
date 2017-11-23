/*
 * Copyright (c) 2017. Stefan Huber
 * This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bayern.steinbrecher.green2.installHandler;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * This class is only needed for the installer to determine where to put the version of Green2.
 *
 * @author Stefan Huber
 */
public final class InstallHandler extends Application {

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Stage primaryStage) {
        List<String> parameters = getParameters().getRaw();
        if (parameters.isEmpty()) {
            Logger.getLogger(InstallHandler.class.getName())
                    .log(Level.SEVERE, "You need to specify an action.\nAllowed actions: {0}", getActionsListing());
        } else {
            String specifiedAction = parameters.get(0).toUpperCase();
            try {
                Actions.valueOf(specifiedAction).call();
                //TODO Try to avoid flow control by exception.
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(InstallHandler.class.getName())
                        .log(Level.SEVERE,
                                "The action\"" + specifiedAction + "\".\nAllowed actions: " + getActionsListing(), ex);
            }
        }
    }

    private static String getActionsListing() {
        return Arrays.stream(Actions.values()).map(Actions::name).collect(Collectors.joining(", "));
    }

    /**
     * The main method.
     *
     * @param args The commandline arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }

    public enum Actions {
        /**
         * Represents the installation process.
         */
        INSTALL {
            /**
             * {@inheritDoc}
             */
            @Override
            public void call() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        },
        /**
         * Represents the uninstallation process.
         */
        UNINSTALL {
            /**
             * {@inheritDoc}
             */
            @Override
            public void call() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };

        /**
         * Calls the action represented by this enum.
         */
        public abstract void call();
    }
}
