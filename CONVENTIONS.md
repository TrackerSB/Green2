# Guidelines and conventions

## Structure

### Classes
#### JavaFX properties
* When looking at a JavaFX property x the order of methods should be:
    * `xProperty()`
    * `getXProperty()`
    * `setXProperty(...)`
* If the property should be readonly omit `setXProperty(...)` and make sure `xProperty()` returns a readonly property.
* All properties of classes serving as instance variables should be defined where they are declared and have to be final.
* When accessing a property calling `getXProperty()` should be preffered instead of directly accessing it.
#### Controller
* Only the most upper controller containing actual implementations for a specific windows should override `initializable(URL, ResourceBundle)`.
* An actual controller should not have any subclasses unless it is related to a wizardable window. If so it should have exactly one subclass which has no subclasses itself.
* Controller without subclasses should be declared as final.
* Controller have to be associated within FXML files.

### FXML
#### FXML based classes used in wizards
* There have to be two fxml files:
    1. `SomeClassWizard.fxml` which contains the actual content and data of the dialog such that it can be used in a wizard.
    2. `SomeClassParent.fxml` which uses `fx:include` to include `SomeClassWizard.fxml` and adds controls like finish or cancel to control the dialog without using it with a wizard.
* There have to be two controller:
    1. `SomeClassController` extending `WizardableController` and providing all functions and properties needed by `SomeClassWizard.fxml`. This controller also contains methods methods for requesting the actual results of the window.
    2. Package private class `SomeClassControllerParent` extending `SomeClassController` and adding only the functions needed for the additional controls of `SomeClassParent.fxml`.
* There has to be only one view which references solely `SomeClassController` for requesting results and loads either `SomeClassParent.fxml` or `SomeClassWizard.fxml`.
* It may be neccessary for `SomeClassControllerParent` to add a listener to some property of `SomeClassController` e.g. to add submit-on-enter functionality. This should be realized by defining a protected method called `addListenerToSomeProperty(ChangeListener<...>)`. This method should be called in the default constructor of the subclass.
### CSS includes
* If a CSS file to include is in the same package it has to be included within FXML.

### Files
#### CSS files
CSS files containing rules only for specific classes or dialogs have to be placed in the same package as the class they are refered to.

## Language features

### Java
#### var keyword
* The var keyword has to be avoided in general
* It is allowed in direct definitions of variables like `var someObject = new SomeObject()
