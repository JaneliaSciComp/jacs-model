

* changed sample query and added additional options for lines, flycoreIDs and crossBarcodes
* userID can now be used for activity and operations filter
* track delete neuron

## 3.3.5 - 03/19/2025

* fixes for SeekableStreams
* fixes and optimizations for delete neurons

* removed CircleCI configuration 

## 3.3.0 - 12/06/2024

* fixed some methods that use reflection to set an object's property to allow setting native types as well

* added direct support for S3

  * the filepath attribute may contain either a file path or an S3 URI

  * added StorageAttributes field which may contain metadata about the physical storage.

* moved JadeBasedDataLocation from the model into JADE client API -
  jacs-model-rendering still contains a FileBasedDataLocation
  implementation but that is only used for testing the RenderingVolume
  implementation

* removed HttpClientProvider and ClientProxy from the rendering package

## 2.99 - 02/29/2024
... We started to track changes only after 2.99
