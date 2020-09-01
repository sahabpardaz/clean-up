# Clean up

This library helps to free up all desired resources or do all clean-up operations by a single call.

By using this library, we will make sure that all operations, will be performed if it is possible.
That means, if any exceptions occurs during clean-up operations of a resource, it will
not prevent clean up of other resources.

### Sample Usage

An example usage may look like this:

```java
Cleanups.of(...)
        .and(...)
        .and(...)
        .and(...)
        .doAll();
 ```

It accepts a list of `AutoCloseable` and `RunnableWithException`.
Upon calling `doAll()` all `AutoCloseable` will be called and all `RunnableWithException` statements will be executed.

If any exceptions occurs during these operations, other operations will not be interrupted.
That is, we ensure that all operations, will be performed if it is possible.
