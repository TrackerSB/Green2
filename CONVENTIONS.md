# Guidelines and conventions

## Structure

### Classes
#### JavaFX properties
* When looking at a JavaFX property x the order of methods should be:
    * `xProperty()`
    * `getXProperty()`
    * `setXProperty(...)`
* If the property should be readonly omit `setXProperty(...)` and make sure `xProperty()` returns a readonly property.
* Define all properties of classes serving as instance variables where you declared them and make them final.
* When accessing a property prefer calling `getXProperty()` instead of directly accessing it.
#### Controller
* An actual controller should not have any subclasses unless related to a wizardable window. If so it should have one subclass which has no subclasses itself.
* Declare controller without subclasses as final.
* Associate controller within FXML files.
#### Utility classes
* A utility class has to be in a package under `bayern.steinbrecher.green2.utility`
* It has to have a name like `SomeUtility`.
* It has to be a `final class`.
* It has to define a `private` constructor like:
    ```
    private SomeUtility(){
        throw new UnsupportedOperationException("Construction of an object is not allowed.");
    }
    ```
### Packages
* Every package name has to start with `bayern.steinbrecher.green2`
* All packages have to define a `package-info.java`
    * If a certain package is part of projects `package-info.java` has to be identical.

### FXML
#### FXML based classes used in wizards
* There have to be two fxml files:
    1. `SomeClassWizard.fxml` which contains the actual content and data of the dialog such that it can be used in a wizard.
    2. `SomeClassParent.fxml` which uses `fx:include` to include `SomeClassWizard.fxml` and adds controls like finish or cancel to control the dialog without using it with a wizard.
* There have to be two controller:
    1. `SomeClassController` extending `WizardableController` and providing all functions and properties needed by `SomeClassWizard.fxml`. This controller also contains methods for requesting the actual results of the window.
    2. Package private class `SomeClassControllerParent` extending `SomeClassController` and adding the functions needed for the controls of `SomeClassParent.fxml`.
* There has to be at most one view which references `SomeClassController` for requesting results and loads either `SomeClassParent.fxml` or `SomeClassWizard.fxml`.
* It may be neccessary for `SomeClassControllerParent` to add a listener to some property of `SomeClassController` e.g. to add submit-on-enter functionality. Realize by defining a protected method called `addListenerToSomeProperty(ChangeListener<...>)`. Call this method the default constructor of the subclass.
#### CSS includes
* If a CSS file to include is in the same package include it within FXML.

### Files
#### CSS files
Place CSS files containing rules for specific classes or dialogs in the same package as the class they are refered to.

## Language features

### Java
#### var keyword
* Avoid the var keyword in general
* `var` is allowed in direct definitions of variables like `var someObject = new SomeObject()
* If the type is generic and `var` is used on the left side the right side should not use the diamond operator `<>` but specify the generic type explitely.
#### CSS
* Avoid `!important`.
* Omit any unit when specifying lengths or sizes of zero.
#### Initializable(...)
* When overriding initializable(...) of any class call `super.initializable(...)`.

## JavaDoc
* Document every non-`private` class, method, field,...
