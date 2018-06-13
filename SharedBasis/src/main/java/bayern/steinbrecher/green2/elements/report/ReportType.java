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

import bayern.steinbrecher.green2.data.EnvironmentHandler.ImageSet;
import java.util.Locale;
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
    ERROR(ImageSet.ERROR_SMALL),
    /**
     * Marks a report entry as additional information.
     */
    INFO(ImageSet.INFO_SMALL),
    /**
     * Marks not yet classified report entries.
     */
    UNDEFINED(null),
    /**
     * Marks a report entry as warning.
     */
    WARNING(ImageSet.WARNING_SMALL);

    private final ImageSet graphic;

    /**
     * Creates a report type where {@code graphic} is a symbolic representation for it.
     *
     * @param graphic The symbol to associate with this type of entry.
     */
    ReportType(ImageSet graphic) {
        this.graphic = graphic;
    }

    /**
     * Returns the graphical symbol used for representing the type.
     *
     * @return The graphical symbol used for representing the type.
     */
    public ImageView getGraphic() {
        ImageView view;
        if (graphic == null) {
            view = null;
        } else {
            view = graphic.getAsImageView();
        }
        return view;
    }

    /**
     * Returns the CSS class name associated with this type of entry.
     *
     * @return The CSS class name associated with this type of entry.
     */
    public String getCSSClass() {
        return "report-type-" + name().toLowerCase(Locale.ROOT);
    }
}
