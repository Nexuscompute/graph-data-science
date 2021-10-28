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
package org.neo4j.gds.ml.nodemodels.pipeline;

import org.neo4j.gds.ml.pipeline.NodePropertyStep;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PipelineInfoResult {
    public final String name;
    public final List<Map<String, Object>> nodePropertySteps;
    public final List<String> featureSteps;
    public final Map<String, Object> splitConfig;
    public final Object parameterSpace;

    PipelineInfoResult(String pipelineName, NodeClassificationPipeline info) {
        this.name = pipelineName;
        this.nodePropertySteps = info
            .nodePropertySteps()
            .stream()
            .map(NodePropertyStep::toMap)
            .collect(Collectors.toList());
        this.featureSteps = info.featureProperties();
        this.splitConfig = info.splitConfig().toMap();
        this.parameterSpace = info.trainingParameterSpace();
    }
}
