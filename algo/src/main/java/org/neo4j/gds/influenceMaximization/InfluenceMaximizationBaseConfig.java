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
package org.neo4j.gds.influenceMaximization;

import org.neo4j.gds.annotation.Configuration;
import org.neo4j.gds.config.AlgoBaseConfig;
import org.neo4j.gds.config.RandomSeedConfig;

public interface InfluenceMaximizationBaseConfig extends AlgoBaseConfig, RandomSeedConfig {

    @Configuration.IntegerRange(min = 1)
    int seedSetSize();

    @Configuration.DoubleRange(min = 0.01, max = 1)
    default double propagationProbability() {
        return 0.1;
    }

    @Configuration.IntegerRange(min = 1)
    default int monteCarloSimulations() {
        return 100;
    }

    @Configuration.Ignore
    default CELFParameters toParameters() {
        return new CELFParameters(
            seedSetSize(),
            propagationProbability(),
            monteCarloSimulations(),
            typedConcurrency(),
            randomSeed().orElse(0L),
            CELFAlgorithmFactory.DEFAULT_BATCH_SIZE
        );
    }
}
