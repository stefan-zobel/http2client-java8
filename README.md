# http2client-java8

![](art/streamsupport-sf.png)

An experimental Java 8 backport of the incubating Java 10 high-level HTTP and WebSocket API (the `jdk.incubator.http` package).

HTTP 1.1 and 2 are both supported, as is SSL. Works also on Java 9 and Java 10. The minimum runtime requirement is OpenJDK (Oracle) Java 8.


Since the `CompletableFuture` from Java 8 doesn't implement the (`JEP 266`) features needed for the Java 9 implementation
of the HTTP client a backport of the Java 9 CompletableFuture is necessary as a Maven dependency:

```xml
<dependency>
    <groupId>net.sourceforge.streamsupport</groupId>
    <artifactId>java9-concurrent-backport</artifactId>
    <version>1.1.0</version>
</dependency>
```

Logging is not functional yet. The code has seen very little testing and is still in development. Don't use this in production!


## LICENSE

GNU General Public License, version 2, with the Classpath Exception
