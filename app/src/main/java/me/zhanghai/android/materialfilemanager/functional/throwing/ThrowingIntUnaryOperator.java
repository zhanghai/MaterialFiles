/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
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
import me.zhanghai.android.materialfilemanager.functional.compat.IntUnaryOperator;

/**
 * Represents an operation on a single {@code int}-valued operand that produces
 * an {@code int}-valued result.  This is the primitive type specialization of
 * {@link ThrowingUnaryOperator} for {@code int}.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #applyAsInt(int)}.
 *
 * @see ThrowingUnaryOperator
 * @since 1.8
 */
@FunctionalInterface
public interface ThrowingIntUnaryOperator extends IntUnaryOperator {

    /**
     * Applies this operator to the given operand.
     *
     * @param operand the operand
     * @return the operator result
     */
    int applyAsIntThrows(int operand) throws Exception;

    /**
     * Applies this operator to the given operand.
     *
     * @param operand the operand
     * @return the operator result
     */
    default int applyAsInt(int operand) {
        try {
            return applyAsIntThrows(operand);
        } catch (Exception e) {
            throw new FunctionalException(e);
        }
    }

    /**
     * Returns a composed operator that first applies the {@code before}
     * operator to its input, and then applies this operator to the result.
     * If evaluation of either operator throws an exception, it is relayed to
     * the caller of the composed operator.
     *
     * @param before the operator to apply before this operator is applied
     * @return a composed operator that first applies the {@code before}
     * operator and then applies this operator
     * @throws NullPointerException if before is null
     *
     * @see #andThen(ThrowingIntUnaryOperator)
     */
    default ThrowingIntUnaryOperator compose(ThrowingIntUnaryOperator before) {
        Objects.requireNonNull(before);
        return (int v) -> applyAsIntThrows(before.applyAsIntThrows(v));
    }

    /**
     * Returns a composed operator that first applies this operator to
     * its input, and then applies the {@code after} operator to the result.
     * If evaluation of either operator throws an exception, it is relayed to
     * the caller of the composed operator.
     *
     * @param after the operator to apply after this operator is applied
     * @return a composed operator that first applies this operator and then
     * applies the {@code after} operator
     * @throws NullPointerException if after is null
     *
     * @see #compose(ThrowingIntUnaryOperator)
     */
    default ThrowingIntUnaryOperator andThen(ThrowingIntUnaryOperator after) {
        Objects.requireNonNull(after);
        return (int t) -> after.applyAsIntThrows(applyAsIntThrows(t));
    }

    /**
     * Returns a unary operator that always returns its input argument.
     *
     * @return a unary operator that always returns its input argument
     */
    static ThrowingIntUnaryOperator identity() {
        return t -> t;
    }
}
