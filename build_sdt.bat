rem set eclipse_ant=C:/Users/m526092/eclipse/plugins/org.eclipse.equinox.launcher_1.3.0.v20140415-2008.jar
rem test EE6
"%JDK_HOME%\bin\java" -jar %eclipse_ant% -application org.eclipse.ant.core.antRunner -buildfile workspace.xml all_packages -verbose