package lib;

public final class ForceJava8Test {

   public ForceJava8Test() {
       if (Double.valueOf(System.getProperty("java.class.version")) >= 53.0) {
           // ensure that the client library believes that it is
           // running on Java 8 and thus behaves accordingly
           System.setProperty("test.java.version.pretend8", "true");
       } else {
           System.err.println("THIS TEST SHOULD BETTER NOT BE RUN ON JAVA 8");
       }
   }
}
