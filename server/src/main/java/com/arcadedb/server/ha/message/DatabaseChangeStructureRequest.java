/*
 * Copyright 2021 Arcade Data Ltd
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.arcadedb.server.ha.message;

import com.arcadedb.database.Binary;
import com.arcadedb.database.DatabaseInternal;
import com.arcadedb.log.LogManager;
import com.arcadedb.schema.EmbeddedSchema;
import com.arcadedb.server.ArcadeDBServer;
import com.arcadedb.server.ha.HAServer;
import com.arcadedb.server.ha.ReplicationException;
import com.arcadedb.utility.FileUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class DatabaseChangeStructureRequest extends HAAbstractCommand {
  private String               databaseName;
  private String               schemaJson;
  private Map<Integer, String> filesToAdd;
  private Map<Integer, String> filesToRemove;

  public DatabaseChangeStructureRequest() {
  }

  public DatabaseChangeStructureRequest(final String databaseName, final String schemaJson, final Map<Integer, String> filesToAdd,
      final Map<Integer, String> filesToRemove) {
    this.databaseName = databaseName;
    this.schemaJson = schemaJson;
    this.filesToAdd = filesToAdd;
    this.filesToRemove = filesToRemove;
  }

  public Map<Integer, String> getFilesToAdd() {
    return filesToAdd;
  }

  public Map<Integer, String> getFilesToRemove() {
    return filesToRemove;
  }

  public String getSchemaJson() {
    return schemaJson;
  }

  @Override
  public void toStream(final Binary stream) {
    stream.putString(databaseName);
    stream.putString(schemaJson);

    stream.putUnsignedNumber(filesToAdd.size());
    for (Map.Entry<Integer, String> file : filesToAdd.entrySet()) {
      stream.putInt(file.getKey());
      stream.putByte((byte) (file.getValue() != null ? 1 : 0));
      if (file.getValue() != null)
        stream.putString(file.getValue());
    }

    stream.putUnsignedNumber(filesToRemove.size());
    for (Map.Entry<Integer, String> file : filesToRemove.entrySet()) {
      stream.putInt(file.getKey());
      stream.putByte((byte) (file.getValue() != null ? 1 : 0));
      if (file.getValue() != null)
        stream.putString(file.getValue());
    }
  }

  @Override
  public void fromStream(final ArcadeDBServer server, final Binary stream) {
    databaseName = stream.getString();
    schemaJson = stream.getString();

    filesToAdd = new HashMap<>();
    int fileCount = (int) stream.getUnsignedNumber();
    for (int i = 0; i < fileCount; ++i) {
      final int fileId = stream.getInt();
      final boolean notNull = stream.getByte() == 1;
      if (notNull)
        filesToAdd.put(fileId, stream.getString());
      else
        filesToAdd.put(fileId, null);
    }

    filesToRemove = new HashMap<>();
    fileCount = (int) stream.getUnsignedNumber();
    for (int i = 0; i < fileCount; ++i) {
      final int fileId = stream.getInt();
      final boolean notNull = stream.getByte() == 1;
      if (notNull)
        filesToRemove.put(fileId, stream.getString());
      else
        filesToRemove.put(fileId, null);
    }
  }

  @Override
  public HACommand execute(final HAServer server, final String remoteServerName, final long messageNumber) {
    try {
      final DatabaseInternal db = (DatabaseInternal) server.getServer().getDatabase(databaseName);

      final String databasePath = db.getDatabasePath();

      // ADD FILES
      for (Map.Entry<Integer, String> entry : filesToAdd.entrySet()) {
        db.getFileManager().getOrCreateFile(entry.getKey(), databasePath + "/" + entry.getValue());
      }

      // REMOVE FILES
      for (Map.Entry<Integer, String> entry : filesToRemove.entrySet()) {
        db.getFileManager().dropFile(entry.getKey());
      }

      // REPLACE SCHEMA FILE
      final File file = new File(db.getDatabasePath() + "/" + EmbeddedSchema.SCHEMA_FILE_NAME);
      FileUtils.writeContentToStream(file, schemaJson.getBytes());

      // RELOAD SCHEMA
      db.getSchema().getEmbedded().loadChanges();

      return new DatabaseChangeStructureResponse();

    } catch (Exception e) {
      LogManager.instance().log(this, Level.SEVERE, "Error on changing database structure request from the leader node", e);
      throw new ReplicationException("Error on changing database structure request from the leader node", e);
    }
  }

  @Override
  public String toString() {
    return "dbchangestructure add=" + filesToAdd + " remote=" + filesToRemove;
  }
}