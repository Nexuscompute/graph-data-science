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
package org.neo4j.gds.algorithms.community;

import org.neo4j.gds.algorithms.AlgorithmComputationResult;
import org.neo4j.gds.algorithms.NodePropertyMutateResult;
import org.neo4j.gds.algorithms.community.specificfields.CommunityStatisticsSpecificFields;
import org.neo4j.gds.algorithms.community.specificfields.LocalClusteringCoefficientSpecificFields;
import org.neo4j.gds.algorithms.community.specificfields.TriangleCountSpecificFields;
import org.neo4j.gds.api.properties.nodes.NodePropertyValuesAdapter;
import org.neo4j.gds.applications.algorithms.machinery.MutateNodePropertyService;
import org.neo4j.gds.config.MutateNodePropertyConfig;
import org.neo4j.gds.core.concurrency.DefaultPool;
import org.neo4j.gds.result.CommunityStatistics;
import org.neo4j.gds.result.StatisticsComputationInstructions;
import org.neo4j.gds.triangle.LocalClusteringCoefficientMutateConfig;
import org.neo4j.gds.triangle.LocalClusteringCoefficientResult;
import org.neo4j.gds.triangle.TriangleCountMutateConfig;

import java.util.function.Supplier;

import static org.neo4j.gds.algorithms.runner.AlgorithmRunner.runWithTiming;

public class CommunityAlgorithmsMutateBusinessFacade {

    private final CommunityAlgorithmsFacade communityAlgorithmsFacade;
    private final MutateNodePropertyService mutateNodePropertyService;

    public CommunityAlgorithmsMutateBusinessFacade(
        CommunityAlgorithmsFacade communityAlgorithmsFacade,
        MutateNodePropertyService mutateNodePropertyService
    ) {
        this.mutateNodePropertyService = mutateNodePropertyService;
        this.communityAlgorithmsFacade = communityAlgorithmsFacade;
    }

    public NodePropertyMutateResult<TriangleCountSpecificFields> triangleCount(
        String graphName,
        TriangleCountMutateConfig config
    ) {

        // 1. Run the algorithm and time the execution
        var intermediateResult = runWithTiming(
            () -> communityAlgorithmsFacade.triangleCount(graphName, config)
        );
        var algorithmResult = intermediateResult.algorithmResult;

        return mutateNodeProperty(
            algorithmResult,
            config,
            (result, configuration) -> NodePropertyValuesAdapter.adapt(result.localTriangles()),
            (result) -> new TriangleCountSpecificFields(result.globalTriangles(), algorithmResult.graph().nodeCount()),
            intermediateResult.computeMilliseconds,
            () -> TriangleCountSpecificFields.EMPTY
        );
    }

    /*
        By using `ASF extends CommunityStatisticsSpecificFields` we enforce the algorithm specific fields
        to contain the statistics information.
     */
    <RESULT, CONFIG extends MutateNodePropertyConfig, ASF extends CommunityStatisticsSpecificFields> NodePropertyMutateResult<ASF> mutateNodeProperty(
        AlgorithmComputationResult<RESULT> algorithmResult,
        CONFIG configuration,
        NodePropertyValuesMapper<RESULT, CONFIG> nodePropertyValuesMapper,
        CommunityFunctionSupplier<RESULT> communityFunctionSupplier,
        SpecificFieldsWithCommunityStatisticsSupplier<RESULT, ASF> specificFieldsSupplier,
        StatisticsComputationInstructions statisticsComputationInstructions,
        long computeMilliseconds,
        Supplier<ASF> emptyASFSupplier
    ) {

        return algorithmResult.result().map(result -> {
            // 2. Construct NodePropertyValues from the algorithm result
            // 2.1 Should we measure some post-processing here?
            var nodePropertyValues = nodePropertyValuesMapper.map(
                result,
                configuration
            );

            // 3. Go and mutate the graph store
            var addNodePropertyResult = mutateNodePropertyService.mutate(
                configuration.mutateProperty(),
                nodePropertyValues,
                configuration.nodeLabelIdentifiers(algorithmResult.graphStore()),
                algorithmResult.graph(),
                algorithmResult.graphStore()
            );

            // 4. Compute result statistics
            var communityStatistics = CommunityStatistics.communityStats(
                nodePropertyValues.nodeCount(),
                communityFunctionSupplier.communityFunction(result),
                DefaultPool.INSTANCE,
                configuration.concurrency(),
                statisticsComputationInstructions
            );

            var componentCount = communityStatistics.componentCount();
            var communitySummary = CommunityStatistics.communitySummary(communityStatistics.histogram());

            var specificFields = specificFieldsSupplier.specificFields(result, componentCount, communitySummary);

            return NodePropertyMutateResult.<ASF>builder()
                .computeMillis(computeMilliseconds)
                .postProcessingMillis(communityStatistics.computeMilliseconds())
                .nodePropertiesWritten(addNodePropertyResult.nodePropertiesAdded())
                .mutateMillis(addNodePropertyResult.mutateMilliseconds())
                .configuration(configuration)
                .algorithmSpecificFields(specificFields)
                .build();
        }).orElseGet(() -> NodePropertyMutateResult.empty(emptyASFSupplier.get(), configuration));

    }

    public NodePropertyMutateResult<LocalClusteringCoefficientSpecificFields> localClusteringCoefficient(
        String graphName,
        LocalClusteringCoefficientMutateConfig configuration
    ) {
        // 1. Run the algorithm and time the execution
        var intermediateResult = runWithTiming(
            () -> communityAlgorithmsFacade.localClusteringCoefficient(graphName, configuration)
        );
        var algorithmResult = intermediateResult.algorithmResult;

        NodePropertyValuesMapper<LocalClusteringCoefficientResult, LocalClusteringCoefficientMutateConfig> mapper = ((result, config) ->
            NodePropertyValuesAdapter.adapt(result.localClusteringCoefficients()));


        return mutateNodeProperty(
            algorithmResult,
            configuration,
            mapper,
            (result) -> new LocalClusteringCoefficientSpecificFields(
                result.localClusteringCoefficients().size(),
                result.averageClusteringCoefficient()
            ),
            intermediateResult.computeMilliseconds,
            () -> LocalClusteringCoefficientSpecificFields.EMPTY
        );
    }

    <RESULT, CONFIG extends MutateNodePropertyConfig, ASF> NodePropertyMutateResult<ASF> mutateNodeProperty(
        AlgorithmComputationResult<RESULT> algorithmResult,
        CONFIG configuration,
        NodePropertyValuesMapper<RESULT, CONFIG> nodePropertyValuesMapper,
        SpecificFieldsSupplier<RESULT, ASF> specificFieldsSupplier,
        long computeMilliseconds,
        Supplier<ASF> emptyASFSupplier
    ) {
        return algorithmResult.result().map(result -> {
            // 2. Construct NodePropertyValues from the algorithm result
            // 2.1 Should we measure some post-processing here?
            var nodePropertyValues = nodePropertyValuesMapper.map(
                result,
                configuration
            );

            // 3. Go and mutate the graph store
            var addNodePropertyResult = mutateNodePropertyService.mutate(
                configuration.mutateProperty(),
                nodePropertyValues,
                configuration.nodeLabelIdentifiers(algorithmResult.graphStore()),
                algorithmResult.graph(),
                algorithmResult.graphStore()
            );

            var specificFields = specificFieldsSupplier.specificFields(result);

            return NodePropertyMutateResult.<ASF>builder()
                .computeMillis(computeMilliseconds)
                .postProcessingMillis(0)
                .nodePropertiesWritten(addNodePropertyResult.nodePropertiesAdded())
                .mutateMillis(addNodePropertyResult.mutateMilliseconds())
                .configuration(configuration)
                .algorithmSpecificFields(specificFields)
                .build();
        }).orElseGet(() -> NodePropertyMutateResult.empty(emptyASFSupplier.get(), configuration));

    }
}
