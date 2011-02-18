Welcome to Drools
=================

Read this document if you want to build or contribute to the drools project.

Drools uses Maven 3 to build the project and all it's modules.


Installing Maven
================

1) Get and configure Maven.

Download Maven from http://maven.apache.org/
Follow the installation instructions.

Linux:
Hint: unzip maven to ~/opt/build
and create a link to place in your PATH:
$ cd ~/opt/build/
$ ln -s apache-maven-3.0.1 apache-maven
$ export PATH="~/opt/build/apache-maven/bin/:$PATH"

2) Give more memory to maven, it will need it to build this big project.

Linux:
$ export MAVEN_OPTS="-Xms256m -Xmx1024m -XX:MaxPermSize=512m"

Windows:
> set MAVEN_OPTS="-Xms256m -Xmx1024m -XX:MaxPermSize=512m"

3) Check if maven is installed correctly.
$ mvn --version


Building with Maven
===================

Go into the project base directory.
$ cd drools
$ ls
You'll find this README.txt file in there.
Notice you also see a pom.xml. Those pom.xml files are the heart of Maven.

Run the build
$ mvn -DskipTests clean install

Or better yet, run the full build (this actives the profile "fullProfile")
$ mvn -Dfull -DskipTests clean install

The first build will take a long time, because a lot of dependencies will be downloaded (and cached locally).
It might even fail, if certain servers are offline or experience hiccups.
In that case, you 'll see an IO error and just run the build again.
After the first successful build, any next build should be fast and stable.

There are 3 profile activation properties:
- <default>: Fast, for during development
- full: Slow, but builds everything (including eclipse plugins and documentation). Used by hudson and during releases.
- soa: prunes away the non-enterprise stuff


Releasing
=========

Use JDK 1.6, because in JDK 1.5 the module guvnor-repository-connector-modeshape is not build.

To produce distribution builds use:
$ mvn -Dfull clean install
$ mvn -Dfull -DskipTests package javadoc:javadoc assembly:assembly

Note that install must be done first as javadoc:javadoc won't work unless the
jars are in the local maven repo, but the tests can be skipped on the second run.

Configuring settings.xml for maven
----------------------------------

Read this document:
  http://community.jboss.org/wiki/MavenGettingStarted-Developers
so you can:
 - deploy artifacts to the jboss repository
 - easily use the jboss plugins


Configuring Eclipse
===================

Avoid a StackOverflowError when building:
Open ECLIPSE_HOME/eclipse.ini and add/change this on openFile -vmargs:
-XX:MaxPermSize=512m
-Xms512m
-Xmx1024m
-Xss1024k

Force language level 5 (not 6), to fail-fast on implemented interface methods that are annotated with @Override
- Open menu Window, menu item Preferences
-- Tree item Java, Tree item Compiler, section JDK Compliance, combobox Compiler compliance level should be "1.5"

There are 2 ways to configure Eclipse based on Maven's poms.

The maven-eclipse-plugin way
----------------------------

The maven-eclipse-plugin plugin is a plugin in Maven for Eclipse.
This is the old way.
Run this command to generate .project and .classpath files.
$ mvn eclipse:eclipse
- Open Eclipse
- Import existing projects, navigate to the project base directory, select all the projects (=modules) it lists.

Important note: mvn eclipse:eclipse does not work for drools-eclipse because it is not compatible with tycho
(and never will be).

The m2eclipse plugin way
------------------------

The m2eclipse plugin is a plugin in Eclipse for Maven.
This is the new, deluxe way (and compatible with tycho).
- Open Eclipse
- Follow the installation instructions of m2eclipse: http://m2eclipse.sonatype.org/
-- Follow the link Installing m2eclipse at the bottom.
- Menu File, menu item Import, tree item Maven, tree item Existing Maven Projects
- Open the main pom.xml with the m2eclipse plugin.
- Select the profiles "notSoaProfile" and "fullProfile".

Code style
----------

Correct number of spaces for tabs:
- Open menu "Window", menu item "Preferences".
-- If you have project specific settings enabled, right click on the project and click the menu item "Properties".
- Open tree item "Java", tree item "Code Style", tree item "Formatter".
-- If you imported the trunk/eclipse-formatter.xml file,
   you don't need to set it here,
   but you do need to set it for XML anyway!
-- Click button "Edit" of the active profile
-- Tab "Indentation"
--- Combobox "Tab policy": spaces only
--- Indentation size: 4
--- Tab size: 4
-- If it is a build in profile, you need to change its name with the textfield on top
- Open tree item "XML", tree item "XML Files", tree item "Editor".
-- Radio button "Indent using space": on
-- Indentation size: 2

Correct file encoding (UTF-8 except for properties files) and EOL (unix):
- Open menu "Window", menu item "Preferences".
- Open tree item "General", tree item "Workspace".
-- Label "Text file encoding", radiobutton "Other", combobox "UTF-8"
-- Label "New text file delimiter", radiobutton "Other", combobox "Unix"
- Open tree item "XML", tree item "XML Files".
-- Combobox "Encoding": ISO 10646/Unicode(UTF-8)
- Open tree item "CSS", tree item "CSS Files".
-- Combobox "Encoding": ISO 10646/Unicode(UTF-8)
- Open tree item "HTML", tree item "HTML Files".
-- Combobox "Encoding": ISO 10646/Unicode(UTF-8)
- Note: i18n properties files must be in ISO-8859-1 as specified by the java ResourceBundle contract.

Correct file headers (do not include @author or a meaningless javadoc):
- Open menu "Window", menu item "Preferences".
-- Tree item "Java", tree item "Code Style", tree item "Code Templates"
--- Tree "Configure generated code and comments", tree item "Comments", tree item "types"
--- Remove "@author Your Name" line.
---- We do not accept @author lines in source files, see FAQ below.


License header
--------------

Eclipse JEE Helios currently has no build-in support of license headers,
but you can configure it for new files.
- Open menu "Window", menu item "Preferences".
-- If you have project specific settings enabled, right click on the project and click the menu item "Properties".
- Open tree item "Java", tree item "Code Style", tree item "Copy templates".
-- Open tree item "Comments", tree item "Files".
-- Replace the text area with this:
/*
 * Copyright 2011 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
-- Do not start or end with a newline character
-- Update the year (2011) every year.


Configuring IntelliJ
====================

The project is big, so you will want to give your IntelliJ more memory.
Open $IDEA_HOME/bin/idea.vmoptions and change the first 3 values to this:
-Xms512m
-Xmx1024m
-XX:MaxPermSize=512m

Don't use the maven-intellij-plugin: it's dead.

IntelliJ has very good build-in support for Maven.
- Open IntelliJ.
- Open new project.
- Open the main pom.xml file from the project base directory.
- Select the profiles "notSoaProfile" and "fullProfile".
- Go grab a coffee while it's indexing.

Verify other settings:
- Open menu File, menu item settings:
- Tree item Compiler, textfield Resource patterns
-- Change the contents "!?*.java" (without the double quotes)
-- This is to avoid that changes in some resources are ignored in the next run/debug (and you are forced to use mvn)
- Tree item compiler, tree item Java Compiler, textfield Additional command line parameters
-- Add " -J-Xss1024k" so it becomes something like "-target 1.5 -J-Xss1024k"
-- This is to avoid an StackOverflowError when building
- Tree item File Types, in the list Recognized File Types, select XML Files.
-- Add Registered Pattern "*.rf" (without the double quotes)
-- This is to avoid that the XML ruleflow files are not included in searches/refactors
- Tree item File Types, in the list Recognized File Types
-- Button Add...
--- Textfield name "DRL files"
--- Textfield Line comment "//", textfield Block comment start "/*", textfield Block comment end "*/"
--- Check the checkboxes Support paired braces, Support paired brackets, Support parens
--- Add some keywords, such as rule, when, then, end
--- Button ok
-- Add Registered Pattern "*.drl" and "*.mvel".

Force language level 5 (not 6), to fail-fast on implemented interface methods that are annotated with @Override
- Open menu File, menu item Project Structure
-- List item Modules, for each module, tab Sources, combobox Language level should be "5.0 ..."

Code style
----------

Correct number of spaces for tabs:
- Open menu "File", menu item "Settings".
- Open tree item "Code Style", tree item "General".
- Open tab "Java"
-- Checkbox "Use tab character": off
-- Textfield "Tab size": 4
-- Textfield "Indent": 4
-- Textfield "Continuation indent": 8
- Open tab "XML"
-- Checkbox "Use tab character": off
-- Textfield "Tab size": 2
-- Textfield "Indent": 2
-- Textfield "Continuation indent": 4

Correct file encoding (UTF-8 except for properties files) and EOL (unix):
- Open menu "File", menu item "Settings".
- Open tree item "Code Style", tree item "General".
-- Combobox "Line separator (for new files)": Unix
- Open tree item "File Encodings".
-- Combobox "IDE Encoding": "UTF-8"
-- Combobox "Default encoding for properties files": ISO-8859-1

Correct file headers (do not include @author or a meaningless javadoc):
- Open menu "File", menu item "Settings".
- Tree item File templates, tab Includes, list item File Header
-- Delete the contents of the text area.
--- Remove "@author Your Name" line.
---- We do not accept @author lines in source files, see FAQ below.
--- Do not include an empty or meaningless javadoc. Only add a javadoc if it has meaningful content.

License header
--------------

- Open menu "File", menu item "Settings".
- Open tree item "Copyright", tree item "Copyright profiles".
- Add Copyright profile
-- Textfield name: JBoss Inc
-- Fill this into the text area:
Copyright $today.year JBoss Inc

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-- Do not start or end with a newline character
- Open tree item "Copyright"
-- Combobox "Default project copyright": JBoss Inc


FAQ
===

Why do you not accept author lines in your source code?
- Becuase the author tags in the java files are a maintenance nightmare
-- A large percentage is wrong, incomplete or inaccurate.
--- Most of the time, it only contains the original author. Many files are completely refactored/expanded by other authors.
-- Git is accurate, that is the canonical source to find the correct author.
- Because the author tags promote "code ownership", which is bad in the long run.
-- If people work on a piece they perceive as being owned by someone else, they tend to:
--- only fix what they are assigned to fix, not everything that's broken.
--- discard responsibility if that code doesn't work properly.
--- be scared of stepping of the feet of the owner
-- More info: How to get a healthy open source project? http://video.google.com/videoplay?docid=-4216011961522818645#

Credit to the author of peices of code is given:
- on the team page: http://www.jboss.org/drools/team
-- Please contact Geoffrey (or any of us) if you want to change/expand your entry in the team page. Don't be shy!
- on the blog: http://blog.athico.com
-- Just request write permission if you don't have it yet and want to write an article.
- with ohloh, which also has statistics: https://www.ohloh.net/p/jboss-drools/contributors
- in the github web interface

