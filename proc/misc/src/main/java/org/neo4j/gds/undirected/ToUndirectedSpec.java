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
package org.neo4j.gds.undirected;

import org.neo4j.gds.NullComputationResultConsumer;
import org.neo4j.gds.core.loading.SingleTypeRelationships;
import org.neo4j.gds.executor.AlgorithmSpec;
import org.neo4j.gds.executor.ComputationResult;
import org.neo4j.gds.executor.ComputationResultConsumer;
import org.neo4j.gds.executor.ExecutionContext;
import org.neo4j.gds.executor.ExecutionMode;
import org.neo4j.gds.executor.GdsCallable;
import org.neo4j.gds.procedures.algorithms.configuration.NewConfigFunction;
import org.neo4j.gds.procedures.algorithms.results.StandardMutateResult;
import org.neo4j.gds.result.AbstractResultBuilder;

import java.util.Map;
import java.util.stream.Stream;

@GdsCallable(
    name = ToUndirectedSpec.CALLABLE_NAME,
    executionMode = ExecutionMode.MUTATE_RELATIONSHIP,
    description = ToUndirectedSpec.DESCRIPTION,
    aliases = {"gds.beta.graph.relationships.toUndirected"}
)
public class ToUndirectedSpec implements AlgorithmSpec<ToUndirected, SingleTypeRelationships, ToUndirectedConfig, Stream<ToUndirectedSpec.MutateResult>, ToUndirectedAlgorithmFactory> {

    static final String DESCRIPTION = "The ToUndirected procedure converts directed relationships to undirected relationships";
    static final String CALLABLE_NAME = "gds.graph.relationships.toUndirected";

    @Override
    public String name() {
        return CALLABLE_NAME;
    }

    @Override
    public ToUndirectedAlgorithmFactory algorithmFactory(ExecutionContext executionContext) {
        return new ToUndirectedAlgorithmFactory();
    }

    @Override
    public NewConfigFunction<ToUndirectedConfig> newConfigFunction() {
        return ((__, config) -> ToUndirectedConfig.of(config));
    }

    protected AbstractResultBuilder<MutateResult> resultBuilder(
        ComputationResult<ToUndirected, SingleTypeRelationships, ToUndirectedConfig> computeResult,
        ExecutionContext executionContext
    ) {
        return new MutateResult.Builder().withInputRelationships(computeResult.graph().relationshipCount());
    }

    @Override
    public ComputationResultConsumer<ToUndirected, SingleTypeRelationships, ToUndirectedConfig, Stream<MutateResult>> computationResultConsumer() {
        return new NullComputationResultConsumer<>();
    }

    public static final class MutateResult extends StandardMutateResult {
        public final long inputRelationships;
        public final long relationshipsWritten;

        private MutateResult(
            long preProcessingMillis,
            long computeMillis,
            long mutateMillis,
            long postProcessingMillis,
            long inputRelationships,
            long relationshipsWritten,
            Map<String, Object> configuration
        ) {
            super(preProcessingMillis, computeMillis, postProcessingMillis, mutateMillis, configuration);
            this.inputRelationships = inputRelationships;
            this.relationshipsWritten = relationshipsWritten;
        }

        public static class Builder extends AbstractResultBuilder<MutateResult> {

            private long inputRelationships;

            Builder withInputRelationships(long inputRelationships) {
                this.inputRelationships = inputRelationships;
                return this;
            }

            @Override
            public MutateResult build() {
                return new MutateResult(
                    preProcessingMillis,
                    computeMillis,
                    mutateMillis,
                    0,
                    inputRelationships,
                    relationshipsWritten,
                    config.toMap()
                );
            }
        }
    }
}
