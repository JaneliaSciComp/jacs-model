# JACS Domain Model and DAOs

## Build

If you don't have gradle version 4.0:
`gradle wrapper` 

Run the unit tests:
`gradle test`

Build the jar:
`gradle jar`

Publish the jar to the local maven repo:
`gradle publishToMavenLocal`

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

