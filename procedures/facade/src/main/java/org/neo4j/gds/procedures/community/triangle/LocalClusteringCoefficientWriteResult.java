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
package org.neo4j.gds.procedures.community.triangle;

import org.neo4j.gds.result.AbstractResultBuilder;

import java.util.Map;

@SuppressWarnings("unused")
public final class LocalClusteringCoefficientWriteResult extends LocalClusteringCoefficientStatsResult {

    public long writeMillis;
    public long nodePropertiesWritten;

    public LocalClusteringCoefficientWriteResult(
        double averageClusteringCoefficient,
        long nodeCount,
        long preProcessingMillis,
        long computeMillis,
        long writeMillis,
        long nodePropertiesWritten,
        Map<String, Object> configuration
    ) {
        super(
            averageClusteringCoefficient,
            nodeCount,
            preProcessingMillis,
            computeMillis,
            configuration
        );
        this.writeMillis = writeMillis;
        this.nodePropertiesWritten = nodePropertiesWritten;
    }

    public static class Builder extends AbstractResultBuilder<LocalClusteringCoefficientWriteResult> {

        double averageClusteringCoefficient = 0;

        public Builder withAverageClusteringCoefficient(double averageClusteringCoefficient) {
            this.averageClusteringCoefficient = averageClusteringCoefficient;
            return this;
        }

        @Override
        public LocalClusteringCoefficientWriteResult build() {
            return new LocalClusteringCoefficientWriteResult(
                averageClusteringCoefficient,
                nodeCount,
                preProcessingMillis,
                computeMillis,
                writeMillis,
                nodePropertiesWritten,
                config.toMap()
            );
        }
    }
}
