package com.naram.weather.util.eventbus

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject

object RxEventBus {
    private val events = HashMap<Event, BehaviorSubject<Any>>()
    val mutableEvents get() = events

    fun post(eventName: Event, event: Any) {
        if(events[eventName] == null)
            events[eventName] = BehaviorSubject.create()
        events[eventName]?.onNext(event)
    }

    inline fun <reified T> listen(eventName: Event): Observable<T> {
        if (mutableEvents.containsKey(eventName).not()) {
            mutableEvents[eventName] = BehaviorSubject.create()
        }
        return mutableEvents[eventName]!!.ofType(T::class.java)
    }
}