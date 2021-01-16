/*
 * Copyright 2018 Julien Hoarau, Robert Winkler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.resilience4j.reactor.ratelimiter.operator;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.reactor.AbstractSubscriber;
import io.vavr.control.Either;
import org.reactivestreams.Subscriber;
import reactor.core.CoreSubscriber;

/**
 * A Reactor {@link Subscriber} to wrap another subscriber in a bulkhead.
 *
 * @param <T> the value type of the upstream and downstream
 */
class RateLimiterSubscriber<T> extends AbstractSubscriber<T> {

    private final RateLimiter rateLimiter;

    protected RateLimiterSubscriber(RateLimiter rateLimiter, CoreSubscriber<? super T> downstreamSubscriber) {
        super(downstreamSubscriber);
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void hookOnNext(T t) {
        if (!isDisposed()) {
            rateLimiter.drainIfNeeded(Either.right(t));
            downstreamSubscriber.onNext(t);
        }
    }

    @Override
    public void hookOnError(Throwable t) {
        rateLimiter.drainIfNeeded(Either.left(t));
        downstreamSubscriber.onError(t);
    }

    @Override
    public void hookOnComplete() {
        downstreamSubscriber.onComplete();
    }
}
