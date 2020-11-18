### Releasing
To release formula artifacts, make sure you have signing and configuration setup

Make sure you have `local.properties` configured
```
signingKey=/path/to/maven-key.asc
signingPassword=

SONATYPE_NEXUS_USERNAME=
SONATYPE_NEXUS_PASSWORD=
```

To upload archives
```sh
.buildscript/upload_archives.sh
```
