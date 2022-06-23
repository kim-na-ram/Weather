package com.naram.weather.util.eventbus

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject

object RxEventBus {
    private val events = HashMap<Event, PublishSubject<Any>>()
    val mutableEvents get() = events

    fun post(eventName: Event, event: Any) {
        events[eventName]?.onNext(event)
    }

    inline fun <reified T> listen(eventName: Event): Observable<T> {
        if(mutableEvents.containsKey(eventName).not()) {
            mutableEvents[eventName] = PublishSubject.create()
        }
        return mutableEvents[eventName]!!.ofType(T::class.java)
    }
}