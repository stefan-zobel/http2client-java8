[![Maven Central](https://img.shields.io/maven-central/v/net.sourceforge.streamsupport/http2client-java8.svg)](http://mvnrepository.com/artifact/net.sourceforge.streamsupport/http2client-java8)

# http2client-java8

![](art/streamsupport-sf.png)

An experimental Java 8 backport of the incubating Java 10 high-level HTTP and WebSocket API (the `jdk.incubator.http` package).

HTTP 1.1 and 2 are both supported, as is SSL. Works also on Java 9 and Java 10. The minimum runtime requirement is OpenJDK (Oracle) Java 8.


```xml
<dependency>
    <groupId>net.sourceforge.streamsupport</groupId>
    <artifactId>http2client-java8</artifactId>
    <version>0.1.1</version>
</dependency>
```


Since the `CompletableFuture` from Java 8 doesn't implement the (`JEP 266`) features needed for the Java 8 implementation
of the HTTP client a backport of the Java 9 CompletableFuture is necessary as a Maven dependency:

```xml
<dependency>
    <groupId>net.sourceforge.streamsupport</groupId>
    <artifactId>java9-concurrent-backport</artifactId>
    <version>1.1.1</version>
</dependency>
```

Status:

* Logging is not functional yet
* Almost all of the tests are working now without resorts to daring gimmicks
* Ready to use by the more adventurous developer


## LICENSE

GNU General Public License, version 2, with the Classpath Exception
