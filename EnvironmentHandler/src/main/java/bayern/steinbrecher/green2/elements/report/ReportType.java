/*
 * Copyright (C) 2018 Stefan Huber
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
package bayern.steinbrecher.green2.elements.report;

import bayern.steinbrecher.green2.data.EnvironmentHandler;
import javafx.scene.image.ImageView;

/**
 * Represents classifications of report entries.
 *
 * @author Stefan Huber
 * @since 2u14
 */
public enum ReportType {
    /**
     * Marks a report entry as errors.
     */
    ERROR("error"),
    /**
     * Marks a report entry as additional information.
     */
    INFO("info"),
    /**
     * Marks not yet classified report entries.
     */
    UNDEFINED(null),
    /**
     * Marks a report entry as warning.
     */
    WARNING("warning");

    private final String graphic;

    private ReportType(String graphic) {
        this.graphic = graphic;
    }

    public ImageView getGraphic() {
        ImageView view;
        if (graphic == null) {
            view = null;
        } else {
            view = EnvironmentHandler.ImageSet.valueOf(graphic).getAsImageView();
        }
        return view;
    }
}
