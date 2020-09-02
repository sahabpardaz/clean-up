# Clean-up

This library helps to free up all desired resources and clean-up operations by a single call.

By using this library, we will make sure that all operations, will be performed if it is possible.
That means, if any exceptions occurs during clean-up operations of a resource, it will
not prevent clean-up of other resources.

### Sample Usage

An example usage may look like this:

```java
cleanups.of(httpServer)     // Calls httpServer.close()
        .and(this::stopWorkingThreads())
        .and(this::cleanTempFolder())
        .and(dbConnection)  // Calls dbConnection.close()
        .doAllQuitely();    // If there is an exception on any operation just logs it.
                            // You can replace it with doAll() then it throws the first exception after trying for all operations.
 ```
