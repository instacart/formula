## Composing outputs
Outputs are meant to be composable. You can build bigger Outputs from smaller Outputs.
```kotlin
data class CheckboxOutput(
  val text: String,
  val isChecked: Boolean,
  val onToggle: () -> Unit
)

data class NotificationSettingsOutput(
  val messagePushNotification: CheckboxOutput,
  val promotionalPushNotifications: CheckboxOutput,
  val marketingEmailNotifications: CheckboxOutput,
  val saveSettingsButton: FooterButtonOutput
)
```

You can also do the same in your Render View layer.
```kotlin
class CheckboxRenderView(root: View) : RenderView<CheckboxOutput> {
  private val checkbox: Checkbox = root.findViewById(R.id.checkbox)

  override val render: Renderer<CheckboxOutput> = Renderer { model ->
    checkbox.text = model.title
    checkbox.isChecked = model.isChecked
    checkbox.setOnCheckedListener {
      model.onToggle()
    }
  }
}

class NotificationSettingsRenderView(root: View) : RenderView<NotificationSettingsOutput> {
  private val messagePushNotification = CheckboxRenderView(root.findViewById(R.id.message_push_checkbox))
  private val promotionalPushNotifications = CheckboxRenderView(root.findViewById(R.id.promotional_push_checkbox))
  private val marketingEmailNotifications = CheckboxRenderView(root.findViewById(R.id.marketing_email_checkbox))
  private val saveButton = FooterButtonRenderView(root.findViewById(R.id.save_button))

  override val render: Renderer<NotificationSettingsOutput> = Renderer { model ->
    messagePushNotification.render(model.messagePushNotification)
    promotionalPushNotifications.render(model.promotionalPushNotifications)
    marketingEmailNotifications.render(model.marketingEmailNotifications)
    saveButton.render(model.saveSettingsButton)
  }
}
```

## Composing formulas
You can pass other formulas through the constructor
```kotlin
class MainPageFormula(
    val headerFormula: HeaderFormula,
    val listFormula: ListFormula,
    val dialogFormula: DialogFormula
) : Formula<Unit, MyState, MainOutput>
```

Use `FormulaContext.child` within `Formula.evaluate` to hook them up.
```kotlin
val listOutput = context.child(
    listFormula,
    ListInput(
        items = state.items,
        onItemSelected = context.onEvent<ItemSelected> { event ->
            // you can respond to child event
        }
    )
)
```

Here is a more complete example:
```kotlin
class MainPageFormula(
    val headerFormula: HeaderFormula,
    val listFormula: ListFormula,
    val dialogFormula: DialogFormula
) : Formula<Unit, MyState, MainOutput> {

    override fun Snapshot<Unit, MyState>.evaluate(): Evaluation<MainOutput> {
        // "context.child" returns the child's output
        val listOutput = context.child(listFormula, createListInput(state))

        val headerOutput = context.child(headerFormula, createHeaderInput(state))

        // We can make decisions using the current `state` about
        // what children to show
        val dialog = if (state.showDialog) {
            context.child(dialogFormula, Unit)
        } else {
            null
        }

        return Evaluation(
            output = MainOutput(
                header = headerOutput,
                list = listOutput,
                dialog = dialog
            )
        )
    }
}
```
