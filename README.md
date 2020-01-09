# Install dependencies
## Install Java 8
* https://www.oracle.com/technetwork/java/javase/downloads/java-archive-javase8-2177648.html
* Version 1.8.0_172 at the time of this writing. Need to look in the IntelliJ project.
## Install [Homebrew](https://brew.sh/).
```
brew install maven mediainfo exiftool imagemagick findutils mysql-client yarn redis
```
## Add MySQL client path to your environment
Homebrew does not automatically link the mysql client into your /usr/local/bin so add to your environment:
```bash
PATH=$PATH:/usr/local/opt/mysql-client/bin
```
# Getting application code
1. Set up SSH access for GitHub:  https://help.github.com/articles/generating-a-new-ssh-key-and-adding-it-to-the-ssh-agent/
2. Pull the narrative-platform repo, which includes an IntelliJ project and modules, and open the folder in IntelliJ. 
3. Run `mvn dependency:tree` from the command line once to get all dependencies. Our GCP-hosted Onfido dependency has issues downloading inside of IntelliJ, so you should only need to do this once on the CLI, and then you can do it in IntelliJ from that point forward.
4. Run `mvn clean install -Dmaven.test.skip=true` to build everything and get the shared dependency into place in the local `.m2` directory. This will enable you to run the reputation bootstrap at the CLI below.

### Useful Chrome Extensions

[Apollo Client Developer Tools](https://chrome.google.com/webstore/detail/apollo-client-developer-t/jdkknkkbebbapilgoeccciglkfbmbnfm), 
[React Developer Tools](https://chrome.google.com/webstore/detail/react-developer-tools/fmkadmapgofadopljbjfkapdkoienihi)

### IntelliJ Plugins

* NodeJS
* JS GraphQL
* Rainbow Brackets
* Styled Components
* BashSupport
* GMavenPlus Intellij Plugin
* Grep Console
* Lombok Plugin
* Mapstruct Support
* Plant UML integration
* Spock Framework Enhancements
* String Manipulation
* nginx Support
* .ignore

### Test IntelliJ `react-ui` Indexing

We've been having intermittent problems with IntelliJ not indexing react-ui files properly. I've fixed the module file as best I can in #2543. Test to make sure the index is working. Use IntelliJ to open the `KycSchema.graphql` file by Search Everywhere (Double-`<shift>`) or Go to File (`Cmd-Shift-N`). Type in `KycSch` and see if IntelliJ auto-completes to the proper file. If it works, you're set!

If it doesn't find the file, then IntelliJ's index is messed up. To fix the problem, do the following:

1. Go into Project Structure (`Cmd-;`).
2. Click Modules.
3. Click on the `react-ui` module.
4. Click the minus to remove it from the project.
5. Hit OK.
6. Shut down IntelliJ.
7. Re-open IntelliJ.
8. Go back into Project Structure (`Cmd-;`).
9. Click Modules.
10. Click the `+` at the top of the module column.
11. Click Import Module.
12. Open the `react-ui.iml` module that is in the `react-ui` directory.
13. Hit Apply.
14. Important: watch the bottom status bar in IntelliJ. If it's indexing, wait for it to finish _before_ clicking OK to close the window.
15. Once indexing is complete, click OK and the window should close.

At this point, IntelliJ should re-index the files in `react-ui`. You can run the same index test search above now to make sure it's working. If it's not, go find a bridge.

# Configuring local environment in the code base
Copy and modify the following to correspond with your local settings:
* application-local_sample.properties to application-local.properties
  - `server.servlet.context-parameters.isDevServer` should be set to `true`.
  - `narrative.storage.credentialsPath` must point to a `local-images.json` file that you will have to create. The contents of this file can be found in 1Password in the Development vault: "local-images.narrative.org Google Bucket Key"
  - You can leave `narrative.storage.bucket=local-images.narrative.org`.
  - `narrative.storage.blobPathPrefix` needs to be set to a unique name for your environment. I'd recommend using your GitHub username.
  - `narrative.kycStorage.credentialsPath` must point to a `local-certification-files.json` file that you will have to create. The contents of this file can be found in 1Password in the Development vault: "local-certification-files.json Google Bucket Key"
  - You can leave `narrative.kycStorage.bucket=local-certification-files`.
  - mysqldump will try to use a socket file when 'localhost' is specified as the target host.  This will not work since MySQL is running in a container. Ensure the property for **global.server** has a value of **127.0.0.1**  
* logback-sample.xml to logback.xml
* src/main/config/install.sample.properties to install.properties.  Add the following properties:
  - admin.email=admin@email.com
  - admin.password=password
  - admin.displayName=adminName


# Setting up local developer infrastructure
## Frontend init & launch
from the `react-ui/` directory:

* first run

```
yarn install
```

when making changes within `shared/`

```
yarn workspace @narrative/shared refresh
```

to build and start the frontend

```
yarn workspace @narrative/web start
```

To rebuild the `es.json` and other language files:

```
yarn workspace @narrative/web translate:build
```

## Stand up DB resources
Run the docker-compose from the narrative-platform root/docker directory to bring up the MySQL and Solr databases, Nginx and Redis: 
```
docker-compose -f  local-compose.yml up 
```
You can easily and quickly switch between the old and new clients by changing the / mapping in nginx.conf from             `proxy_pass http://host.docker.internal:3000;` to `proxy_pass http://host.docker.internal:8080;`
and running `nginx -s reload` in the nginx container

## Local Topology

* Yarn (Express) server runs on port 3000 (and will auto-start when you run the front end code).
* Nginx forwards all traffic to the Spring/Tomcat instance by default, but can be set up to work with Prerender.
* The Spring/Tomcat instance and Narrative Reputation module both use MySQL and Redis. MySQL and Redis both run locally in Docker via local-compose.yml.
* Solr servers are used for search. Solr runs locally in Docker via local-compose.yml.
* Mail delivery can be set up through any SMTP service (e.g. SendGrid).
* The "cluster control panel" (aka management console) web interface is accessible via HTTP on port 8082.

## Initialize the schema
### Init Reputation
1. Go into `reputation/scripts`.
2. Run `./createDBAndExecAdminPredeploy` (all of the default args should work). Ignore the error about `WatchedUser` for now. It's a chicken and egg problem.
3. Run `./updateDB` (again, all of the default args should work).

### NetworkInstall
1. Open the pre-configured IntelliJ project by pointing IntelliJ at narrative-platform
2. "Reimport All Maven Projects" from the Maven tab to ensure IntelliJ libraries are all set up properly
3. Build -> Rebuild Project
4. Execute the 'NetworkInstall' run target

### Init Reputation (again)
1. Repeat the Init Reputation steps 1-3 from above. There shouldn't be an error during step 2 this time since `WatchedUser` will exist now. (We need to fix this eventually, but it should work for now.)

![alt text](https://media.giphy.com/media/l0MYt5jPR6QX5pnqM/giphy.gif "Party")

[Markdown Cheat Sheet](https://github.com/adam-p/markdown-here/wiki/Markdown-Cheatsheet#code)

## Run Application

* Run the application locally via the NarrativeApplication Spring run config in IntelliJ.
* Run the Reputation module locally via the NarrativeReputationApplication Spring run config in IntelliJ.

## Deployment via Spring Boot
VM Options:
* `-Djruby.native.verbose=true `
* `-Dorg.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH=true `
* `-Dorg.apache.el.parser.SKIP_IDENTIFIER_CHECK=true `
* `-Djava.net.preferIPv4Stack=true `
* `-ea -server -Xms256m -Xmx2048m`

# Running Liquibase via Maven
Make sure the parent pom has been installed into your local .m2 database. You can install it without building everything by running `mvn -N install` from the project root.

There is a Run Configuration in IntelliJ (narrative-reputation [liquibase:update]) that you can use to run liquibase. You'll just need to set the root db password and the password that you want to use for the reputation user before you run.

You can also run from the command line while in the reputation project with `mvn liquibase:update -Dreputation_user_password=AAA -DjdbcUrl=jdbc:mysql://localhost:3306/reputation?createDatabaseIfNotExist=true -DrootDbUser=root -DrootDbPassword=password`. Again, you'll need to set appropriate values for reputation_user_password, and rootDbPassword.

# Using `docker/db-ctl.sh`

This tool will allow you to list, copy, use, and delete a user's local-compose databases. Need to make a backup of your DB? Need to switch to a saved DB? Need to list your DBs? Need to delete a saved DB? Use this tool.

All commands are to be run from the root directory of the narrative-platform project.

To get help:

```
➜ docker/db-ctl.sh -h
  Usage: docker/db-ctl.sh [CMD] [ARG]

  List images:
    docker/db-ctl.sh ls

  Make a snapshot of the current DBs and name it 'foo' (this WILL stop any running local-compose...):
    docker/db-ctl.sh cp foo

  Use the snapshot named 'bar':
    docker/db-ctl.sh use bar

  Delete the snapshot named 'zed':
    docker/db-ctl.sh rm zed
```

To list:

```
➜ docker/db-ctl.sh ls
Active snapshot:
  2018-12-10-09-17-01
List of snapshots:
  2018-12-05-11-18-40
  2018-12-10-09-17-01
  2018-12-05-11-21-09
```

To create snapshot:

```
➜ docker/db-ctl.sh cp foo
Stopping local-compose...
Making a copy of the active data: 2018-12-05-11-18-40 ...
Copy complete!
Active snapshot:
  2018-12-05-11-18-40
List of snapshots:
  2018-12-05-11-18-40
  foo
  2018-12-05-11-21-09
```

To use a snapshot:

```
➜ docker/db-ctl.sh use foo
Active snapshot:
  foo
List of snapshots:
  2018-12-05-11-18-40
  foo
  2018-12-05-11-21-09
```

To remove a snapshot (NOTE - cannot delete an active snapshot):

```
➜ docker/db-ctl.sh rm foo
Deleting foo ...
Done!
Active snapshot:
  2018-12-05-11-21-09
List of snapshots:
  2018-12-05-11-18-40
  2018-12-05-11-21-09
```