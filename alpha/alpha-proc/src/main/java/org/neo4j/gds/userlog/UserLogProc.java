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
package org.neo4j.gds.userlog;

import org.neo4j.gds.BaseProc;
import org.neo4j.gds.core.utils.warnings.Warning;
import org.neo4j.gds.core.utils.warnings.WarningStore;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

import java.util.stream.Stream;

public class UserLogProc extends BaseProc {

    @Context
    public WarningStore warningStore;


    @Procedure("gds.alpha.userLog")
    @Description("Log warnings and hints for currently running tasks.")

    public Stream<UserLogResult> userLog(
        @Name(value = "jobId", defaultValue = "") String jobId
    ) {
        return getWarningsLog();
    }

    private Stream<UserLogResult> getWarningsLog() {
        return warningStore.query(username()).stream().map(UserLogResult::fromTaskStoreEntry);
    }



    public static class UserLogResult {
        public String taskName;
        public String message;

        static UserLogResult fromTaskStoreEntry(Warning warning) {

            return new UserLogResult(warning.getTaskDescription(),warning.getWarningMessage());
        }


        public UserLogResult(String taskName,String message) {
            this.taskName = taskName;
            this.message = message;
        }
    }


}

