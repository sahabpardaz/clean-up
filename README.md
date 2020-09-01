# Clean-up

This library helps to free up all desired resources and clean-up operations by a single call.

By using this library, we will make sure that all operations, will be performed if it is possible.
That means, if any exceptions occurs during clean-up operations of a resource, it will
not prevent clean-up of other resources.

### Sample Usage

`Cleanups` accepts a list of `AutoCloseable` and `RunnableWithException`.
Upon calling `doAll()` all `AutoCloseable` will be called and all `RunnableWithException` statements will be executed.

An example usage may look like this:

```java
List<FlowTracker> appThreads = new List<>();
// Populate list with your own flow trackers ...
// Resources taken by flow trackers must be freed by calling {@code close()} method.

HttpServer httpServer = new HttpServer(...);
JolokiaServer jolokiaServer = new JolokiaServer(...);
Application application = new Application(jolokiaServer, httpServer, appThreads);

// Do whatever you want to do.

Cleanups.of(application::close, jolokiaServer::stop, httpServer::stop)
        .and(appThreads)
        .doAll();
 ```

In above example, `application`, `jolokiaServer` and `httpServer` are closed, respectively. Then, the list
of all threads will be closed.

If any exceptions occur during these operations, they will be logged but other operations will not be interrupted.
That is, we ensure that all operations, will be performed if it is possible.
