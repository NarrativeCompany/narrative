# Reputation schema maintenance

## Creating the reputation database

Execute the following script:
```bash
→ scripts/createDBAndExecAdminPredeploy
~/work/projects/narrative-platform/reputation ~/work/projects/narrative-platform/reputation

#########################################################################
### Execution of this script will destroy your existing reputation DB ###
#########################################################################
Do you wish to continue? -Continue?} [y/n]: y

MySQL host [127.0.0.1]:
MySQL port [3306]:
MySQL user - must have CREATE DATABASE grant [root]:
MySQL password [password]:
reputation_user password [password]: mysql: 
``` 
 and provide the host/port and user/password that has GRANT ALL access.  Additionally you will need to provide a password to use for the reputation_user user that will be created during execution of the script.
 
 The script will perform the following actions:
 1. Drop the reputation_user (if exists)
 1. Drop the 'reputation' schema
 1. Execute Liquibase against the changelog-admin-master.xml changelog with the 'predeploy' context activated
 
 ## Installing/updating the reputation schema
 ### Automatic installation/update
 Start the Reputation application - Liquibase will be executed against the db.changelog-master.xml with the 'install' and 'update' contexts active during Spring boot startup.  
 ### Manual install/update
Execute the following script and answer the prompts:
```bash
→ scripts/updateDB
~/work/projects/narrative-platform/reputation ~/work/projects/narrative-platform/reputation

MySQL URL to update [jdbc:mysql://127.0.0.1:3306]:
reputation_user password [password]:
```

## Generating a Liquibase changelog that diffs your entity classes to your local DB
Execute the following script and answer the prompts:
```bash
→ scripts/diffEntityWithDB
~/work/projects/narrative-platform/reputation ~/work/projects/narrative-platform/reputation

MySQL URL to compare to [jdbc:mysql://127.0.0.1:3306]:
reputation_user password [password]:
```
The changelog diff will be located at reputation/target/schema-diff.xml.  Note that this output will not be directly usable but should be considered a starting point to be used when creating change logs for your entity changes.

## Liquibase contexts and changeset scope
Liquibase provides a mechanism called a "context" that allows change sets to be tagged and executed based on the active context names.  This is useful for filtering change sets based on the context of when the change sets are executed.  All of the reputation change sets have a context associated with them and they fall into 2 groups each with 2 categories:
1. Admin changes that must be executed as an "admin" (root level) user
    1. **predeploy** -  executed *before* a deployment (i.e. downtime deployment)
    1. **postdeploy** - executed *after* a deployment and after the application is running.  This context is useful if there is admin level DDL that needs to be executed against objects owned by the schema managed by Liquibase.  i.e. a GRANT to another user to SELECT a table that is part of the Reputation schema

1. Schema changes executed as the schema owner user (reputation_user)
    1. **install** - DDL to be executed to create the baseline Reputation schema
    1. **update** - DDL to be executed as part of an incremental update to the Reputation schema.  These changesets will be organized by release.

**install** and **update** contexts are manages separately so that **update** DDL may be re-assigned to the **install** context later in the application lifecycle.  The **install** change sets will no longer be inspected at every application start up.  This is a task that is typically performed when a major release is fielded. 

### Directory structure
Directories corresponding with the context names discussed in [Liquibase contexts and changeset scope](#Liquibase-contexts-and-changeset-scope)
```
resources/db
├── README.md
├── admin
│   ├── changelog-admin-master.xml
│   ├── ddl
│   │   └── createdb.sql
│   ├── postdeploy
│   └── predeploy
│       ├── 001-create-users.xml
│       └── 002-create-view-admin.xml
└── changelog
    ├── db.changelog-master.xml
    ├── install
    │   ├── 001-create-tables.xml
    │   ├── 002-create-views.xml
    │   ├── 003-create-functions.xml
    │   └── 004-create-spring-batch-schema.xml
    └── update
```