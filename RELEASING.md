### Releasing

Formula supports publishing to two targets:
- **JFrog Artifactory** - Private maven repository (automatic on VERSION change)
- **Maven Central** - Public maven repository

### Automatic Publishing

When the `VERSION` file is updated on the `master` branch, the GitHub Actions workflow automatically publishes to Artifactory and creates a GitHub Release.

### Local Development

#### Artifactory
```properties
# local.properties
ARTIFACTORY_URL=
ARTIFACTORY_USERNAME=
ARTIFACTORY_PASSWORD=
```

```sh
./gradlew uploadArchives -PartifactoryRelease
```

#### Maven Central
```properties
# local.properties
signingKey=path/to/signing-key.asc
signingPassword=your-signing-password
SONATYPE_NEXUS_USERNAME=
SONATYPE_NEXUS_PASSWORD=
```

```sh
./gradlew uploadArchives -PmavenCentralRelease
```

### CI/CD Secrets Required

#### Artifactory
- `ARTIFACTORY_URL`
- `ARTIFACTORY_USERNAME`
- `ARTIFACTORY_PASSWORD`

#### Maven Central
- `SONATYPE_NEXUS_USERNAME`
- `SONATYPE_NEXUS_PASSWORD`
- `SIGNING_KEY` - GPG private key content
- `SIGNING_PASSWORD` - GPG key passphrase
