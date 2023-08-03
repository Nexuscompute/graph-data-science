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
package org.neo4j.gds.facade;

import org.neo4j.gds.api.DatabaseId;
import org.neo4j.gds.api.GraphName;
import org.neo4j.gds.api.User;
import org.neo4j.gds.core.loading.GraphStoreCatalogService;
import org.neo4j.gds.core.utils.progress.tasks.ProgressTracker;
import org.neo4j.gds.wcc.WccAlgorithmFactory;
import org.neo4j.gds.wcc.WccBaseConfig;

public class AlgorithmsBusinessFacade {

    private final GraphStoreCatalogService graphStoreCatalogService;

    public AlgorithmsBusinessFacade(GraphStoreCatalogService graphStoreCatalogService) {
        this.graphStoreCatalogService = graphStoreCatalogService;
    }

    public ComputationResult<WccBaseConfig> wcc(
        String graphName,
        WccBaseConfig config,
        User user,
        DatabaseId databaseId,
        ProgressTracker progressTracker
    ) {

        var graphWithGraphStore = graphStoreCatalogService.getGraphWithGraphStore(
            GraphName.parse(graphName),
            config,
            config.relationshipWeightProperty(),
            user,
            databaseId
        );

        var graph = graphWithGraphStore.getLeft();
        var graphStore = graphWithGraphStore.getRight();

        if (graph.isEmpty()) {
            return ComputationResult.withoutAlgorithmResult(graph, config, graphStore);
        }

        var factory = new WccAlgorithmFactory<>();
        var wcc = factory.build(graph, config, progressTracker);
        var wccResult = wcc.compute();
        return ComputationResult.of(wccResult, graph, config, graphStore);
    }

}
