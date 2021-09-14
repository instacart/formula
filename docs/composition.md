## Composing Render models
Render Models are meant to be composable. You can build bigger Render Models from smaller Render Models.
```kotlin
data class CheckboxRenderModel(
  val text: String,
  val isChecked: Boolean,
  val onToggle: () -> Unit
)

data class NotificationSettingsRenderModel(
  val messagePushNotification: CheckboxRenderModel,
  val promotionalPushNotifications: CheckboxRenderModel,
  val marketingEmailNotifications: CheckboxRenderModel,
  val saveSettingsButton: FooterButtonRenderModel
)
```

You can also do the same in your Render View layer.
```kotlin
class CheckboxRenderView(root: View) : RenderView<CheckboxRenderModel> {
  private val checkbox: Checkbox = root.findViewById(R.id.checkbox)
  
  override val render: Renderer<CheckboxRenderModel> = Renderer { model ->
    checkbox.text = model.title
    checkbox.isChecked = model.isChecked
    checkbox.setOnCheckedListener {
      model.onToggle()
    }
  } 
}

class NotificationSettingsRenderView(root: View) : RenderView<NotificationSettingsRenderModel> {
  private val messagePushNotification = CheckboxRenderView(root.findViewById(R.id.message_push_checkbox))
  private val promotionalPushNotifications = CheckboxRenderView(root.findViewById(R.id.promotional_push_checkbox))
  private val marketingEmailNotifications = CheckboxRenderView(root.findViewById(R.id.marketing_email_checkbox))
  private val saveButton = FooterButtonRenderView(root.findViewById(R.id.save_button))
  
  override val render: Renderer<NotificationSettingsRenderModel> = Renderer { model ->
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
) : Formula<> 
```

Use `FormulaContext.child` within `Formula.evaluate` to hook them up.
```kotlin
val listRenderModel = context
    .child(listFormula)
    .input {
        ListInput(
            items = state.items,
            onItemSelected = context.onEvent<ItemSelected> { event ->
                // you can respond to child event
            }
    }
```
 
Here is a more complete example:
```kotlin
class MainPageFormula(
    val headerFormula: HeaderFormula,
    val listFormula: ListFormula,
    val dialogFormula: DialogFormula
) : Formula<> {
    
    override fun evaluate(input: Unit, state: MyState, context: FormulaContext<...>): Evaluation<...> {
        // "context.child" returns a RenderModel 
        val listRenderModel = context.child(listFormula, createListInput(state))

        val headerRenderModel = context.child(headerFormula, createHeaderInput(state))

        // We can make decisions using the current `state` about 
        // what children to show
        val dialog = if (state.showDialog) {
            context.child(dialogFormula, Unit)
        } else {
            null
        }
    
        return Evaluation(
            output = MainRenderModel(
                header = headerRenderModel,
                list = listRenderModel,
                dialog = dialog
            )
        )
    }
}
```
