/*
 * Copyright (c) 2017. Stefan Huber
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package bayern.steinbrecher.gruen2;

import javafx.fxml.Initializable;
import javafx.stage.Stage;

/**
 * Represents a controller all other controller have to extend.
 *
 * @author Stefan Huber
 */
public abstract class Controller implements Initializable {

    /**
     * The stage the controller has to interact with.
     */
    protected Stage stage = null;
    /**
     * Only {@code true} when the user explicitly abborted his input. (E.g.
     * pressing the X of the window.)
     */
    private boolean userAbborted = false;

    /**
     * Sets the stage the conroller can refer to. (E.g. for closing the stage)
     * NOTE: It overrides {@code onCloseRequest}.
     *
     * @param stage The stage to refer to.
     */
    public void setStage(Stage stage) {
        this.stage = stage;
        stage.setOnCloseRequest(evt -> userAbborted = true);
    }

    /**
     * Throws a {@code IllegalStateException} only if stage is {@code null}.
     */
    protected void checkStage() {
        if (stage == null) {
            throw new IllegalStateException(
                    "You have to call setStage(...) first");
        }
    }

    /**
     * Checks whether the user abborted his input.
     *
     * @return {@code true} only if the user abborted his input explicitly.
     */
    public boolean userAbborted() {
        return userAbborted;
    }
}
