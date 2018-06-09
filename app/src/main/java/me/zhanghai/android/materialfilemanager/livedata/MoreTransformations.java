/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.livedata;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import me.zhanghai.android.materialfilemanager.functional.compat.BiFunction;
import me.zhanghai.android.materialfilemanager.functional.compat.Function;
import me.zhanghai.android.materialfilemanager.functional.extension.QuadFunction;
import me.zhanghai.android.materialfilemanager.functional.extension.TriFunction;

public class MoreTransformations {

    private MoreTransformations() {}

    @MainThread
    public static <A, B, T> MediatorLiveData<T> switchMap(@NonNull LiveData<A> triggerA,
                                                          @NonNull Function<A, LiveData<T>> func) {
        MediatorLiveData<T> result = new MediatorLiveData<>();
        result.addSource(triggerA, new Observer<A>() {
            LiveData<T> mSource;
            @Override
            public void onChanged(@Nullable A a) {
                LiveData<T> newLiveData = func.apply(a);
                if (mSource == newLiveData) {
                    return;
                }
                if (mSource != null) {
                    result.removeSource(mSource);
                }
                mSource = newLiveData;
                if (mSource != null) {
                    result.addSource(mSource, result::setValue);
                }
            }
        });
        return result;
    }

    @MainThread
    public static <A, B, T> MediatorLiveData<T> switchMap(
            @NonNull LiveData<A> triggerA, @NonNull LiveData<B> triggerB,
            @NonNull BiFunction<A, B, LiveData<T>> func) {
        MediatorLiveData<T> result = new MediatorLiveData<>();
        result.addSource(triggerA, new Observer<A>() {
            LiveData<T> mSource;
            @Override
            public void onChanged(@Nullable A a) {
                LiveData<T> newLiveData = func.apply(a, triggerB.getValue());
                if (mSource == newLiveData) {
                    return;
                }
                if (mSource != null) {
                    result.removeSource(mSource);
                }
                mSource = newLiveData;
                if (mSource != null) {
                    result.addSource(mSource, result::setValue);
                }
            }
        });
        result.addSource(triggerB, new Observer<B>() {
            LiveData<T> mSource;
            @Override
            public void onChanged(@Nullable B b) {
                LiveData<T> newLiveData = func.apply(triggerA.getValue(), b);
                if (mSource == newLiveData) {
                    return;
                }
                if (mSource != null) {
                    result.removeSource(mSource);
                }
                mSource = newLiveData;
                if (mSource != null) {
                    result.addSource(mSource, result::setValue);
                }
            }
        });
        return result;
    }

    @MainThread
    public static <A, B, C, T> MediatorLiveData<T> switchMap(
            @NonNull LiveData<A> triggerA, @NonNull LiveData<B> triggerB,
            @NonNull LiveData<C> triggerC, @NonNull TriFunction<A, B, C, LiveData<T>> func) {
        MediatorLiveData<T> result = new MediatorLiveData<>();
        result.addSource(triggerA, new Observer<A>() {
            LiveData<T> mSource;
            @Override
            public void onChanged(@Nullable A a) {
                LiveData<T> newLiveData = func.apply(a, triggerB.getValue(), triggerC.getValue());
                if (mSource == newLiveData) {
                    return;
                }
                if (mSource != null) {
                    result.removeSource(mSource);
                }
                mSource = newLiveData;
                if (mSource != null) {
                    result.addSource(mSource, result::setValue);
                }
            }
        });
        result.addSource(triggerB, new Observer<B>() {
            LiveData<T> mSource;
            @Override
            public void onChanged(@Nullable B b) {
                LiveData<T> newLiveData = func.apply(triggerA.getValue(), b, triggerC.getValue());
                if (mSource == newLiveData) {
                    return;
                }
                if (mSource != null) {
                    result.removeSource(mSource);
                }
                mSource = newLiveData;
                if (mSource != null) {
                    result.addSource(mSource, result::setValue);
                }
            }
        });
        result.addSource(triggerC, new Observer<C>() {
            LiveData<T> mSource;
            @Override
            public void onChanged(@Nullable C c) {
                LiveData<T> newLiveData = func.apply(triggerA.getValue(), triggerB.getValue(), c);
                if (mSource == newLiveData) {
                    return;
                }
                if (mSource != null) {
                    result.removeSource(mSource);
                }
                mSource = newLiveData;
                if (mSource != null) {
                    result.addSource(mSource, result::setValue);
                }
            }
        });
        return result;
    }

    @MainThread
    public static <A, B, C, D, T> MediatorLiveData<T> switchMap(
            @NonNull LiveData<A> triggerA, @NonNull LiveData<B> triggerB,
            @NonNull LiveData<C> triggerC, @NonNull LiveData<D> triggerD,
            @NonNull QuadFunction<A, B, C, D, LiveData<T>> func) {
        MediatorLiveData<T> result = new MediatorLiveData<>();
        result.addSource(triggerA, new Observer<A>() {
            LiveData<T> mSource;
            @Override
            public void onChanged(@Nullable A a) {
                LiveData<T> newLiveData = func.apply(a, triggerB.getValue(), triggerC.getValue(),
                        triggerD.getValue());
                if (mSource == newLiveData) {
                    return;
                }
                if (mSource != null) {
                    result.removeSource(mSource);
                }
                mSource = newLiveData;
                if (mSource != null) {
                    result.addSource(mSource, result::setValue);
                }
            }
        });
        result.addSource(triggerB, new Observer<B>() {
            LiveData<T> mSource;
            @Override
            public void onChanged(@Nullable B b) {
                LiveData<T> newLiveData = func.apply(triggerA.getValue(), b, triggerC.getValue(),
                        triggerD.getValue());
                if (mSource == newLiveData) {
                    return;
                }
                if (mSource != null) {
                    result.removeSource(mSource);
                }
                mSource = newLiveData;
                if (mSource != null) {
                    result.addSource(mSource, result::setValue);
                }
            }
        });
        result.addSource(triggerC, new Observer<C>() {
            LiveData<T> mSource;
            @Override
            public void onChanged(@Nullable C c) {
                LiveData<T> newLiveData = func.apply(triggerA.getValue(), triggerB.getValue(), c,
                        triggerD.getValue());
                if (mSource == newLiveData) {
                    return;
                }
                if (mSource != null) {
                    result.removeSource(mSource);
                }
                mSource = newLiveData;
                if (mSource != null) {
                    result.addSource(mSource, result::setValue);
                }
            }
        });
        result.addSource(triggerD, new Observer<D>() {
            LiveData<T> mSource;
            @Override
            public void onChanged(@Nullable D d) {
                LiveData<T> newLiveData = func.apply(triggerA.getValue(), triggerB.getValue(),
                        triggerC.getValue(), d);
                if (mSource == newLiveData) {
                    return;
                }
                if (mSource != null) {
                    result.removeSource(mSource);
                }
                mSource = newLiveData;
                if (mSource != null) {
                    result.addSource(mSource, result::setValue);
                }
            }
        });
        return result;
    }
}
