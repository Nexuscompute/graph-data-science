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
package org.neo4j.gds.core.write;

import org.neo4j.gds.api.IdMapping;
import org.neo4j.gds.config.ConcurrencyConfig;
import org.neo4j.gds.core.TransactionContext;
import org.neo4j.gds.core.utils.TerminationFlag;
import org.neo4j.gds.core.utils.progress.tasks.ProgressTracker;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.function.LongUnaryOperator;

public abstract class NodePropertyExporterBuilder<T extends NodePropertyExporter> {
    protected final TransactionContext transactionContext;
    protected LongUnaryOperator toOriginalId;
    protected long nodeCount;
    protected TerminationFlag terminationFlag;

    protected ExecutorService executorService;
    protected int writeConcurrency;
    protected ProgressTracker progressTracker;

    protected NodePropertyExporterBuilder(TransactionContext transactionContext) {
        this.transactionContext = Objects.requireNonNull(transactionContext);
        this.writeConcurrency = ConcurrencyConfig.DEFAULT_CONCURRENCY;
        this.progressTracker = ProgressTracker.NULL_TRACKER;
    }

    public abstract T build();

    public NodePropertyExporterBuilder<T> withIdMapping(IdMapping idMapping) {
        Objects.requireNonNull(idMapping);
        this.nodeCount = idMapping.nodeCount();
        this.toOriginalId = idMapping::toOriginalNodeId;
        return this;
    }

    public NodePropertyExporterBuilder<T> withTerminationFlag(TerminationFlag terminationFlag) {
        this.terminationFlag = terminationFlag;
        return this;
    }

    /**
     * Set the {@link ProgressTracker} to use for logging progress during export.
     *
     * If a {@link org.neo4j.gds.core.utils.progress.tasks.TaskProgressTracker} is used, caller must manage beginning and finishing the subtasks.
     * By default, an {@link org.neo4j.gds.core.utils.progress.tasks.ProgressTracker.EmptyProgressTracker} is used. That one doesn't require caller to manage any tasks.
     *
     * @param progressTracker The progress tracker to use for logging progress during export.
     * @return this
     */
    public NodePropertyExporterBuilder<T> withProgressTracker(ProgressTracker progressTracker) {
        this.progressTracker = progressTracker;
        return this;
    }

    public NodePropertyExporterBuilder<T> parallel(ExecutorService es, int writeConcurrency) {
        this.executorService = es;
        this.writeConcurrency = writeConcurrency;
        return this;
    }

}
