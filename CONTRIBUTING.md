# What is Contributing?
Contributing, means helping this project to grow, makes you a Contributor in this repository. This the page for reading before contributing.

# Difference between Contributor and Collaborator
Contributor is a person outside of the core development team and contributed to this repository via making pull requests.
Collaborator is a person inside of the core development team and have write access to this repository.

# Contributing to this repository
Step by step:
* First, fork this project.
  * Second, create a branch for your changes. (e.g: "fix-updater")
  * Then, make changes to the created branch via commits.
  * Finally, open a pull request for your branch to be merged.
You must fill the pull request template before publishing pull request.
We review your pull request and merge to master or another target branch if we found great.

Don't be fear, we are all kind & formal here. Just open a issue if you are not knowing how to fix it, if you know how to fix it,
Then just follow the above guide and open a pull request. If your pull request merged, you can now a Contributor. Congratulations!

**Also read the <a href="https://github.com/LifeMC/LifeSkript/blob/master/PROJECT_PREFERENCES.md">project preferences</a> before making any contribution!**

# Cloning, importing and building JAR
Step by step:
* Clone this repository via Eclipse in Git Repositories section.
* After cloning, right click the Skript repository under Git Repositories section and select "Import Project"
* Select next, ok or finish in the all dialogs showed.
* Now the project is imported into eclipse. The project settings etc. are all automatically setted because we have pre-configured .settings etc. files.
* For building jar, don't use Eclipse's export function. Right-click the project and click Show In -> System Explorer, this opens the repository path in your system. After this, run the build project.ink shorcut, or the build.bat with administrator rights. For running build.bat with administrator rights, just rightclick the file and select "Run as Administrator".
* After doing all things, the project is ready and waiting you to change something. After changing "something" in the project, you can use privately for your server etc. or make a pull request (read above for how-to) for making changes available also on main project and usable by everyone used this plugin.

# Requirements for testing project
* You must enable Java Assertions. (via "java -ea -jar X.jar", the **-ea** does this.)
* You must use debug mode in Skript config. (via "verbosity: debug")
* You must have at least one script and variable to test Skript works properly.
