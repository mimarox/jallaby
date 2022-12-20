# Jallaby
Jallaby is a hierarchical state machine container

## Get it now and start implementing and running state machines
The first beta version of Jallaby Beans has just been released. Get it at http://jallaby.org/downloads/jallaby-beans-1.0.1-beta.zip and start implementing and running state machines!

To implement a state machine just start a new project and let it depend on <code>org.jallaby:jallaby-beans-api:jar:1.1.1</code>. Use the annotations within this lib to write your classes and have a look at the Jallaby Beans samples at https://github.com/mimarox/jallaby-samples.

When your done implementing build your project and put the jar-with-dependencies inside the deploy folder of your extracted jallaby-beans download. Then run cmd in Windows, go to the main folder of your jallaby-beans installation, type "jallaby", hit Enter and see the Jallaby Beans server starting. Have fun experimenting with it and drop me a line for any requests or improvements.

## Use the Maven Archetype to create your Jallaby Beans project
Just enter <code>mvn archetype:generate -DarchetypeGroupId=org.jallaby -DarchetypeArtifactId=jallaby-beans-maven-archetype -DarchetypeVersion=1.0.0 -DgroupId=&lt;groupdId&gt; -DartifactId=&lt;artifactId&gt;</code> on your command prompt and hit Enter. Specify the project version interactively and voila, you've got your Jallaby Beans project up and running.

Build your project with <code>mvn clean install</code> and copy the file ending in <code>.sma</code> from the deploy folder into the deploy folder of your Jallaby Beans installation and, when the Jallaby server is already running, it will pick up your new state machine and let it run.

# Dedication
This software and all Jallaby related projects are dedicated to my lovely kids, Jonathan and Leonie. May our Lord bless you abundantly!
