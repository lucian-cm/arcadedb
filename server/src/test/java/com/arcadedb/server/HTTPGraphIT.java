package com.arcadedb.server;

import com.arcadedb.log.LogManager;
import com.arcadedb.serializer.json.JSONArray;
import com.arcadedb.serializer.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;

public class HTTPGraphIT extends BaseGraphServerTest {
  @Test
  public void checkAuthenticationError() throws Exception {
    testEachServer((serverIndex) -> {
      final HttpURLConnection connection = (HttpURLConnection) new URL(
          "http://127.0.0.1:248" + serverIndex + "/api/v1/query/graph/sql/select%20from%20V1%20limit%201").openConnection();

      connection.setRequestMethod("GET");
      connection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString("root:wrong".getBytes()));
      try {
        connection.connect();
        readResponse(connection);
        Assertions.fail("Authentication was bypassed!");
      } catch (final IOException e) {
        Assertions.assertTrue(e.toString().contains("403"));
      } finally {
        connection.disconnect();
      }
    });
  }

  @Test
  public void checkNoAuthentication() throws Exception {
    testEachServer((serverIndex) -> {
      final HttpURLConnection connection = (HttpURLConnection) new URL(
          "http://127.0.0.1:248" + serverIndex + "/api/v1/query/graph/sql/select%20from%20V1%20limit%201").openConnection();

      connection.setRequestMethod("GET");
      try {
        connection.connect();
        readResponse(connection);
        Assertions.fail("Authentication was bypassed!");
      } catch (final IOException e) {
        Assertions.assertTrue(e.toString().contains("403"));
      } finally {
        connection.disconnect();
      }
    });
  }

  @Test
  public void checkContent() throws Exception {
    testEachServer((serverIndex) -> {
      final HttpURLConnection connection = (HttpURLConnection) new URL("http://127.0.0.1:248" + serverIndex + "/").openConnection();
      connection.setRequestMethod("GET");
      connection.setRequestProperty("Authorization",
          "Basic " + Base64.getEncoder().encodeToString(("root:" + BaseGraphServerTest.DEFAULT_PASSWORD_FOR_TESTS).getBytes()));

      try {
        final String response = readResponse(connection);
        LogManager.instance().log(this, Level.FINE, "Response: ", null, response);
        Assertions.assertEquals(200, connection.getResponseCode());
        Assertions.assertEquals("OK", connection.getResponseMessage());
        Assertions.assertTrue(response.length() > 1000);

      } finally {
        connection.disconnect();
      }
    });
  }

  @Test
  public void checkQueryInGet() throws Exception {
    testEachServer((serverIndex) -> {
      final HttpURLConnection connection = (HttpURLConnection) new URL(
          "http://127.0.0.1:248" + serverIndex + "/api/v1/query/graph/sql/select%20from%20V1%20limit%201").openConnection();

      connection.setRequestMethod("GET");
      connection.setRequestProperty("Authorization",
          "Basic " + Base64.getEncoder().encodeToString(("root:" + BaseGraphServerTest.DEFAULT_PASSWORD_FOR_TESTS).getBytes()));
      connection.connect();

      try {
        final String response = readResponse(connection);
        LogManager.instance().log(this, Level.FINE, "Response: ", null, response);
        Assertions.assertEquals(200, connection.getResponseCode());
        Assertions.assertEquals("OK", connection.getResponseMessage());
        Assertions.assertTrue(response.contains("V1"));

      } finally {
        connection.disconnect();
      }
    });
  }

  @Test
  public void checkQueryInPost() throws Exception {
    testEachServer((serverIndex) -> {
      final HttpURLConnection connection = (HttpURLConnection) new URL("http://127.0.0.1:248" + serverIndex + "/api/v1/query/graph").openConnection();

      connection.setRequestMethod("POST");
      connection.setRequestProperty("Authorization",
          "Basic " + Base64.getEncoder().encodeToString(("root:" + BaseGraphServerTest.DEFAULT_PASSWORD_FOR_TESTS).getBytes()));
      formatPayload(connection, "sql", "select from V1 limit 1", null, new HashMap<>());
      connection.connect();

      try {
        final String response = readResponse(connection);
        LogManager.instance().log(this, Level.FINE, "Response: ", null, response);
        Assertions.assertEquals(200, connection.getResponseCode());
        Assertions.assertEquals("OK", connection.getResponseMessage());
        Assertions.assertTrue(response.contains("V1"));
      } finally {
        connection.disconnect();
      }
    });
  }

  @Test
  public void checkCommand() throws Exception {
    testEachServer((serverIndex) -> {
      final HttpURLConnection connection = (HttpURLConnection) new URL("http://127.0.0.1:248" + serverIndex + "/api/v1/command/graph").openConnection();

      connection.setRequestMethod("POST");
      connection.setRequestProperty("Authorization",
          "Basic " + Base64.getEncoder().encodeToString(("root:" + BaseGraphServerTest.DEFAULT_PASSWORD_FOR_TESTS).getBytes()));
      formatPayload(connection, "sql", "select from V1 limit 1", null, new HashMap<>());
      connection.connect();

      try {
        final String response = readResponse(connection);
        LogManager.instance().log(this, Level.FINE, "Response: ", null, response);
        Assertions.assertEquals(200, connection.getResponseCode());
        Assertions.assertEquals("OK", connection.getResponseMessage());
        Assertions.assertTrue(response.contains("V1"));
      } finally {
        connection.disconnect();
      }
    });
  }

  @Test
  public void checkCommandLoadByRIDWithParameters() throws Exception {
    testEachServer((serverIndex) -> {
      final HttpURLConnection connection = (HttpURLConnection) new URL("http://127.0.0.1:248" + serverIndex + "/api/v1/command/graph").openConnection();

      connection.setRequestMethod("POST");
      connection.setRequestProperty("Authorization",
          "Basic " + Base64.getEncoder().encodeToString(("root:" + BaseGraphServerTest.DEFAULT_PASSWORD_FOR_TESTS).getBytes()));
      formatPayload(connection, "sql", "SELECT FROM :rid", null, Collections.singletonMap("rid", "#1:0"));
      connection.connect();

      try {
        final String response = readResponse(connection);
        LogManager.instance().log(this, Level.FINE, "Response: ", null, response);
        Assertions.assertEquals(200, connection.getResponseCode());
        Assertions.assertEquals("OK", connection.getResponseMessage());
        Assertions.assertTrue(response.contains("V1"));
      } finally {
        connection.disconnect();
      }
    });
  }

  @Test
  public void checkCommandLoadByRIDInWhereWithParameters() throws Exception {
    testEachServer((serverIndex) -> {
      final HttpURLConnection connection = (HttpURLConnection) new URL("http://127.0.0.1:248" + serverIndex + "/api/v1/command/graph").openConnection();

      connection.setRequestMethod("POST");
      connection.setRequestProperty("Authorization",
          "Basic " + Base64.getEncoder().encodeToString(("root:" + BaseGraphServerTest.DEFAULT_PASSWORD_FOR_TESTS).getBytes()));
      formatPayload(connection, "sql", "SELECT FROM " + VERTEX1_TYPE_NAME + " where @rid = :rid", null, Collections.singletonMap("rid", "#1:0"));
      connection.connect();

      try {
        final String response = readResponse(connection);
        LogManager.instance().log(this, Level.FINE, "Response: ", null, response);
        Assertions.assertEquals(200, connection.getResponseCode());
        Assertions.assertEquals("OK", connection.getResponseMessage());
        Assertions.assertTrue(response.contains("V1"));
      } finally {
        connection.disconnect();
      }
    });
  }

  /**
   * Issue https://github.com/ArcadeData/arcadedb/discussions/468
   */
  @Test
  public void checkCommandLoadByRIDIn() throws Exception {
    testEachServer((serverIndex) -> {
      final HttpURLConnection connection = (HttpURLConnection) new URL("http://127.0.0.1:248" + serverIndex + "/api/v1/command/graph").openConnection();

      connection.setRequestMethod("POST");
      connection.setRequestProperty("Authorization",
          "Basic " + Base64.getEncoder().encodeToString(("root:" + BaseGraphServerTest.DEFAULT_PASSWORD_FOR_TESTS).getBytes()));
      formatPayload(connection, "sql", "SELECT FROM " + VERTEX1_TYPE_NAME + " where @rid in (#1:0)", null, Collections.emptyMap());
      connection.connect();

      try {
        final String response = readResponse(connection);
        LogManager.instance().log(this, Level.FINE, "Response: ", null, response);
        Assertions.assertEquals(200, connection.getResponseCode());
        Assertions.assertEquals("OK", connection.getResponseMessage());
        Assertions.assertTrue(response.contains("V1"));
      } finally {
        connection.disconnect();
      }
    });
  }

  /**
   * Issue https://github.com/ArcadeData/arcadedb/discussions/468
   */
  @Test
  public void checkCommandLet() throws Exception {
    testEachServer((serverIndex) -> {
      final HttpURLConnection connection = (HttpURLConnection) new URL("http://127.0.0.1:248" + serverIndex + "/api/v1/command/graph").openConnection();

      connection.setRequestMethod("POST");
      connection.setRequestProperty("Authorization",
          "Basic " + Base64.getEncoder().encodeToString(("root:" + BaseGraphServerTest.DEFAULT_PASSWORD_FOR_TESTS).getBytes()));
      formatPayload(connection, "sql",
          "SELECT $p from " + VERTEX1_TYPE_NAME + " let pid = @rid, p = (select from " + VERTEX1_TYPE_NAME + " where @rid = $parent.pid)", null,
          Collections.emptyMap());
      connection.connect();

      try {
        final String response = readResponse(connection);
        LogManager.instance().log(this, Level.FINE, "Response: ", null, response);
        Assertions.assertEquals(200, connection.getResponseCode());
        Assertions.assertEquals("OK", connection.getResponseMessage());
        Assertions.assertTrue(response.contains("#1:0"));
      } finally {
        connection.disconnect();
      }
    });
  }

  @Test
  public void checkCommandNoDuplication() throws Exception {
    testEachServer((serverIndex) -> {
      final HttpURLConnection connection = (HttpURLConnection) new URL("http://127.0.0.1:248" + serverIndex + "/api/v1/command/graph").openConnection();

      connection.setRequestMethod("POST");
      connection.setRequestProperty("Authorization",
          "Basic " + Base64.getEncoder().encodeToString(("root:" + BaseGraphServerTest.DEFAULT_PASSWORD_FOR_TESTS).getBytes()));
      formatPayload(connection, "sql", "SELECT FROM E1", "studio", Collections.emptyMap());
      connection.connect();

      try {
        final String response = readResponse(connection);
        LogManager.instance().log(this, Level.FINE, "Response: ", null, response);
        Assertions.assertEquals(200, connection.getResponseCode());
        Assertions.assertEquals("OK", connection.getResponseMessage());

        final JSONObject responseAsJson = new JSONObject(response);

        final List<Object> vertices = responseAsJson.getJSONObject("result").getJSONArray("vertices").toList();
        Assertions.assertEquals(2, vertices.size());
        for (final Object o : vertices)
          Assertions.assertTrue(((Map) o).get("t").equals("V1") || ((Map) o).get("t").equals("V2"));

        final List<Object> records = responseAsJson.getJSONObject("result").getJSONArray("records").toList();
        Assertions.assertEquals(3, records.size());
        for (final Object o : records)
          Assertions.assertTrue(((Map) o).get("@type").equals("V1") || ((Map) o).get("@type").equals("V2") || ((Map) o).get("@type").equals("E1"));

        final List<Object> edges = responseAsJson.getJSONObject("result").getJSONArray("edges").toList();
        Assertions.assertEquals(1, edges.size());
        for (final Object o : edges)
          Assertions.assertTrue(((Map) o).get("t").equals("E1"));

      } finally {
        connection.disconnect();
      }
    });
  }

  @Test
  public void checkRecordLoading() throws Exception {
    testEachServer((serverIndex) -> {
      final HttpURLConnection connection = (HttpURLConnection) new URL(
          "http://127.0.0.1:248" + serverIndex + "/api/v1/document/graph/" + BaseGraphServerTest.root.getIdentity().toString().substring(1)).openConnection();

      connection.setRequestMethod("GET");
      connection.setRequestProperty("Authorization",
          "Basic " + Base64.getEncoder().encodeToString(("root:" + BaseGraphServerTest.DEFAULT_PASSWORD_FOR_TESTS).getBytes()));
      connection.connect();

      try {
        final String response = readResponse(connection);
        LogManager.instance().log(this, Level.FINE, "Response: ", null, response);
        Assertions.assertEquals(200, connection.getResponseCode());
        Assertions.assertEquals("OK", connection.getResponseMessage());
        Assertions.assertTrue(response.contains("V1"));
      } finally {
        connection.disconnect();
      }
    });
  }

  @Test
  public void checkRecordCreate() throws Exception {
    testEachServer((serverIndex) -> {
      final HttpURLConnection connection = (HttpURLConnection) new URL("http://127.0.0.1:248" + serverIndex + "/api/v1/document/graph").openConnection();

      connection.setRequestMethod("POST");
      connection.setRequestProperty("Authorization",
          "Basic " + Base64.getEncoder().encodeToString(("root:" + BaseGraphServerTest.DEFAULT_PASSWORD_FOR_TESTS).getBytes()));

      final JSONObject payload = new JSONObject("{\"@type\":\"Person\",\"name\":\"Jay\",\"surname\":\"Miner\",\"age\":69}");

      connection.setRequestMethod("POST");
      connection.setDoOutput(true);

      connection.connect();

      final PrintWriter pw = new PrintWriter(new OutputStreamWriter(connection.getOutputStream()));
      pw.write(payload.toString());
      pw.close();

      final String rid;
      try {
        final String response = readResponse(connection);

        Assertions.assertEquals(200, connection.getResponseCode());
        Assertions.assertEquals("OK", connection.getResponseMessage());
        LogManager.instance().log(this, Level.FINE, "Response: ", null, response);
        final JSONObject responseAsJson = new JSONObject(response);
        Assertions.assertTrue(responseAsJson.has("result"));
        rid = responseAsJson.getString("result");
        Assertions.assertTrue(rid.contains("#"));
      } finally {
        connection.disconnect();
      }

      final HttpURLConnection connection2 = (HttpURLConnection) new URL(
          "http://127.0.0.1:248" + serverIndex + "/api/v1/document/graph/" + rid.substring(1)).openConnection();

      connection2.setRequestMethod("GET");
      connection2.setRequestProperty("Authorization",
          "Basic " + Base64.getEncoder().encodeToString(("root:" + BaseGraphServerTest.DEFAULT_PASSWORD_FOR_TESTS).getBytes()));
      connection2.connect();

      try {
        final String response = readResponse(connection2);
        LogManager.instance().log(this, Level.FINE, "Response: ", null, response);
        final JSONObject responseAsJson = new JSONObject(response);
        Assertions.assertTrue(responseAsJson.has("result"));
        final JSONObject object = responseAsJson.getJSONObject("result");
        Assertions.assertEquals(200, connection2.getResponseCode());
        Assertions.assertEquals("OK", connection2.getResponseMessage());
        Assertions.assertEquals(rid, object.remove("@rid").toString());
        Assertions.assertEquals("d", object.remove("@cat"));
        Assertions.assertEquals(payload.toMap(), object.toMap());

      } finally {
        connection2.disconnect();
      }
    });
  }

  @Test
  public void checkDatabaseExists() throws Exception {
    testEachServer((serverIndex) -> {
      final HttpURLConnection connection = (HttpURLConnection) new URL("http://127.0.0.1:248" + serverIndex + "/api/v1/exists/graph/").openConnection();

      connection.setRequestMethod("GET");
      connection.setRequestProperty("Authorization",
          "Basic " + Base64.getEncoder().encodeToString(("root:" + BaseGraphServerTest.DEFAULT_PASSWORD_FOR_TESTS).getBytes()));
      connection.connect();

      try {
        final String response = readResponse(connection);
        LogManager.instance().log(this, Level.FINE, "Response: ", null, response);
        Assertions.assertEquals(200, connection.getResponseCode());
        Assertions.assertEquals("OK", connection.getResponseMessage());
        Assertions.assertTrue(new JSONObject(response).getBoolean("result"));
      } finally {
        connection.disconnect();
      }
    });
  }

  @Test
  public void checkDatabaseList() throws Exception {
    testEachServer((serverIndex) -> {
      final HttpURLConnection connection = (HttpURLConnection) new URL("http://127.0.0.1:248" + serverIndex + "/api/v1/databases").openConnection();

      connection.setRequestMethod("GET");
      connection.setRequestProperty("Authorization",
          "Basic " + Base64.getEncoder().encodeToString(("root:" + BaseGraphServerTest.DEFAULT_PASSWORD_FOR_TESTS).getBytes()));
      connection.connect();

      try {
        final String response = readResponse(connection);
        LogManager.instance().log(this, Level.FINE, "Response: ", null, response);
        Assertions.assertEquals(200, connection.getResponseCode());
        Assertions.assertEquals("OK", connection.getResponseMessage());
        final JSONArray databases = new JSONObject(response).getJSONArray("result");
        Assertions.assertEquals(1, databases.length(), "Found the following databases: " + databases);
      } finally {
        connection.disconnect();
      }
    });
  }

  @Test
  public void createAndDropDatabase() throws Exception {
    testEachServer((serverIndex) -> {
      // CREATE THE DATABASE 'JUSTFORFUN'
      HttpURLConnection connection = (HttpURLConnection) new URL("http://127.0.0.1:248" + serverIndex + "/api/v1/server").openConnection();
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Authorization",
          "Basic " + Base64.getEncoder().encodeToString(("root:" + BaseGraphServerTest.DEFAULT_PASSWORD_FOR_TESTS).getBytes()));
      formatPayload(connection, new JSONObject().put("command", "create database justforfun"));
      connection.connect();

      try {
        final String response = readResponse(connection);
        LogManager.instance().log(this, Level.FINE, "Response: ", null, response);
        Assertions.assertEquals(200, connection.getResponseCode());
        Assertions.assertEquals("OK", connection.getResponseMessage());
        Assertions.assertEquals("ok", new JSONObject(response).getString("result"));

      } finally {
        connection.disconnect();
      }

      // CHECK EXISTENCE
      connection = (HttpURLConnection) new URL("http://127.0.0.1:248" + serverIndex + "/api/v1/exists/justforfun").openConnection();
      connection.setRequestMethod("GET");
      connection.setRequestProperty("Authorization",
          "Basic " + Base64.getEncoder().encodeToString(("root:" + BaseGraphServerTest.DEFAULT_PASSWORD_FOR_TESTS).getBytes()));
      connection.connect();

      try {
        final String response = readResponse(connection);
        LogManager.instance().log(this, Level.FINE, "Response: ", null, response);
        Assertions.assertEquals(200, connection.getResponseCode());
        Assertions.assertEquals("OK", connection.getResponseMessage());
        Assertions.assertTrue(new JSONObject(response).getBoolean("result"));

      } finally {
        connection.disconnect();
      }

      // DROP DATABASE
      connection = (HttpURLConnection) new URL("http://127.0.0.1:248" + serverIndex + "/api/v1/server").openConnection();
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Authorization",
          "Basic " + Base64.getEncoder().encodeToString(("root:" + BaseGraphServerTest.DEFAULT_PASSWORD_FOR_TESTS).getBytes()));
      formatPayload(connection, new JSONObject().put("command", "drop database justforfun"));
      connection.connect();

      try {
        final String response = readResponse(connection);
        LogManager.instance().log(this, Level.FINE, "Response: ", null, response);
        Assertions.assertEquals(200, connection.getResponseCode());
        Assertions.assertEquals("OK", connection.getResponseMessage());
        Assertions.assertEquals("ok", new JSONObject(response).getString("result"));

      } finally {
        connection.disconnect();
      }

      // CHECK NOT EXISTENCE
      connection = (HttpURLConnection) new URL("http://127.0.0.1:248" + serverIndex + "/api/v1/exists/justforfun").openConnection();
      connection.setRequestMethod("GET");
      connection.setRequestProperty("Authorization",
          "Basic " + Base64.getEncoder().encodeToString(("root:" + BaseGraphServerTest.DEFAULT_PASSWORD_FOR_TESTS).getBytes()));
      connection.connect();

      try {
        final String response = readResponse(connection);
        LogManager.instance().log(this, Level.FINE, "Response: ", null, response);
        Assertions.assertEquals(200, connection.getResponseCode());
        Assertions.assertEquals("OK", connection.getResponseMessage());
        Assertions.assertFalse(new JSONObject(response).getBoolean("result"));

      } finally {
        connection.disconnect();
      }
    });
  }

  @Test
  public void closeAndReopenDatabase() throws Exception {
    testEachServer((serverIndex) -> {
      // CREATE THE DATABASE 'JUSTFORFUN'
      HttpURLConnection connection = (HttpURLConnection) new URL("http://127.0.0.1:248" + serverIndex + "/api/v1/server").openConnection();
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Authorization",
          "Basic " + Base64.getEncoder().encodeToString(("root:" + BaseGraphServerTest.DEFAULT_PASSWORD_FOR_TESTS).getBytes()));
      formatPayload(connection, new JSONObject().put("command", "create database closeAndReopen"));
      connection.connect();

      try {
        final String response = readResponse(connection);
        LogManager.instance().log(this, Level.FINE, "Response: ", null, response);
        Assertions.assertEquals(200, connection.getResponseCode());
        Assertions.assertEquals("OK", connection.getResponseMessage());
        Assertions.assertEquals("ok", new JSONObject(response).getString("result"));

      } finally {
        connection.disconnect();
      }

      // CLOSE DATABASE
      connection = (HttpURLConnection) new URL("http://127.0.0.1:248" + serverIndex + "/api/v1/server").openConnection();
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Authorization",
          "Basic " + Base64.getEncoder().encodeToString(("root:" + BaseGraphServerTest.DEFAULT_PASSWORD_FOR_TESTS).getBytes()));
      formatPayload(connection, new JSONObject().put("command", "close database closeAndReopen"));
      connection.connect();

      try {
        final String response = readResponse(connection);
        LogManager.instance().log(this, Level.FINE, "Response: ", null, response);
        Assertions.assertEquals(200, connection.getResponseCode());
        Assertions.assertEquals("OK", connection.getResponseMessage());
        Assertions.assertEquals("ok", new JSONObject(response).getString("result"));

      } finally {
        connection.disconnect();
      }

      // RE-OPEN DATABASE
      connection = (HttpURLConnection) new URL("http://127.0.0.1:248" + serverIndex + "/api/v1/server").openConnection();
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Authorization",
          "Basic " + Base64.getEncoder().encodeToString(("root:" + BaseGraphServerTest.DEFAULT_PASSWORD_FOR_TESTS).getBytes()));
      formatPayload(connection, new JSONObject().put("command", "open database closeAndReopen"));
      connection.connect();

      try {
        final String response = readResponse(connection);
        LogManager.instance().log(this, Level.FINE, "Response: ", null, response);
        Assertions.assertEquals(200, connection.getResponseCode());
        Assertions.assertEquals("OK", connection.getResponseMessage());
        Assertions.assertEquals("ok", new JSONObject(response).getString("result"));

      } finally {
        connection.disconnect();
      }

      // CHECK EXISTENCE
      connection = (HttpURLConnection) new URL("http://127.0.0.1:248" + serverIndex + "/api/v1/exists/closeAndReopen").openConnection();
      connection.setRequestMethod("GET");
      connection.setRequestProperty("Authorization",
          "Basic " + Base64.getEncoder().encodeToString(("root:" + BaseGraphServerTest.DEFAULT_PASSWORD_FOR_TESTS).getBytes()));
      connection.connect();

      try {
        final String response = readResponse(connection);
        LogManager.instance().log(this, Level.FINE, "Response: ", null, response);
        Assertions.assertEquals(200, connection.getResponseCode());
        Assertions.assertEquals("OK", connection.getResponseMessage());
        Assertions.assertTrue(new JSONObject(response).getBoolean("result"));

      } finally {
        connection.disconnect();
      }
    });
  }

  @Test
  public void testEmptyDatabaseName() throws Exception {
    testEachServer((serverIndex) -> {
      // CREATE THE DATABASE ''
      final HttpURLConnection connection = (HttpURLConnection) new URL("http://127.0.0.1:248" + serverIndex + "/api/v1/server").openConnection();
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Authorization",
          "Basic " + Base64.getEncoder().encodeToString(("root:" + BaseGraphServerTest.DEFAULT_PASSWORD_FOR_TESTS).getBytes()));
      formatPayload(connection, new JSONObject().put("command", "create database "));
      connection.connect();

      try {
        readResponse(connection);
        Assertions.fail("Empty database should be an error");
      } catch (final Exception e) {
        Assertions.assertEquals(400, connection.getResponseCode());

      } finally {
        connection.disconnect();
      }
    });
  }
}
