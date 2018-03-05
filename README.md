# Simple Search
Simple Search is a Java application that searches files based on user specified criteria, executing specific actions on the files matching the criteria.
It is possible to specify the text to search, the pattern of the filename (that can include wildcards like * and ?), matching dates, filesize range, recursive search and if the search has to be casesensitive. 

# Background
This application was developed more than ten years ago for PC Window environment. The GUI of the application is based on AWT/Swing framework.

# Compile
Run following commands to build the application:
```
javac -d . *.java
jar cmf build.mf search.jar *.java search readme.txt gpl.txt run.bat 
```

# Run
Just use the following command to run the application:
```
java search/Search
```

# Screenshot

![Screenshot](https://raw.githubusercontent.com/javalc6/simple-search/master/search/images/mainwindow.png)
