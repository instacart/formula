### Releasing
As of now, we're publishing Formula releases to a private JFrog Artifactory maven repo. TODO: migrate to public Maven Central.

Make sure you have `local.properties` configured
```
ARTIFACTORY_URL=
ARTIFACTORY_USERNAME=
ARTIFACTORY_PASSWORD=
```

To upload archives
```sh
.buildscript/upload_archives.sh
```
