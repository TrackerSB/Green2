/*
 * Copyright (c) 2017. Stefan Huber
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package bayern.steinbrecher.gruen2.login;

import bayern.steinbrecher.gruen2.View;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a login.
 *
 * @author Stefan Huber
 */
public abstract class Login extends View<LoginController> {

    @Override
    public abstract void start(Stage primaryStage) throws IOException;

    /**
     * Returns the information that was entered in the login. This method blocks
     * until the frame is closed or hidden. It won't show more than once even if
     * multiple threads call it. They will be blocked and notified when the
     * login window closes.
     *
     * @return The Information that was entered in the login.
     */
    public Optional<Map<LoginKey, String>> getLoginInformation() {
        showOnceAndWait();
        return controller.getLoginInformation();
    }
}
