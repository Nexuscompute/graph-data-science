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
package org.neo4j.gds.beta.indexInverse;

import org.assertj.core.api.AssertionsForInterfaceTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.neo4j.gds.NodeLabel;
import org.neo4j.gds.Orientation;
import org.neo4j.gds.RelationshipType;
import org.neo4j.gds.api.CompositeRelationshipIterator;
import org.neo4j.gds.api.GraphStore;
import org.neo4j.gds.beta.undirected.ImmutableToUndirectedConfig;
import org.neo4j.gds.core.concurrency.Pools;
import org.neo4j.gds.core.loading.CollectingMultiplePropertiesConsumer;
import org.neo4j.gds.core.loading.SingleTypeRelationshipImportResult;
import org.neo4j.gds.core.utils.progress.tasks.ProgressTracker;
import org.neo4j.gds.extension.GdlExtension;
import org.neo4j.gds.extension.GdlGraph;
import org.neo4j.gds.extension.IdFunction;
import org.neo4j.gds.extension.Inject;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@GdlExtension
class IndexInverseConfigTest {
    @GdlGraph(orientation = Orientation.NATURAL, indexInverse = true)
    private static final String DIRECTED =
        "  (a), (b), (c), (d)" +
        ", (a)-[:T1]->(b)" +
        ", (b)-[:T1]->(a)" +
        ", (b)-[:T1]->(c)" +
        ", (a)-[:T1]->(a)";

    @GdlGraph(graphNamePrefix = "undirected", orientation = Orientation.UNDIRECTED)
    private static final String UNDIRECTED = "()-[:X]->()";
    @Inject
    GraphStore graphStore;

    @Inject
    GraphStore undirectedGraphStore;

    @Test
    void failIfAlreadyIndexed() {
        var config = IndexInverseConfigImpl
            .builder()
            .relationshipType("T1")
            .mutateRelationshipType("T3")
            .build();

        assertThatThrownBy(() -> config.graphStoreValidation(
            graphStore,
            config.nodeLabelIdentifiers(graphStore),
            config.internalRelationshipTypes(graphStore)
        ))
            .hasMessageMatching("Inverse index already exists for 'T1'.");
    }

    @Test
    void failOnUndirectedInput() {
        var config = IndexInverseConfigImpl
            .builder()
            .relationshipType("X")
            .mutateRelationshipType("T2")
            .build();

        assertThatThrownBy(() -> config.graphStoreValidation(
            undirectedGraphStore,
            config.nodeLabelIdentifiers(undirectedGraphStore),
            config.internalRelationshipTypes(undirectedGraphStore)
        ))
            .hasMessage("Creating an inverse index for undirected relationships is not supported.");
    }
}
