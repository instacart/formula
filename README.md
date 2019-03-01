# Formula
A functional reactive framework for managing state and side effects based on RxJava. It enables building 
deterministic, composable, testable applications.

To learn more, see the our [Gettting Started Guide](docs/Getting-Started.md). Also, take a look at the samples 
folder.


## Download

Add [JitPack](https://jitpack.io) to your list of repositories:

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
```

And add the library to your list of dependencies (replace `x.y.z` with the most recent version name):

```groovy
dependencies {
    implementation 'com.github.instacart:formula:x.y.z'
    kapt 'com.github.instacart:formula-compiler:x.y.z'
    
    implementation 'com.github.instacart:formula-integration:x.y.z'
}
```
