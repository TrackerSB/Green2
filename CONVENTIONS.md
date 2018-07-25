# Guidelines and conventions

As far as possible these conventions are checked by the checkstyle plugin.

## Style

### Java
* If not explicitely otherwise specified apply the Java coding conventions [(http://www.oracle.com/technetwork/java/codeconventions-150003.pdf)](http://www.oracle.com/technetwork/java/codeconventions-150003.pdf).
* The line length mentioned in section 4.1 is limited to 120 characters.
* When chaining commands every command is in its own line like:
    ```
    someObject.commandA()
        .commandB()
        .commandC()
        .commandD();
    ```

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
* When accessing a property within a constructor prefer direct access.
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
        throw new UnsupportedOperationException("Construction of an object is prohibited.");
    }
    ```
#### Logging
* Each class needing a logger has to define it as `private static final Logger LOGGER = Logger.getLogger(MyClass.class.getName());`.
* This is the only logger to be used by that class.
* If it is an enum use `Logger.getLogger(MyClass.class.getName())`.
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
#### Bind properties of elements in FXML files
* Prefer to bind properties of elements defined in FXML files using FXML instead of binding them within its controller.
#### CSS includes
* If a CSS file to include is in the same package include it within FXML.

### Files
#### CSS files
Place CSS files containing rules for specific classes or dialogs in the same package as the class they are refered to.

## Licenses
* Add the license to every file before the package declaration.
* Do not add a license to `package-info.java` files.

## Language features

### Java
#### var keyword
* Avoid the var keyword in general
* `var` is allowed in direct definitions of variables like `var someObject = new SomeObject()
* If the type is generic and `var` is used on the left side the right side should not use the diamond operator `<>` but specify the generic type explitely.
#### default keyword for methods
Do not use `default` methods.
#### Initializable(...)
* When overriding initializable(...) of any class call `super.initializable(...)`.
#### Stream API
* Prefer `Stream#forEach(...)` over `Iterable#forEach(...)`.
### CSS
* Avoid `!important`.
* Omit any unit when specifying lengths or sizes of zero.

## JavaDoc
* Document every non-`private` class, method, field,...
* When referencing any method, interface, class, etc. use `{@link ...}`.
* When a class or interface is solely referenced within JavaDocs use full qualified name instead of an import to avoid "unused" imports.
* Document packages using a `package-info.java`. When multiple modules contain the same package add only one package description. If they depend on each other prefer the module they depend on.
