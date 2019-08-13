To simplify testing your Formulas, you can use `formula-test` module.
```
testImplementation 'com.github.instacart:formula-test:{latest_version}'
```

Testing the last render model emission
```kotlin
val subject = MyFormula().test().renderModel {
    assertThat(this.name).isEqualTo("my name")
}
```

If your Formula has children, you can replace their render model output
```kotlin
val subject = MyFormula().test {
    // Note: we are using mockito to mock ChildRenderModel, you could also manually create it.
    child(MyChildFormula::class, mock<ChildRenderModel>())
}
```

To inspect the input that was passed to the child
```kotlin
subject.childInput(MyChildFormula::class) {
    assertThat(this.property).isEqualTo("property")
}
```

You can fake child events
```kotlin
subject.childInput(MyChildFormula::class) {
    this.onEvent("fake data")
}
```
