package com.jheng.bay.util

import java.util.concurrent.Executors
import kotlin.reflect.KClass

interface Event {

    data class ADVISOR_DOC_CHANGED(
            val advisor_id: Int
    ) : Event

    data class PROJECT_DOC_CHANGED(
            val project_id: Int
    ) : Event

    data class TASK_REVENUE_CHANGE(
            val task_id: Int
    ) : Event

    data class PROJECT_CREATED(
            val project_id: Int
    ) : Event

    data class CONTRACT_TRANSFER_HAPPEND(
            val current_contract_id: Int,
            val successor_contract_id: Int
    ) : Event

    data class CONTRACT_TURN_EXPIRED(
            val contract_id: Int
    ) : Event

    data class CONTRACT_TURN_EFFECTIVE(
            val contract_id: Int
    ) : Event
}

//====================================================================================================================*/

//region implemention
interface EventListener {
    fun onEvent(event: Event)
}

enum class CallbackOption {
    Sync,
    Async
}

/**
 *  For decoupling cross-module method invocations.
 *
 *  @see `Observer pattern`
 */
object InvokeUtil {
    private val executorService = Executors.newFixedThreadPool(3)!!
    private val map: MutableMap<String, MutableSet<Pair<EventListener, CallbackOption>>> = mutableMapOf()

    @Synchronized
    fun register(
            self: EventListener,
            interests: Map<KClass<out Event>, CallbackOption>
    ) {
        for ((eventClazz, option) in interests) {
            if (map[eventClazz.simpleName] != null) {
                map[eventClazz.simpleName]!!.add(self to option)
            } else {
                map[eventClazz.simpleName!!] = mutableSetOf(self to option)
            }
        }
    }

    fun trigger(event: Event) {
        for ((listener, option) in map[event::class.simpleName] ?: return) {
            when (option) {
                CallbackOption.Sync -> listener.onEvent(event)
                CallbackOption.Async -> executorService.submit { listener.onEvent(event) }
            }
        }
    }
}
//endregion
