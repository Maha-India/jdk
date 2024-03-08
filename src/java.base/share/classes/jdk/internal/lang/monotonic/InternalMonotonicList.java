/*
 * Copyright (c) 2024, Oracle and/or its affiliates. All rights reserved.
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

package jdk.internal.lang.monotonic;

import jdk.internal.vm.annotation.Stable;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.Predicate;

import static jdk.internal.lang.monotonic.MonotonicUtil.*;

public final class InternalMonotonicList<V>
        extends AbstractList<Monotonic<V>>
        implements List<Monotonic<V>> {

    @Stable
    private final Monotonic<V>[] elements;

    @SuppressWarnings({"unchecked", "rawtypes"})
    InternalMonotonicList(int size) {
        this.elements = (Monotonic<V>[]) new Monotonic[size];
    }

    @Override
    public Monotonic<V> get(int index) {
        Monotonic<V> m = elements[index];
        if (m != null) {
            return m;
        }
        m = elementVolatile(index);
        if (m != null) {
            return m;
        }
        Monotonic<V> created = Monotonic.of();
        m = caeElement(index, created);
        return m == null ? created : m;
    }

    @Override
    public int size() {
        return elements.length;
    }

    // all mutating methods throw UnsupportedOperationException
    @Override public boolean add(Monotonic<V> v) {throw uoe();}
    @Override public boolean addAll(Collection<? extends Monotonic<V>> c) {throw uoe();}
    @Override public boolean addAll(int index, Collection<? extends Monotonic<V>> c) {throw uoe();}
    @Override public void clear() {throw uoe();}
    @Override public boolean remove(Object o) {throw uoe();}
    @Override public boolean removeAll(Collection<?> c) {throw uoe();}
    @Override public boolean removeIf(Predicate<? super Monotonic<V>> filter) {throw uoe();}
    @Override public boolean retainAll(Collection<?> c) {throw uoe();}
    @Override public void sort(Comparator<? super Monotonic<V>> c) {throw uoe();}

    @Override
    public int indexOf(Object o) {
        Objects.requireNonNull(o);
        return super.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        Objects.requireNonNull(o);
        return super.lastIndexOf(o);
    }

    @Override
    public List<Monotonic<V>> reversed() {
        // Todo: Fix this
        throw new UnsupportedOperationException();
        //return ReverseOrderListView.of(this, true); // we must assume it's modifiable
    }

    @SuppressWarnings("unchecked")
    private Monotonic<V> elementVolatile(int index) {
        return (Monotonic<V>) UNSAFE.getReferenceVolatile(elements, MonotonicUtil.objectOffset(index));
    }

    @SuppressWarnings("unchecked")
    private Monotonic<V> caeElement(int index, Monotonic<V> created) {
        // Make sure no reordering of store operations
        freeze();
        return (Monotonic<V>) UNSAFE.compareAndExchangeReference(elements, MonotonicUtil.objectOffset(index), null, created);
    }

    // Factories

    public static <V> List<Monotonic<V>> of(int size) {
        return new InternalMonotonicList<>(size);
    }

    public static <V> IntFunction<V> asMemoized(int size,
                                                IntFunction<? extends V> mapper,
                                                boolean background) {
        // Todo: Fix this
        throw new UnsupportedOperationException();
    }

}
