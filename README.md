# JACS Domain Model and DAOs

[![CircleCI](https://circleci.com/gh/JaneliaSciComp/jacs-model.svg?style=svg)](https://circleci.com/gh/JaneliaSciComp/jacs-model)

## Build

Run the unit tests:
```
/gradlew test
```

Build the jar:
```
./gradlew jar
```

Publish the jar to the local maven repo:
```
./gradlew publishToMavenLocal
```

To publish the jar to the remote repo you have to provide your maven repo credentials as gradle properties, e.g.
```
./gradlew -PmavenRepoUser=YourUserName -PmavenRepoPassword=YourPassword publish
```

There is also a publish script that takes username and password as positional arguments or if they are not provided it prompts the user to enter them as shown below:
```
./publish.sh [<username> [<password>]]
```


If you have the properties mavenRepoUser and mavenRepoPassword already defined in ${HOME}/.gradle/gradle.properties then you don't have to specify them in the command line.


Note: If the publish fails with a 'sun.security.validator.ValidatorException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target' exception you will have to add Janelia wildcard certificate to your Java trusted store and then kill all running gradle daemons.

If you already have the certificate locally on your file system, install it using the keytool command, as show below otherwise first download the certificate using 'gnutls'.
```
gnutls-cli nexus.janelia.org --print-cert
```

In the example below 'cert.crt' is the Janelia wildcard certificate. When prompted for the password, if you have not changed your keystore password, the default is 'changeit'.

```
sudo keytool -importcert -alias JaneliaWildcard -file cert.crt -keystore /Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/jre/lib/security/cacerts
```

## Summary

This module contains common domain models and DAOs which can be used to access data stored in the JACS databases.

Currently, this module is attempting to unify the fragmented JACSv1 and JACSv2 models. It mainly contains models from v1, which will be shared between v1 and v2 until all functionality from v1 can be migrated to v2. 

## Contents

* **org.janelia.model**
    * **access** - DAO's and other API's used for data access.
        * **domain** - Contains legacy DAO's from JACSv1. This is the current production API.
        * **tiledMicroscope** - Classes for dealing with in-memory manipulation of the "Tm" (Tiled microscope) module for the Large Volume Tools
    * **domain** - Document-based domain model for Fly Confocal Imaging and MouseLight. These document classes are directly serialized into MongoDB, and are currently used in the web service interfaces.
    * **sage** - Contains the Hibernate-based model for communicating with the SAGE database. In the future, this will be replaced with a SAGE web service.
    * **security** - Contains model classes for the JACS security model.
    * **util** - Useful utilities

## License 

[Janelia Open Source License](https://www.janelia.org/open-science/software-licensing)
