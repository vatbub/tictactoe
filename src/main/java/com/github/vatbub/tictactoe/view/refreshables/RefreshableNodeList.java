package com.github.vatbub.tictactoe.view.refreshables;

/*-
 * #%L
 * tictactoe
 * %%
 * Copyright (C) 2016 - 2017 Frederik Kammel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import org.intellij.lang.annotations.Flow;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A list for {@link Refreshable} nodes
 */
@SuppressWarnings("unused")
public class RefreshableNodeList extends ArrayList<Refreshable> {
    /**
     * Constructs an empty list with the specified initial capacity.
     *
     * @param initialCapacity the initial capacity of the list
     * @throws IllegalArgumentException if the specified initial capacity
     *                                  is negative
     */
    public RefreshableNodeList(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Constructs an empty list with an initial capacity of ten.
     */
    public RefreshableNodeList() {
    }

    /**
     * Constructs a list containing the elements of the specified
     * collection, in the order they are returned by the collection's
     * iterator.
     *
     * @param c the collection whose elements are to be placed into this list
     * @throws NullPointerException if the specified collection is null
     */
    public RefreshableNodeList(@Flow(sourceIsContainer = true, targetIsContainer = true) Collection<? extends Refreshable> c) {
        super(c);
    }

    /**
     * Calls the {@link Refreshable#refresh(double, double, double, double)} method of all elements of this list
     *
     * @param oldWindowWidth  The width of the window prior to resizing
     * @param oldWindowHeight The height of the window prior to resizing
     * @param newWindowWidth  The width of the window after to resizing
     * @param newWindowHeight The height of the window after to resizing
     */
    public void refreshAll(double oldWindowWidth, double oldWindowHeight, double newWindowWidth, double newWindowHeight) {
        for (Refreshable node:this){
            node.refresh(oldWindowWidth, oldWindowHeight, newWindowWidth, newWindowHeight);
        }
    }
}
