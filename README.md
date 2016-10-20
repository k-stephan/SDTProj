This is a Java/cucumber framework which uses Selenium to run website tests.

All the documentation related to the framework can be found here:
http://confluence5/pages/viewpage.action?pageId=35160962

If you want to use the framework, you can add it as a maven dependency:

<dependency>
    <groupId>com.macys.sdt</groupId>
    <artifactId>sdt-framework</artifactId>
    <version>LATEST</version>
</dependency>
<dependency>
    <groupId>com.macys.sdt</groupId>
    <artifactId>sdt-framework</artifactId>
    <version>LATEST</version>
    <classifier>sources</classifier>
</dependency>
<dependency>
    <groupId>com.macys.sdt</groupId>
    <artifactId>sdt-framework</artifactId>
    <version>LATEST</version>
    <classifier>javadoc</classifier>
</dependency>

<repository>
    <id>Macys_SDT</id>
    <url>http://11.142.14.56:8081/nexus/content/repositories/Macys_SDT</url>
    <releases>
        <enabled>true</enabled>
        <updatePolicy>always</updatePolicy>
    </releases>
    <snapshots>
        <enabled>true</enabled>
        <updatePolicy>always</updatePolicy>
    </snapshots>
</repository>