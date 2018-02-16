# Guidelines and conventions

## Structure
### JavaFX properties
* When looking at a JavaFX property x the order of methods should be:
    * `xProperty()`
    * `getXProperty()`
    * `setXProperty(...)`
    * If the property should be readonly omit `setXProperty(...)` and make sure `xProperty()` returns a readonly property.
* When accessing a property calling `getXProperty()` should be preffered instead of directly accessing it.
