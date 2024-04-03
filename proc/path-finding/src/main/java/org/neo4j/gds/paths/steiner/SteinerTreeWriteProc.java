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
package org.neo4j.gds.paths.steiner;

import org.neo4j.gds.procedures.GraphDataScienceProcedures;
import org.neo4j.gds.procedures.algorithms.pathfinding.SteinerWriteResult;
import org.neo4j.gds.results.MemoryEstimateResult;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Internal;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

import java.util.Map;
import java.util.stream.Stream;

import static org.neo4j.gds.paths.steiner.Constants.STEINER_DESCRIPTION;
import static org.neo4j.gds.procedures.ProcedureConstants.MEMORY_ESTIMATION_DESCRIPTION;
import static org.neo4j.procedure.Mode.READ;
import static org.neo4j.procedure.Mode.WRITE;

public class SteinerTreeWriteProc {
    @Context
    public GraphDataScienceProcedures facade;

    @Procedure(value = "gds.steinerTree.write", mode = WRITE)
    @Description(STEINER_DESCRIPTION)
    public Stream<SteinerWriteResult> write(
        @Name(value = "graphName") String graphName,
        @Name(value = "configuration", defaultValue = "{}") Map<String, Object> configuration
    ) {
        return facade.pathFinding().steinerTreeWrite(graphName, configuration);
    }

    @Procedure(value = "gds.steinerTree.write.estimate", mode = READ)
    @Description(MEMORY_ESTIMATION_DESCRIPTION)
    public Stream<MemoryEstimateResult> estimate(
        @Name(value = "graphName") Object graphNameOrConfiguration,
        @Name(value = "configuration", defaultValue = "{}") Map<String, Object> configuration
    ) {
        return facade.pathFinding().steinerTreeWriteEstimate(graphNameOrConfiguration, configuration);
    }


    @Deprecated
    @Procedure(value = "gds.beta.steinerTree.write", mode = WRITE, deprecatedBy = "gds.steinerTree.write")
    @Description(STEINER_DESCRIPTION)
    @Internal
    public Stream<SteinerWriteResult> writeBeta(
        @Name(value = "graphName") String graphName,
        @Name(value = "configuration", defaultValue = "{}") Map<String, Object> configuration
    ) {
        facade.deprecatedProcedures().called("gds.beta.steinerTree.write");
        facade
            .log()
            .warn("Procedure `gds.beta.steinerTree.write` has been deprecated, please use `gds.steinerTree.write`.");
        return write(graphName, configuration);
    }
}
