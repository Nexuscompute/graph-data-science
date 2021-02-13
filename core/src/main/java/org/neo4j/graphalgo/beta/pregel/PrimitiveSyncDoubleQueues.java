/*
 * Copyright (c) "Neo4j"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.graphalgo.beta.pregel;

import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;
import org.neo4j.graphalgo.core.utils.mem.MemoryEstimation;
import org.neo4j.graphalgo.core.utils.mem.MemoryEstimations;
import org.neo4j.graphalgo.core.utils.mem.MemoryUsage;
import org.neo4j.graphalgo.core.utils.paged.HugeAtomicLongArray;
import org.neo4j.graphalgo.core.utils.paged.HugeLongArray;
import org.neo4j.graphalgo.core.utils.paged.HugeObjectArray;

import java.util.Arrays;

public final class PrimitiveSyncDoubleQueues extends PrimitiveDoubleQueues {
    // toggling in-between super steps
    private HugeObjectArray<double[]> prevQueues;
    private HugeAtomicLongArray prevTails;

    public static PrimitiveSyncDoubleQueues of(long nodeCount, AllocationTracker tracker) {
        return of(nodeCount, MIN_CAPACITY, tracker);
    }

    public static PrimitiveSyncDoubleQueues of(long nodeCount, int initialCapacity, AllocationTracker tracker) {
        var currentTails = HugeAtomicLongArray.newArray(nodeCount, tracker);
        var prevTails = HugeAtomicLongArray.newArray(nodeCount, tracker);

        var currentQueues = HugeObjectArray.newArray(double[].class, nodeCount, tracker);
        var prevQueues = HugeObjectArray.newArray(double[].class, nodeCount, tracker);

        var capacity = Math.max(initialCapacity, MIN_CAPACITY);
        currentQueues.setAll(value -> new double[capacity]);
        prevQueues.setAll(value -> new double[capacity]);

        return new PrimitiveSyncDoubleQueues(currentTails, currentQueues, prevTails, prevQueues);
    }

    public static MemoryEstimation memoryEstimation() {
        return MemoryEstimations.builder(PrimitiveSyncDoubleQueues.class)
            .add("current queues", HugeObjectArray.memoryEstimation(MemoryUsage.sizeOfDoubleArray(MIN_CAPACITY)))
            .add("previous queues", HugeObjectArray.memoryEstimation(MemoryUsage.sizeOfDoubleArray(MIN_CAPACITY)))
            .perNode("current tails", HugeAtomicLongArray::memoryEstimation)
            .perNode("previous tails", HugeAtomicLongArray::memoryEstimation)
            .perNode("reference counts", HugeAtomicLongArray::memoryEstimation)
            .build();
    }

    private PrimitiveSyncDoubleQueues(
        HugeAtomicLongArray currentTails,
        HugeObjectArray<double[]> currentQueues,
        HugeAtomicLongArray prevTails,
        HugeObjectArray<double[]> prevQueues
    ) {
        super(currentTails, currentQueues);
        this.prevQueues = prevQueues;
        this.prevTails = prevTails;
    }

    void swapQueues() {
        // swap tail indexes
        var tmpTails = tails;
        this.tails = prevTails;
        this.prevTails = tmpTails;
        // swap queues
        var tmpQueues = queues;
        this.queues = prevQueues;
        this.prevQueues = tmpQueues;

        this.tails.setAll(0);
    }

    void initIterator(Iterator iterator, long nodeId) {
        iterator.init(prevQueues.get(nodeId), (int) prevTails.get(nodeId));
    }

    @Override
    void grow(long nodeId, int minCapacity) {
        var queue = queues.get(nodeId);
        var capacity = queue.length;
        // grow by 50%
        var newCapacity = capacity + (capacity >> 1);
        queues.set(nodeId, Arrays.copyOf(queue, newCapacity));
    }

    @Override
    void release() {
        super.release();
        this.prevTails.release();
        this.prevQueues.release();
    }

    static class Iterator implements Messages.MessageIterator {

        double[] queue;
        private int length;
        private int pos;

        void init(double[] queue, int length) {
            this.queue = queue;
            this.pos = 0;
            this.length = length;
        }

        @Override
        public boolean hasNext() {
            return pos < length;
        }

        @Override
        public double nextDouble() {
            return queue[pos++];
        }

        @Override
        public boolean isEmpty() {
            return length == 0;
        }
    }
}
