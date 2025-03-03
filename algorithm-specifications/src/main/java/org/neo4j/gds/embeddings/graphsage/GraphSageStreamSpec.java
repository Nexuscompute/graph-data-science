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
package org.neo4j.gds.embeddings.graphsage;

import org.neo4j.gds.NullComputationResultConsumer;
import org.neo4j.gds.embeddings.graphsage.algo.GraphSage;
import org.neo4j.gds.embeddings.graphsage.algo.GraphSageAlgorithmFactory;
import org.neo4j.gds.embeddings.graphsage.algo.GraphSageResult;
import org.neo4j.gds.embeddings.graphsage.algo.GraphSageStreamConfig;
import org.neo4j.gds.executor.AlgorithmSpec;
import org.neo4j.gds.executor.ComputationResultConsumer;
import org.neo4j.gds.executor.ExecutionContext;
import org.neo4j.gds.executor.GdsCallable;
import org.neo4j.gds.executor.validation.ValidationConfiguration;
import org.neo4j.gds.procedures.algorithms.configuration.NewConfigFunction;
import org.neo4j.gds.procedures.algorithms.embeddings.GraphSageStreamResult;

import java.util.stream.Stream;

import static org.neo4j.gds.embeddings.graphsage.Constants.GRAPH_SAGE_DESCRIPTION;
import static org.neo4j.gds.executor.ExecutionMode.STREAM;

@GdsCallable(name = "gds.beta.graphSage.stream", description = GRAPH_SAGE_DESCRIPTION, executionMode = STREAM)
public class GraphSageStreamSpec implements AlgorithmSpec<GraphSage, GraphSageResult, GraphSageStreamConfig, Stream<GraphSageStreamResult>, GraphSageAlgorithmFactory<GraphSageStreamConfig>> {

    @Override
    public String name() {
        return "GraphSageStream";
    }

    @Override
    public GraphSageAlgorithmFactory<GraphSageStreamConfig> algorithmFactory(ExecutionContext executionContext) {
        return new GraphSageAlgorithmFactory<>(executionContext.modelCatalog());
    }

    @Override
    public NewConfigFunction<GraphSageStreamConfig> newConfigFunction() {
        return GraphSageStreamConfig::of;
    }

    @Override
    public ComputationResultConsumer<GraphSage, GraphSageResult, GraphSageStreamConfig, Stream<GraphSageStreamResult>> computationResultConsumer() {
        return new NullComputationResultConsumer<>();
    }

    @Override
    public ValidationConfiguration<GraphSageStreamConfig> validationConfig(ExecutionContext executionContext) {
        return new GraphSageConfigurationValidation<>(executionContext.modelCatalog());
    }
}
