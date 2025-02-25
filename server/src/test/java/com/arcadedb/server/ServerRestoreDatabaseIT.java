/*
 * Copyright © 2021-present Arcade Data Ltd (info@arcadedata.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-FileCopyrightText: 2021-present Arcade Data Ltd (info@arcadedata.com)
 * SPDX-License-Identifier: Apache-2.0
 */
package com.arcadedb.server;

import com.arcadedb.ContextConfiguration;
import com.arcadedb.GlobalConfiguration;
import com.arcadedb.database.Database;
import com.arcadedb.database.DatabaseFactory;
import com.arcadedb.query.sql.executor.ResultSet;
import com.arcadedb.utility.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;

public class ServerRestoreDatabaseIT extends BaseGraphServerTest {
  public ServerRestoreDatabaseIT() {
    FileUtils.deleteRecursively(new File("./target/config"));
    FileUtils.deleteRecursively(new File("./target/databases"));
    GlobalConfiguration.SERVER_DATABASE_DIRECTORY.setValue("./target/databases");
    GlobalConfiguration.SERVER_ROOT_PATH.setValue("./target");
  }

  @AfterEach
  @Override
  public void endTest() {
    super.endTest();
    FileUtils.deleteRecursively(new File("./target/config"));
    FileUtils.deleteRecursively(new File("./target/databases"));
  }

  @Override
  protected boolean isCreateDatabases() {
    return false;
  }

  protected void onServerConfiguration(final ContextConfiguration config) {
    final File backupFile = new File("backups/graph/backup-test.zip");
    if (backupFile.exists())
      backupFile.delete();

    final Database database = new DatabaseFactory("./target/databases/" + getDatabaseName()).create();

    database.getSchema().createDocumentType("testDoc");
    database.transaction(() -> {
      database.newDocument("testDoc").set("prop", "value").save();

      // COUNT INSIDE TX
      Assertions.assertEquals(1, database.countType("testDoc", true));
    });

    // COUNT OUTSIDE TX
    Assertions.assertEquals(1, database.countType("testDoc", true));

    Assertions.assertFalse(database.isTransactionActive());

    database.close();
    final Database database2 = new DatabaseFactory("./target/databases/" + getDatabaseName()).open();

    final ResultSet result = database2.command("sql", "backup database file://" + backupFile.getName());
    Assertions.assertTrue(result.hasNext());
    Assertions.assertEquals("OK", result.next().getProperty("result"));

    Assertions.assertTrue(backupFile.exists());
    database2.drop();

    config.setValue(GlobalConfiguration.SERVER_DEFAULT_DATABASES, "graph[elon:musk:admin]{restore:file://backups/graph/backup-test.zip}");
  }

  @Test
  public void defaultDatabases() {
    getServer(0).getSecurity().authenticate("elon", "musk", "graph");
    final Database database = getServer(0).getDatabase("graph");
    Assertions.assertEquals(1, database.countType("testDoc", true));
    FileUtils.deleteRecursively(new File(GlobalConfiguration.SERVER_DATABASE_DIRECTORY.getValueAsString() + "0/Movies"));
  }
}
