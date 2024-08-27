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
package org.neo4j.gds.procedures.algorithms;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class CanonicalProcedureNameTest {

    @ParameterizedTest
    @ValueSource(strings={"gds.shortestpath.dijkstra",
        "gds.shortestPath.dijkstra.mutate",
        "GDS.SHORTESTPATH.DiJkStRa",
        "shortestPath.dijkstra",
        "gds.shortestPath.dijkstra"
    })
    void shouldNormalizeInput(String input) {
        assertThat(CanonicalProcedureName.parse(input).getNormalisedForm()).isEqualTo("gds.shortestpath.dijkstra");
    }

    @ParameterizedTest
    @ValueSource(strings={"gds.shortestpath.dijkstra",
        "gds.shortestPath.dijkstra.mutate",
        "GDS.SHORTESTPATH.DiJkStRa",
        "shortestPath.dijkstra",
        "gds.shortestPath.dijkstra",
        "gds.beta.foo",
        "gds.alpha.foo"
    })
    void shouldRetainRawInput(String input) {
        assertThat(CanonicalProcedureName.parse(input).getRawForm()).isEqualTo(input);
    }

    @Test
    void shouldIgnoreBetaTier() {
        assertThat(CanonicalProcedureName.parse("gds.beta.foo").getNormalisedForm()).isEqualTo("gds.foo");
    }
    @Test
    void shouldIgnoreAlphaTier() {
        assertThat(CanonicalProcedureName.parse("gds.alpha.foo").getNormalisedForm()).isEqualTo("gds.foo");
    }

}
