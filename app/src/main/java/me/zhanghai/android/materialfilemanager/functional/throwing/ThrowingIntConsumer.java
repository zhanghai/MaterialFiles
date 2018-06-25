/*
 * Copyright (c) 2010, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package me.zhanghai.android.materialfilemanager.functional.throwing;

import java.util.Objects;

import me.zhanghai.android.materialfilemanager.functional.FunctionalException;
import me.zhanghai.android.materialfilemanager.functional.compat.IntConsumer;

/**
 * Represents an operation that accepts a single {@code int}-valued argument and
 * returns no result.  This is the primitive type specialization of
 * {@link ThrowingConsumer} for {@code int}.  Unlike most other functional interfaces,
 * {@code ThrowingIntConsumer} is expected to operate via side-effects.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #accept(int)}.
 *
 * @see ThrowingConsumer
 * @since 1.8
 */
@FunctionalInterface
public interface ThrowingIntConsumer extends IntConsumer {

    /**
     * Performs this operation on the given argument.
     *
     * @param value the input argument
     */
    void acceptThrows(int value) throws Exception;

    /**
     * Performs this operation on the given argument.
     *
     * @param value the input argument
     */
    default void accept(int value) {
        try {
            acceptThrows(value);
        } catch (Exception e) {
            throw new FunctionalException(e);
        }
    }

    /**
     * Returns a composed {@code ThrowingIntConsumer} that performs, in sequence, this
     * operation followed by the {@code after} operation. If performing either
     * operation throws an exception, it is relayed to the caller of the
     * composed operation.  If performing this operation throws an exception,
     * the {@code after} operation will not be performed.
     *
     * @param after the operation to perform after this operation
     * @return a composed {@code ThrowingIntConsumer} that performs in sequence this
     * operation followed by the {@code after} operation
     * @throws NullPointerException if {@code after} is null
     */
    default ThrowingIntConsumer andThen(ThrowingIntConsumer after) {
        Objects.requireNonNull(after);
        return (int t) -> { acceptThrows(t); after.acceptThrows(t); };
    }
}
