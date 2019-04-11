package com.instacart.formula.integration

import com.instacart.formula.fragment.FragmentContract
import com.instacart.formula.integration.internal.BaseBindingBuilder
import com.instacart.formula.integration.internal.SingleBinding
import io.reactivex.Flowable
import kotlin.reflect.KClass

/**
 * Used to create a [Binding] for [FragmentContract] keys.
 */
class FragmentBindingBuilder<ParentComponent, Component>(
    componentFactory: ComponentFactory<ParentComponent, Component>
) : BaseBindingBuilder<ParentComponent, Component, FragmentContract<*>>(componentFactory) {

    fun <RenderModel : Any, Contract : FragmentContract<RenderModel>> bind(
        contract: KClass<Contract>,
        stateManagement: (Component, Contract) -> Flowable<RenderModel>
    ) = apply {
        val binding = SingleBinding(contract.java, stateManagement)
        bind(binding as Binding<Component, FragmentContract<*>>)
    }

    fun <RenderModel : Any, Contract : FragmentContract<RenderModel>> bind(
        contract: KClass<Contract>,
        stateManagement: (Contract) -> Flowable<RenderModel>
    ) = apply {
        val stateManagementCorrected = { scope: Component, key: Contract ->
            stateManagement(key)
        }
        val binding = SingleBinding(contract.java, stateManagementCorrected)
        bind(binding as Binding<Component, FragmentContract<*>>)
    }

    fun bind(flowIntegration: FlowIntegration<Component, *>) = apply {
        bind(flowIntegration.binding())
    }
}
