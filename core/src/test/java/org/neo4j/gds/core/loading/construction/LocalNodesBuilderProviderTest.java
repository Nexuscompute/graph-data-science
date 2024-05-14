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
package org.neo4j.gds.core.loading.construction;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.neo4j.gds.core.concurrency.Concurrency;
import org.neo4j.gds.core.concurrency.ParallelUtil;
import org.neo4j.gds.termination.TerminationFlag;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class LocalNodesBuilderProviderTest {

    @Nested
    class ThreadLocalProviderTest {

        @Test
        void shouldAcquireTheSameInstanceForTheSameThread() throws Exception {
            var provider = LocalNodesBuilderProvider.threadLocal(() -> mock(LocalNodesBuilder.class));
            var slot = provider.acquire();
            var builder = slot.get();
            slot.release();

            for (int i = 0; i < 10; i++) {
                var s = provider.acquire();
                assertThat(s.get()).isEqualTo(builder);
                slot.release();

            }

            var otherThread = new Thread(() -> {
                for (int i = 0; i < 10; i++) {
                    var s = provider.acquire();
                    assertThat(s.get()).isNotEqualTo(builder);
                    slot.release();
                }
            });

            otherThread.start();
            otherThread.join();

            provider.close();
        }

        @Test
        void closeTheLocalBuilders() {
            var mock = mock(LocalNodesBuilder.class);
            var provider = LocalNodesBuilderProvider.threadLocal(() -> mock);
            var slot = provider.acquire();
            slot.release();
            provider.close();

            verify(mock).close();
        }
    }

    @Nested
    class PooledLocalProviderTest {

        @Test
        void shouldAcquireTheSameInstanceForTheSameThread() {
            class CountingSupplier implements java.util.function.Supplier<LocalNodesBuilder> {
                private int count = 0;

                @Override
                public LocalNodesBuilder get() {
                    count++;
                    return mock(LocalNodesBuilder.class);
                }

                private int getCount() {
                    return count;
                }
            }


            var builderSupplier = new CountingSupplier();

            var provider = LocalNodesBuilderProvider.pooled(builderSupplier, new Concurrency(2));
            ParallelUtil.parallelStreamConsume(
                IntStream.range(0, 1000),
                new Concurrency(4),
                TerminationFlag.RUNNING_TRUE,
                i -> {
                    var slot = provider.acquire();
                    slot.get();
                    slot.release();
                }
            );
            provider.close();

            assertThat(builderSupplier.getCount()).isEqualTo(2);
        }

        @Test
        void closeTheLocalBuilders() {
            var mock = mock(LocalNodesBuilder.class);
            var provider = LocalNodesBuilderProvider.pooled(() -> mock, new Concurrency(1));
            var slot = provider.acquire();
            slot.release();

            provider.close();

            verify(mock).close();
        }
    }
}
