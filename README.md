android-jersey-client
=====================

An plug-and-play Android version of the Jersey 1.x client library based on v1.17.1.

Current port is based on the v1.17.1 available at http://java.net/projects/jersey/sources/svn/show/trunk/jersey  

To use it in your Android Maven projects you need to add the following repository and dependency to your project pom.xml.  

The repository :  

    <repositories>  
      ...  
      <repository>  
        <id>Android Jersey Client API REPO</id>  
        <url>http://avianey.github.io/android-jersey-client/</url>  
      </repository>  
    </repositories>

The dependency :  

    <dependencies>
      ...
      <dependency>
        <groupId>fr.avianey.android-jersey-client</groupId>
        <artifactId>jersey-client</artifactId>
        <version>1.17.1</version>
      </dependency>
    </dependencies>
