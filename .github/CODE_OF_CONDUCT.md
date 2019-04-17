# Code Of Conduct

Code of conduct includes the requirements you should read before making any contribution.

## Our Standards

### The preferences, by the core development team
- You should use preferred IDE as Eclipse IDE.
- You should include license headers and author section in new added files.
- You should end new files with a new line. Read more: <a href="https://stackoverflow.com/questions/729692/why-should-text-files-end-with-a-newline">Why should text files end with a newline?</a>
- You should not make any breaking changes and test at least one time your changes. You must check compile time errors and you should check runtime errors from a test server before making a pull request. (Tip: If you have a null warning or error, just suppress it.)

### Some information about files and folders
- The files under .github folder is github-specific files for GitHub.
- The files under .settings folder are for Eclipse settings.
- The files under src are the source files for the project.
- Files starting with "." in main folder are git or eclipse options.
- Files named with a full uppercase letter in the main folder are for GitHub.
- You can build the project using build.bat, you must run this file via administrator permissions, we have a a shortcut (.ink file) that runs the file via administrator perms, just use it or directly right click the build.bat file and run with administrator perms.
- The files under src/excluded are excluded files, not included in built project.
- The files or folders under src/test are also excluded, but it's for tests.
- The plugin.yml, config.sk etc. files are in the src/resources folder. These are automatically added to built jar file by Maven.
- Look the above three statements, for this situation, the real source is under src/main.
- The Skript's actual source code can be found under src/main/java/ch/njol folder. The others (currently yggdrasil and util) are just dependencies, owned by Skript, but not directly Skript. It have general resources.
