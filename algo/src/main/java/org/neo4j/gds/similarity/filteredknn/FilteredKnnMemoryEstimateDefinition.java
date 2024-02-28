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
package org.neo4j.gds.similarity.filteredknn;

import org.neo4j.gds.AlgorithmMemoryEstimateDefinition;
import org.neo4j.gds.core.utils.mem.MemoryEstimation;
import org.neo4j.gds.core.utils.mem.MemoryEstimations;
import org.neo4j.gds.similarity.knn.KnnMemoryEstimateDefinition;
import org.neo4j.gds.similarity.knn.KnnMemoryEstimationParametersBuilder;

public class FilteredKnnMemoryEstimateDefinition implements AlgorithmMemoryEstimateDefinition<KnnMemoryEstimationParametersBuilder> {
    @Override
    public MemoryEstimation memoryEstimation(KnnMemoryEstimationParametersBuilder parametersSansNodeCount) {

        return MemoryEstimations
            .builder(FilteredKnn.class)
            .add(new KnnMemoryEstimateDefinition().memoryEstimation(parametersSansNodeCount)).build();

    }
}
