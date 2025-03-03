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
package org.neo4j.gds.projection;

import org.neo4j.gds.transaction.TransactionContext;

import static org.neo4j.gds.projection.Constants.UNKNOWN_ROW_COUNT;

public interface QueryEstimator {

    int estimateRows(String query);

    static QueryEstimator fromTransaction(TransactionContext transaction) {
        return new TxQueryEstimator(transaction);
    }

    static QueryEstimator empty() {
        return __ -> UNKNOWN_ROW_COUNT;
    }
}

final class TxQueryEstimator implements QueryEstimator {
    private final TransactionContext transactionContext;

    TxQueryEstimator(TransactionContext transactionContext) {
        this.transactionContext = transactionContext;
    }

    @Override
    public int estimateRows(String query) {
        return QueryRowEstimationUtil.estimatedRows(transactionContext, query);
    }
}
