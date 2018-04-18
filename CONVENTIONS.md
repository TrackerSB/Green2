# Guidelines and conventions

## Structure
### JavaFX properties
* When looking at a JavaFX property x the order of methods should be:
    * `xProperty()`
    * `getXProperty()`
    * `setXProperty(...)`
    * If the property should be readonly omit `setXProperty(...)` and make sure `xProperty()` returns a readonly property.
* When accessing a property calling `getXProperty()` should be preffered instead of directly accessing it.
### FXML files used in wizards
* There have to be two fxml files:
    1. `SomeClassWizard.fxml` which contains the actual content and data of the dialog such that it can be used in a wizard.
    2. `SomeClassParent.fxml` which uses `fx:include` to include SomeClassWizard.fxml and adds controls like finish or cancel to control the dialog without using it with a wizard.
* There have to be two controller:
    1. `SomeClassController` extending WizardableController and providing all functions and properties needed by SomeClassWizard.fxml. This controller also contains methods methods for requesting the actual results of the window.
    2. Package private class `SomeClassControllerParent` extending SomeClassController and adding only the functions needed for the additional controls of SomeClassParent.fxml.
* There has to be only one view which references solely SomeClassController for requesting results and loads either SomeClassParent.fxml or SomeClassWizard.fxml.
* It may be neccessary for SomeClassControllerParent to add a listener to some property of SomeClassController e.g. to add submit-on-enter functionality. This should be realized by defining a protected method called addListenerToSomeProperty(ChangeListener<...>).
