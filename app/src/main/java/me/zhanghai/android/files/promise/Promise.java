/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.promise;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.zhanghai.android.files.functional.Functional;
import me.zhanghai.android.files.functional.IterableCompat;
import me.zhanghai.android.files.functional.compat.Consumer;

@SuppressWarnings({ "unused", "WeakerAccess" })
public class Promise<T> {

    @NonNull
    private final CountDownLatch mLatch;

    @NonNull
    private final Object mLock = new Object();

    @NonNull
    private Status mStatus = Status.PENDING;
    private T mValue;
    private Exception mReason;

    @NonNull
    private final List<Consumer<T>> mOnFulfilleds = new ArrayList<>();
    @NonNull
    private final List<Consumer<Exception>> mOnRejecteds = new ArrayList<>();

    private final Settler<T> mSettler = new Settler<T>() {
        @Override
        public void resolve(@Nullable T value) {
            fulfill(value);
        }
        @Override
        public void resolvePromise(@Nullable Promise<T> promise) {
            resolvePromise_(promise);
        }
        @Override
        public void reject(@Nullable Exception reason) {
            reject_(reason);
        }
    };

    @NonNull
    public static <T> Promise<T> resolve(@Nullable T value) {
        return new Promise<>(value);
    }

    @NonNull
    public static <T> Promise<T> resolvePromise(@Nullable Promise<T> promise) {
        if (promise == null) {
            return resolve(null);
        }
        return promise;
    }

    @NonNull
    public static <T> Promise<T> reject(@Nullable Exception reason) {
        return new Promise<>(reason);
    }

    @NonNull
    public static <T> Promise<T> race(@NonNull Iterable<Promise<T>> promises) {
        return new Promise<>(settler -> IterableCompat.forEach(promises, promise ->
                promise.then(settlerToOnFulfilled(settler), settlerToOnRejected(settler))));
    }

    @NonNull
    public static <T> Promise<List<T>> all(@NonNull Iterable<Promise<T>> promises) {
        return new Promise<>(settler -> {
            List<Promise<T>> pendingPromises = new ArrayList<>();
            IterableCompat.forEach(promises, pendingPromises::add);
            int promiseCount = pendingPromises.size();
            List<T> values = new ArrayList<>(Collections.nCopies(promiseCount, null));
            AtomicInteger atomicPendingCount = new AtomicInteger(promiseCount);
            Functional.forEach(pendingPromises, (promise, index) -> promise.then(value -> {
                values.set(index, value);
                int pendingCount = atomicPendingCount.decrementAndGet();
                if (pendingCount == 0) {
                    settler.resolve(values);
                }
                return null;
            }, settlerToOnRejected(settler)));
        });
    }

    public Promise(@NonNull Executable<T> executable) {
        Objects.requireNonNull(executable);
        mLatch = new CountDownLatch(1);
        execute(executable);
    }

    private Promise(@Nullable T value) {
        mLatch = new CountDownLatch(0);
        mStatus = Status.FULFILLED;
        mValue = value;
    }

    private Promise(@Nullable Exception reason) {
        mLatch = new CountDownLatch(0);
        mStatus = Status.REJECTED;
        mReason = reason;
    }

    private void execute(@NonNull Executable<T> executable) {
        try {
            executable.execute(mSettler);
        } catch (Exception e) {
            reject_(e);
        }
    }

    private void fulfill(@Nullable T value) {
        synchronized (mLock) {
            if (mStatus != Status.PENDING) {
                return;
            }
            mValue = value;
            mStatus = Status.FULFILLED;
            mLatch.countDown();
            IterableCompat.forEach(mOnFulfilleds, onFulfilled -> onFulfilled.accept(value));
        }
    }

    private void resolvePromise_(@Nullable Promise<T> promise) {
        if (promise == null) {
            fulfill(null);
            return;
        }
        synchronized (mLock) {
            if (mStatus != Status.PENDING) {
                return;
            }
            execute(promiseToExecutable(promise));
        }
    }

    @NonNull
    private static <T> Executable<T> promiseToExecutable(@NonNull Promise<T> promise) {
        return settler -> promise.then(settlerToOnFulfilled(settler),
                settlerToOnRejected(settler));
    }

    @NonNull
    private static <T, R> OnFulfilled<T, R> settlerToOnFulfilled(@NonNull Settler<T> settler) {
        return value -> {
            settler.resolve(value);
            return null;
        };
    }

    @NonNull
    private static <T, R> OnRejected<R> settlerToOnRejected(@NonNull Settler<T> settler) {
        return reason -> {
            settler.reject(reason);
            return null;
        };
    }

    private void reject_(@Nullable Exception reason) {
        synchronized (mLock) {
            if (mStatus != Status.PENDING) {
                return;
            }
            mReason = reason;
            mStatus = Status.REJECTED;
            mLatch.countDown();
            IterableCompat.forEach(mOnRejecteds, onRejected -> onRejected.accept(reason));
        }
    }

    // TODO: Ensure we are always asynchronous?
    private void done(@Nullable Consumer<T> onFulfilled, @Nullable Consumer<Exception> onRejected) {
        synchronized (mLock) {
            switch (mStatus) {
                case PENDING:
                    if (onFulfilled != null) {
                        mOnFulfilleds.add(onFulfilled);
                    }
                    if (onRejected != null) {
                        mOnRejecteds.add(onRejected);
                    }
                    break;
                case FULFILLED:
                    if (onFulfilled != null) {
                        onFulfilled.accept(mValue);
                    }
                    break;
                case REJECTED:
                    if (onRejected != null) {
                        onRejected.accept(mReason);
                    }
                    break;
                default:
                    throw new AssertionError(mStatus);
            }
        }
    }

    @NonNull
    public <R> Promise<R> then(@NonNull OnFulfilled<T, R> onFulfilled,
                               @Nullable OnRejected<R> onRejected) {
        Objects.requireNonNull(onFulfilled);
        return new Promise<>(settler -> done(value -> {
            try {
                settler.resolve(onFulfilled.onFulfilled(value));
            } catch (Exception e) {
                settler.reject(e);
            }
        }, reason -> {
            if (onRejected != null) {
                try {
                    settler.resolve(onRejected.onRejected(reason));
                } catch (Exception e) {
                    settler.reject(e);
                }
            } else {
                settler.reject(reason);
            }
        }));
    }

    @NonNull
    public <R> Promise<R> thenFulfilledPromise(@NonNull OnFulfilled<T, Promise<R>> onFulfilled,
                                               @Nullable OnRejected<R> onRejected) {
        Objects.requireNonNull(onFulfilled);
        return new Promise<>(settler -> done(value -> {
            try {
                settler.resolvePromise(onFulfilled.onFulfilled(value));
            } catch (Exception e) {
                settler.reject(e);
            }
        }, reason -> {
            if (onRejected != null) {
                try {
                    settler.resolve(onRejected.onRejected(reason));
                } catch (Exception e) {
                    settler.reject(e);
                }
            } else {
                settler.reject(reason);
            }
        }));
    }

    @NonNull
    public <R> Promise<R> thenRejectedPromise(@NonNull OnFulfilled<T, R> onFulfilled,
                                              @Nullable OnRejected<Promise<R>> onRejected) {
        Objects.requireNonNull(onFulfilled);
        return new Promise<>(settler -> done(value -> {
            try {
                settler.resolve(onFulfilled.onFulfilled(value));
            } catch (Exception e) {
                settler.reject(e);
            }
        }, reason -> {
            if (onRejected != null) {
                try {
                    settler.resolvePromise(onRejected.onRejected(reason));
                } catch (Exception e) {
                    settler.reject(e);
                }
            } else {
                settler.reject(reason);
            }
        }));
    }

    @NonNull
    public <R> Promise<R> thenSettledPromise(@NonNull OnFulfilled<T, Promise<R>> onFulfilled,
                                             @Nullable OnRejected<Promise<R>> onRejected) {
        Objects.requireNonNull(onFulfilled);
        return new Promise<>(settler -> done(value -> {
            try {
                settler.resolvePromise(onFulfilled.onFulfilled(value));
            } catch (Exception e) {
                settler.reject(e);
            }
        }, reason -> {
            if (onRejected != null) {
                try {
                    settler.resolvePromise(onRejected.onRejected(reason));
                } catch (Exception e) {
                    settler.reject(e);
                }
            } else {
                settler.reject(reason);
            }
        }));
    }

    @NonNull
    public <R> Promise<R> then(@NonNull OnFulfilled<T, R> onFulfilled) {
        return then(onFulfilled, null);
    }

    @NonNull
    public <R> Promise<R> thenPromise(@NonNull OnFulfilled<T, Promise<R>> onFulfilled) {
        return thenFulfilledPromise(onFulfilled, null);
    }

    @NonNull
    public Promise<T> catch_(@NonNull OnRejected<T> onRejected) {
        Objects.requireNonNull(onRejected);
        return then(value -> value, onRejected);
    }

    @NonNull
    public Promise<T> catchPromise(@NonNull OnRejected<Promise<T>> onRejected) {
        Objects.requireNonNull(onRejected);
        return thenRejectedPromise(value -> value, onRejected);
    }

    public <R> Promise<T> finally_(@NonNull OnFinally<R> onFinally) {
        Objects.requireNonNull(onFinally);
        return then(value -> {
                    onFinally.onFinally();
                    return value;
                },
                reason -> {
                    onFinally.onFinally();
                    throw reason;
                });
    }

    public <R> Promise<T> finallyPromise(@NonNull OnFinally<Promise<R>> onFinally) {
        Objects.requireNonNull(onFinally);
        return thenSettledPromise(value -> Promise.resolvePromise(onFinally.onFinally())
                        .then(value2 -> value),
                reason -> Promise.resolvePromise(onFinally.onFinally())
                        .then(value2 -> { throw reason; }));
    }

    public T await() throws ExecutionException, InterruptedException {
        mLatch.await();
        synchronized (mLock) {
            switch (mStatus) {
                case FULFILLED:
                    return mValue;
                case REJECTED:
                    throw new ExecutionException(mReason);
                default:
                    throw new AssertionError(mStatus);
            }
        }
    }

    public T await(long timeout, @NonNull TimeUnit unit) throws ExecutionException,
            InterruptedException, TimeoutException {
        if (!mLatch.await(timeout, unit)) {
            throw new TimeoutException();
        }
        synchronized (mLock) {
            switch (mStatus) {
                case FULFILLED:
                    return mValue;
                case REJECTED:
                    throw new ExecutionException(mReason);
                default:
                    throw new AssertionError(mStatus);
            }
        }
    }

    private enum Status {
        PENDING,
        FULFILLED,
        REJECTED
    }
}
