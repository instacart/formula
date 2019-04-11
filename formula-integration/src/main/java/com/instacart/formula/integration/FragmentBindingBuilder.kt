package com.instacart.formula.integration

import com.instacart.formula.fragment.FragmentContract
import com.instacart.formula.integration.internal.BaseBindingBuilder
import com.instacart.formula.integration.internal.SingleBinding
import io.reactivex.Flowable
import kotlin.reflect.KClass

/**
 * Used to create a [Binding] for [FragmentContract] keys.
 */
class FragmentBindingBuilder<Component> : BaseBindingBuilder<Component, FragmentContract<*>>() {

    fun <T: FragmentContract<RenderModel>, RenderModel : Any> bind(type : KClass<T>, integration: Integration<Component, T, RenderModel>) = apply {
        bind(SingleBinding(type.java, integration) as Binding<Component, FragmentContract<*>>)
    }

    fun <RenderModel : Any, Contract : FragmentContract<RenderModel>> bind(
        contract: KClass<Contract>,
        stateManagement: (Component, Contract) -> Flowable<RenderModel>
    ) = apply {
        val integration = object : Integration<Component, Contract, RenderModel>() {
            override fun create(component: Component, key: Contract): Flowable<RenderModel> {
                return stateManagement(component, key)
            }
        }
        bind(contract, integration)
    }

    fun <RenderModel : Any, Contract : FragmentContract<RenderModel>> bind(
        contract: KClass<Contract>,
        stateManagement: (Contract) -> Flowable<RenderModel>
    ) = apply {
        val integration = object : Integration<Component, Contract, RenderModel>() {
            override fun create(component: Component, key: Contract): Flowable<RenderModel> {
                return stateManagement(key)
            }
        }
        bind(contract, integration)
    }

    inline fun <reified T: FragmentContract<RenderModel>, RenderModel : Any> bind(
        integration: Integration<Component, T, RenderModel>
    ) = apply {
        bind(T::class, integration)
    }

    fun bind(flowIntegration: FlowIntegration<Component, *>) = apply {
        bind(flowIntegration.binding())
    }
}
