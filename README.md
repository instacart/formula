# Formula
A functional reactive framework for managing state and side effects based on RxJava. It enables building 
deterministic, composable, testable applications.

To learn more, see our [Gettting Started Guide](docs/Getting-Started.md). Also, take a look at the [samples](samples) 
folder.

## Integration
The integration module provides declarative API to connect reactive state management to Android Fragments. 
This module has been designed for gradual adoption. You can use as much or as little of it as you like.

To learn more, see our [Integration Guide](docs/Integration.md).

## Download

Add [JitPack](https://jitpack.io) to your list of repositories:

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
```

And add the library to your list of dependencies:

```groovy
dependencies {
    implementation 'com.github.instacart:formula:0.2.1'
    kapt 'com.github.instacart:formula-compiler:0.2.1'
    
    implementation 'com.github.instacart:formula-integration:0.2.1'
}
```

# License

```
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
