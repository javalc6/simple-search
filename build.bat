@rem build 2.0
@rem can be used only with package!
@mkdir search
del search\*.class
javac -d . *.java
jar cmf build.mf search.jar *.java search readme.txt gpl.txt run.bat 