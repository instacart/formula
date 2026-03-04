### Dependency
To simplify testing your Formulas, use `formula-test` module.
```
testImplementation 'com.instacart.formula:formula-test:{latest_version}'
```

### Basic example 
Use `test` extension to start testing your formula. Then, you can use `output` 
function to make assertions on the output or invoke event listeners.

```kotlin
CounterFormula()
    .test()
    // Making assertion on the initial output
    .output {
        assertThat(count).isEqualTo("Count: 0")
    }
    // Invoking an event listener that live on the output.
    .output { onIncrement() }
    .output { onIncrement() }
    // Making assertion on final output
    .output {
       assertThat(count).isEqualTo("Count: 2")
    }
```
Note: you can find the `CounterFormula` in the `samples` folder in the repository.


### Fakes
In some tests, we want to provide a fake implementation of a formula that can emit
an output, check data passed by the parent, or pass events back to the parent.

```kotlin
interface MyFormula : IFormula<MyFormula.Input, MyFormula.Output> {
    class MyInput()
    class MyOutput()
}

class FakeMyFormula : MyFormula {
    override val implementation = TestFormula<MyFormula.Input, MyFormula.Output>(
        initialOutput = MyFormula.Output()
    )
}
```

Once you have a fake, you can assert on input, trigger events, and emit new output.

```kotlin
val fake = FakeMyFormula()

// Assert on the data passed
fake.implementation.input {
    assertThat(this.id).isEqualTo(1)
}

// Trigger an event
fake.implementation.input {
    this.onTextChanged("new text")
}

// Emit a new output
fake.implementation.output(MyFormula.Output())
```
