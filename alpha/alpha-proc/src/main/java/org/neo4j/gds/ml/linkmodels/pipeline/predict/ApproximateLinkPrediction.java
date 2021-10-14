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
package org.neo4j.gds.ml.linkmodels.pipeline.predict;

import org.neo4j.gds.NodeLabel;
import org.neo4j.gds.RelationshipType;
import org.neo4j.gds.api.Graph;
import org.neo4j.gds.api.GraphStore;
import org.neo4j.gds.core.concurrency.Pools;
import org.neo4j.gds.core.utils.mem.AllocationTracker;
import org.neo4j.gds.core.utils.progress.tasks.ProgressTracker;
import org.neo4j.gds.core.write.ImmutableRelationship;
import org.neo4j.gds.core.write.Relationship;
import org.neo4j.gds.ml.linkmodels.LinkPredictionResult;
import org.neo4j.gds.ml.linkmodels.PredictedLink;
import org.neo4j.gds.ml.linkmodels.pipeline.PipelineExecutor;
import org.neo4j.gds.ml.linkmodels.pipeline.logisticRegression.LinkLogisticRegressionData;
import org.neo4j.gds.similarity.SimilarityResult;
import org.neo4j.gds.similarity.knn.ImmutableKnnBaseConfig;
import org.neo4j.gds.similarity.knn.ImmutableKnnContext;
import org.neo4j.gds.similarity.knn.Knn;
import org.neo4j.gds.similarity.knn.KnnBaseConfig;
import org.neo4j.values.storable.Value;
import org.neo4j.values.storable.Values;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

public class ApproximateLinkPrediction extends LinkPrediction {
    private final KnnBaseConfig knnConfig;

    public ApproximateLinkPrediction(
        LinkLogisticRegressionData modelData,
        PipelineExecutor pipelineExecutor,
        Collection<NodeLabel> nodeLabels,
        Collection<RelationshipType> relationshipTypes,
        GraphStore graphStore,
        int concurrency,
        Optional<Long> randomSeed,
        int topK,
        double deltaThreshold,
        int maxIterations,
        int randomJoins,
        double sampleRate,
        ProgressTracker progressTracker
    ) {
        this(
            modelData,
            pipelineExecutor,
            nodeLabels,
            relationshipTypes,
            graphStore,
            ImmutableKnnBaseConfig
                .builder()
                // FIXME wait for API decision
                .nodeWeightProperty("DUMMY")
                .concurrency(concurrency)
                .randomSeed(randomSeed)
                .topK(topK)
                .randomJoins(randomJoins)
                .deltaThreshold(deltaThreshold)
                .maxIterations(maxIterations)
                .sampleRate(sampleRate)
                .build(),
            progressTracker
        );
    }

    public ApproximateLinkPrediction(
        LinkLogisticRegressionData modelData,
        PipelineExecutor pipelineExecutor,
        Collection<NodeLabel> nodeLabels,
        Collection<RelationshipType> relationshipTypes,
        GraphStore graphStore,
        KnnBaseConfig knnConfig,
        ProgressTracker progressTracker
    ) {
        super(modelData, pipelineExecutor, nodeLabels, relationshipTypes, graphStore, knnConfig.concurrency(), progressTracker);
        this.knnConfig = knnConfig;
    }
    @Override
    LinkPredictionResult predictLinks(
        Graph graph,
        LinkPredictionSimilarityComputer linkPredictionSimilarityComputer
    ) {
        var knnResult = new Knn(
            graph.nodeCount(),
            knnConfig,
            linkPredictionSimilarityComputer,
            ImmutableKnnContext.of(
                Pools.DEFAULT,
                AllocationTracker.empty(),
                progressTracker
            )
        ).compute();

        // Knn can always contain existing relationships. As predictions over existing relationships give false quality metrics we need to exclude them
        Stream<SimilarityResult> predictions = knnResult
            .streamSimilarityResult()
            .filter(i -> !graph.exists(i.sourceNodeId(), i.targetNodeId()));

        return new Result(predictions);
    }

    static class Result implements LinkPredictionResult {
        private final Stream<SimilarityResult> predictions;

        // reverse entries
        // existing relationships

        Result(Stream<SimilarityResult> predictions) {this.predictions = predictions;}

        @Override
        public Stream<PredictedLink> stream() {
            return predictions.map(i -> PredictedLink.of(i.sourceNodeId(), i.targetNodeId(), i.similarity));
        }

        @Override
        public Stream<Relationship> relationshipStream() {
            return predictions.map(i -> ImmutableRelationship.of(i.node1, i.node2, new Value[]{Values.doubleValue(i.similarity)}));
        }
    }
}
