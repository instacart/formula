### Dependency
To simplify testing your Formulas, use `formula-test` module.
```
testImplementation 'com.instacart.formula:formula-test:{latest_version}'
```

### Basic example 
Use `test` extension to start testing your formula. Then, you can use `output` 
function to make assertions on the output or invoke callbacks.

```kotlin
CounterFormula()
    .test()
    // Making assertion on the initial output
    .output {
        assertThat(count).isEqualTo("Count: 0")
    }
    // Invoking the callbacks that live on the output.
    .output { onIncrement() }
    .output { onIncrement() }
    // Making assertion on final output
    .output {
       assertThat(count).isEqualTo("Count: 2")
    }
```
Note: you can find the `CounterFormula` in the `samples` folder in the repository.


### Fakes and Mocks
In some tests, we want to provide a fake/mock implementation of formula that
can emit an output, check data passed by the parent or pass events back to the parent.

#### Mockito example
```kotlin
val testFormula = TestFormula<MyFormula.Input, MyFormula.Output>(
    initialOutput = MyFormula.Output()
)
// We use spy to ensure that it calls other real methods.
val formula = spy<MyFormula>()
whenever(formula.implementation()).thenReturn(testFormula)
```
  
#### Using interfaces and fakes in your testing
```kotlin
interface MyFormula : IFormula<MyFormula.Input, MyFormula.Output> {
    class MyInput()
    class MyOutput()
}

// Test fake implementation
class FakeMyFormula : MyFormula {
  val testFormula = TestFormula<MyFormula.Input, MyFormula.Output>(
      initialOutput = MyFormula.Output()
  )
  
  override fun implementation() = testFormula
}
```

#### Interacting with Test Formula
```kotlin
val testFormula = TestFormula()

// Assert on the data passed
testFormula.input {  
    assertThat(this.id).isEqualTo(1)
}

// Trigger a callback
testFormula.input {
    this.onTextChanged("new text")
}

// Emit a new output
testFormula.output(MyFormula.Output())
```
