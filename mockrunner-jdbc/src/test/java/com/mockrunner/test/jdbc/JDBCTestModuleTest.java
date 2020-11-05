package com.mockrunner.test.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.mockrunner.base.VerifyFailedException;
import com.mockrunner.jdbc.JDBCTestModule;
import com.mockrunner.jdbc.ParameterSets;
import com.mockrunner.mock.jdbc.JDBCMockObjectFactory;
import com.mockrunner.mock.jdbc.MockBlob;
import com.mockrunner.mock.jdbc.MockCallableStatement;
import com.mockrunner.mock.jdbc.MockClob;
import com.mockrunner.mock.jdbc.MockParameterMap;
import com.mockrunner.mock.jdbc.MockPreparedStatement;
import com.mockrunner.mock.jdbc.MockResultSet;
import com.mockrunner.mock.jdbc.MockSavepoint;
import com.mockrunner.mock.jdbc.MockStatement;
import com.mockrunner.mock.jdbc.ParameterIndex;

public class JDBCTestModuleTest
{
    private JDBCMockObjectFactory mockfactory;
    private JDBCTestModule module;

    @Before
    public void setUp() throws Exception
    {
        mockfactory = new JDBCMockObjectFactory();
        module = new JDBCTestModule(mockfactory);
    }
    
    private void prepareCallableStatements() throws Exception
    {   
        mockfactory.getMockConnection().prepareCall("{call getData(?, ?, ?, ?)}");
        mockfactory.getMockConnection().prepareCall("{call setData(?, ?, ?, ?)}");
    }
    
    private void preparePreparedStatements() throws Exception
    {   
        mockfactory.getMockConnection().prepareStatement("INSERT INTO TEST (COL1, COL2) VALUES(?, ?)");
        mockfactory.getMockConnection().prepareStatement("insert into test (col1, col2, col3) values(?, ?, ?)");
        mockfactory.getMockConnection().prepareStatement("update mytable set test = test + ? where id = ?", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE, ResultSet.HOLD_CURSORS_OVER_COMMIT);
    }
    
    private void prepareStatements() throws Exception
    {   
        mockfactory.getMockConnection().createStatement();
        mockfactory.getMockConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
    }

    @Test
    public void testGetStatements() throws Exception
    {
        List<MockStatement> statements = module.getStatements();
        assertNotNull(statements);
        assertEquals(0, statements.size());
        assertNull(module.getStatement(1));
        module.verifyNumberStatements(0);
        prepareStatements();
        statements = module.getStatements();
        assertNotNull(statements);
        assertEquals(2, statements.size());
        assertNotNull(module.getStatement(0));
        assertNotNull(module.getStatement(1));
        module.verifyNumberStatements(2);
    }
    
    @Test
    public void testGetPreparedStatementsByIndex() throws Exception
    {
        List<MockPreparedStatement> statements = module.getPreparedStatements();
        assertNotNull(statements);
        assertEquals(0, statements.size());
        assertNull(module.getPreparedStatement(1));
        module.verifyNumberPreparedStatements(0);
        preparePreparedStatements();
        statements = module.getPreparedStatements();
        assertNotNull(statements);
        assertEquals(3, statements.size());
        module.verifyNumberPreparedStatements(3);  
    }
    
    @Test
    public void testGetPreparedStatementsBySQL() throws Exception
    {
        preparePreparedStatements();
        List<MockPreparedStatement> statements = module.getPreparedStatements("insert");
        assertNotNull(statements);
        assertEquals(2, statements.size());
        MockPreparedStatement statement = module.getPreparedStatement("insert");
        assertEquals("INSERT INTO TEST (COL1, COL2) VALUES(?, ?)", statement.getSQL());
        module.verifyNumberPreparedStatements(1, "update");
        module.verifyNumberPreparedStatements(1, "UPDATE");
        module.verifyNumberPreparedStatements(2, "insert");
        module.verifyNumberPreparedStatements(3);
        module.verifyPreparedStatementPresent("update");
        module.verifyPreparedStatementNotPresent("select");
        module.setCaseSensitive(true);
        statements = module.getPreparedStatements("insert");
        assertNotNull(statements);
        assertEquals(1, statements.size());
        statement = module.getPreparedStatement("insert");
        assertEquals("insert into test (col1, col2, col3) values(?, ?, ?)", statement.getSQL());
        module.verifyNumberPreparedStatements(1, "update");
        module.verifyNumberPreparedStatements(0, "UPDATE");
        module.verifyNumberPreparedStatements(1, "insert");
        module.verifyNumberPreparedStatements(1, "INSERT");
        module.verifyNumberPreparedStatements(3);
        module.setExactMatch(true);
        statements = module.getPreparedStatements("insert");
        assertNotNull(statements);
        assertEquals(0, statements.size());
        module.verifyNumberPreparedStatements(0, "update");
        module.verifyNumberPreparedStatements(0, "UPDATE");
        module.verifyNumberPreparedStatements(0, "insert");
        module.verifyNumberPreparedStatements(0, "INSERT");
        module.verifyPreparedStatementNotPresent("update");
        module.verifyPreparedStatementPresent("insert into test (col1, col2, col3) values(?, ?, ?)");
    }
    
    @Test
    public void testGetPreparedStatementsBySQLRegEx() throws Exception
    {
        module.setUseRegularExpressions(true);
        preparePreparedStatements();
        List<MockPreparedStatement> statements = module.getPreparedStatements("insert");
        assertNotNull(statements);
        assertEquals(0, statements.size());
        statements = module.getPreparedStatements("insert into.*");
        assertEquals(2, statements.size());
        module.verifyNumberPreparedStatements(0, "update");
        module.verifyNumberPreparedStatements(2, "insert (.*) test.*");
        module.verifyNumberPreparedStatements(2, "insert (.*) TEST.*");
        module.setCaseSensitive(true);
        module.verifyNumberPreparedStatements(0, "insert (.*) TEST.*");
    }
    
    @Test
    public void testGetPreparedStatementObjects() throws Exception
    {
        preparePreparedStatements();
        MockPreparedStatement statement = module.getPreparedStatement("update");
        statement.setInt(1, 3);
        statement.setLong(2, 10000);
        statement.setNull(3, 1);
        assertEquals(3, statement.getParameter(1));
        assertEquals(10000L, statement.getParameter(2));
        assertNull(statement.getParameter(3));
        assertTrue(statement.getParameterMap().containsKey(new ParameterIndex(3)));
        module.verifyPreparedStatementParameterPresent(statement, 1);
        module.verifyPreparedStatementParameterPresent("update", 3);
        module.verifyPreparedStatementParameterNotPresent("update", 4);
        module.verifyPreparedStatementParameterNotPresent(0, 1);
        module.verifyPreparedStatementParameter(statement, 1, 3);
        module.verifyPreparedStatementParameter(2, 2, 10000L);
        module.verifyPreparedStatementParameter(statement, 3, null);
        try
        {
            module.verifyPreparedStatementParameter(2, 2, null);
            fail();
        }
        catch(VerifyFailedException exc)
        {
            //should throw Exception
        }
        try
        {
            module.verifyPreparedStatementParameter(statement, 1, null);
            fail();
        }
        catch(VerifyFailedException exc)
        {
            //should throw Exception
        }
        statement = module.getPreparedStatement("INSERT INTO TEST (COL1, COL2) VALUES(?, ?)");  
        statement.setString(1, "test1");
        statement.setString(2, "test2");
        statement.setBytes(3, new byte[] {1, 2, 3});
        statement.setBytes(4, new byte[] {});
        module.verifyPreparedStatementParameterPresent(statement, 2);
        module.verifyPreparedStatementParameterPresent(statement, 3);
        module.verifyPreparedStatementParameterPresent(statement, 4);
        module.verifyPreparedStatementParameterNotPresent(statement, 5);
        module.verifyPreparedStatementParameter(0, 3, new byte[] {1, 2, 3});
        module.verifyPreparedStatementParameter(0, 4, new byte[] {});
    }
    
    @Test
    public void testGetCallableStatementsByIndex() throws Exception
    {
        module.verifyNumberCallableStatements(0);
        prepareCallableStatements();
        module.verifyNumberCallableStatements(2);
        List<MockCallableStatement> statements = module.getCallableStatements();
        assertEquals("{call getData(?, ?, ?, ?)}", statements.get(0).getSQL());
        assertEquals("{call setData(?, ?, ?, ?)}", statements.get(1).getSQL());
    }
    
    @Test
    public void testGetCallableStatementsBySQL() throws Exception
    {
        prepareCallableStatements();
        List<MockCallableStatement> statements = module.getCallableStatements("call");
        assertTrue(statements.size() == 2);
        MockCallableStatement statement = module.getCallableStatement("CALL");
        assertEquals("{call getData(?, ?, ?, ?)}", statement.getSQL());
        module.setCaseSensitive(true);
        statement = module.getCallableStatement("CALL");
        assertNull(statement);
        module.setCaseSensitive(false);
        module.setExactMatch(true);
        statement = module.getCallableStatement("CALL");
        assertNull(statement);
        statements = module.getCallableStatements("{call setData(?, ?, ?, ?)}");
        assertTrue(statements.size() == 1);
        module.setExactMatch(false);
        module.verifyNumberCallableStatements(1, "call getData");
        module.verifyNumberCallableStatements(2, "call");
        module.verifyCallableStatementPresent("call setData");
        module.verifyCallableStatementNotPresent("call setXYZ");
    }
    
    @Test
    public void testGetCallableStatementsBySQLRegEx() throws Exception
    {
        module.setUseRegularExpressions(true);
        prepareCallableStatements();
        List<MockCallableStatement> statements = module.getCallableStatements("call");
        assertTrue(statements.isEmpty());
        MockCallableStatement statement = module.getCallableStatement(".*CALL.*");
        assertEquals("{call getData(?, ?, ?, ?)}", statement.getSQL());
        module.verifyCallableStatementNotPresent("call setData");
        module.verifyCallableStatementPresent("{call setData.*}");
    }
    
    @Test
    public void testGetCallableStatementObjects() throws Exception
    {
        prepareCallableStatements();
        MockCallableStatement statement = module.getCallableStatement("{call setData(?, ?, ?, ?)}");
        statement.setInt("xyz", 1);
        statement.setString("3", null);
        statement.setString(1, "xyz");
        MockParameterMap namedParameter = statement.getNamedParameterMap();
        MockParameterMap indexedParameter = statement.getIndexedParameterMap();
        assertTrue(namedParameter.size() == 2);
        assertEquals(1, namedParameter.get("xyz"));
        assertNull(namedParameter.get("3"));
        assertTrue(indexedParameter.size() == 1);
        assertEquals("xyz", indexedParameter.get(1));
        module.verifyCallableStatementParameterPresent(1, 1);
        try
        {
            module.verifyCallableStatementParameterNotPresent(statement, "3");
            fail();
        }
        catch(VerifyFailedException exc)
        {
            //should throw Exception
        }
        module.verifyCallableStatementParameterNotPresent(1, 2);
        module.verifyCallableStatementParameterPresent(statement, "3");
        module.verifyCallableStatementParameterNotPresent(statement, "31"); 
        module.verifyCallableStatementParameter("{call setData(?, ?, ?, ?)}", "xyz", 1);
        module.verifyCallableStatementParameter(1, 1, "xyz");
        module.verifyCallableStatementParameter(1, "3", null);
        try
        {
            module.verifyCallableStatementParameter("{call setData(?, ?, ?, ?)}", "xyz", null);
            fail();
        }
        catch(VerifyFailedException exc)
        {
            //should throw Exception
        }
        try
        {
            module.verifyCallableStatementParameter(1, 1, null);
            fail();
        }
        catch(VerifyFailedException exc)
        {
            //should throw Exception
        }
        try
        {
            module.verifyCallableStatementParameter(1, 1, "zzz");
            fail();
        }
        catch(VerifyFailedException exc)
        {
            //should throw Exception
        }
        try
        {
            module.verifyCallableStatementParameter(1, 5, null);
            fail();
        }
        catch(VerifyFailedException exc)
        {
            //should throw Exception
        }
        statement.setBytes(1, new byte[] {1});
        statement.setBlob(2, new MockBlob(new byte[] {3, 4}));
        statement.setClob(3, new MockClob("test"));
        module.verifyCallableStatementParameter(1, 1, new byte[] {1});
        module.verifyCallableStatementParameter(statement, 2, new MockBlob(new byte[] {3, 4}));
        module.verifyCallableStatementParameter(1, 3, new MockClob("test"));
        try
        {
            module.verifyCallableStatementParameter(1, 1, new byte[] {2});
            fail();
        }
        catch(VerifyFailedException exc)
        {
            //should throw Exception
        }
    }
    
    @Test
    public void testVerifyCallableStatementOutParameterRegistered() throws Exception
    {
        prepareCallableStatements();
        MockCallableStatement statement = module.getCallableStatement("{call getData(?, ?, ?, ?)}");
        statement.registerOutParameter(1, Types.DECIMAL);
        statement.registerOutParameter("test", Types.BLOB);
        statement.registerOutParameter("xyz", Types.BINARY);
        module.verifyCallableStatementOutParameterRegistered(statement, 1);
        module.verifyCallableStatementOutParameterRegistered(statement, "test");
        module.verifyCallableStatementOutParameterRegistered(statement, "xyz");
        try
        {
            module.verifyCallableStatementOutParameterRegistered("{call setData(?, ?, ?, ?)}", "xyz");
            fail();
        }
        catch(VerifyFailedException exc)
        {
            //should throw Exception
        }
        try
        {
            module.verifyCallableStatementOutParameterRegistered(1, "test");
            fail();
        }
        catch(VerifyFailedException exc)
        {
            //should throw Exception
        }
        module.verifyCallableStatementOutParameterRegistered(0, "test");
    }
    
    @Test
    public void testGetExecutedSQLStatements() throws Exception
    {
        prepareStatements();
        preparePreparedStatements();
        prepareCallableStatements();
        MockStatement statement = module.getStatement(0);
        statement.execute("select");
        statement.execute("UPDATE");
        MockPreparedStatement preparedStatement = module.getPreparedStatement("insert");
        preparedStatement.execute();
        MockCallableStatement callableStatement = module.getCallableStatement("call");
        callableStatement.executeUpdate();
        List<String> sqlStatements = module.getExecutedSQLStatements();
        assertTrue(sqlStatements.size() == 4);
        assertTrue(sqlStatements.contains("select"));
        assertTrue(sqlStatements.contains("UPDATE"));
        assertTrue(sqlStatements.contains("INSERT INTO TEST (COL1, COL2) VALUES(?, ?)"));
        assertTrue(sqlStatements.contains("{call getData(?, ?, ?, ?)}"));
        module.verifySQLStatementExecuted("select");
        module.verifySQLStatementExecuted("update");
        module.verifySQLStatementExecuted("INSERT");
        module.verifySQLStatementExecuted("{call");
        module.verifySQLStatementNotExecuted("{call}");
        module.setCaseSensitive(true);
        module.verifySQLStatementExecuted("UPDATE");
        module.verifySQLStatementNotExecuted("update");
        module.setExactMatch(true);
        module.verifySQLStatementExecuted("{call getData(?, ?, ?, ?)}");
        module.verifySQLStatementNotExecuted("call");
        module.setCaseSensitive(false);
        module.verifySQLStatementExecuted("{CALL getData(?, ?, ?, ?)}");
    }
    
    @Test
    public void testGetExecutedSQLStatementsRegEx() throws Exception
    {
        module.setUseRegularExpressions(true);
        prepareStatements();
        preparePreparedStatements();
        prepareCallableStatements();
        MockStatement statement = module.getStatement(0);
        statement.execute("select");
        statement.execute("UPDATE");
        MockPreparedStatement preparedStatement = module.getPreparedStatement("insert.*");
        preparedStatement.execute();
        MockCallableStatement callableStatement = module.getCallableStatement("{call.*");
        callableStatement.executeUpdate();
        module.verifySQLStatementExecuted("select");
        module.verifySQLStatementExecuted("update.*");
        module.verifySQLStatementExecuted("INSERT into .*");
        module.verifySQLStatementExecuted("{call.*");
        module.verifySQLStatementNotExecuted("{call}");
        module.setCaseSensitive(true);
        module.verifySQLStatementExecuted("UPDATE.*");
        module.verifySQLStatementNotExecuted("update");
        module.setExactMatch(true);
        module.verifySQLStatementNotExecuted("UPDATE.*");
        module.verifySQLStatementExecuted("UPDATE");
    }
    
    public void doNotTestGetReturnedResultSets() throws Exception
    {
        prepareStatements();
        preparePreparedStatements();
        prepareCallableStatements();
        MockResultSet resultSet1 = module.getStatementResultSetHandler().createResultSet("1");
        MockResultSet resultSet2 = module.getStatementResultSetHandler().createResultSet("2");
        MockResultSet resultSet3 = module.getStatementResultSetHandler().createResultSet("3");
        MockResultSet resultSet4 = module.getStatementResultSetHandler().createResultSet("4");
        MockResultSet resultSet5 = module.getStatementResultSetHandler().createResultSet("5");
        MockResultSet resultSet6 = module.getStatementResultSetHandler().createResultSet("6");
        MockResultSet resultSet7 = module.getStatementResultSetHandler().createResultSet("7");
        module.getStatementResultSetHandler().prepareGlobalResultSet(resultSet1);
        module.getStatementResultSetHandler().prepareResultSet("select id", resultSet2);
        module.getStatementResultSetHandler().prepareResultSets("select xyz", new MockResultSet[] {resultSet3, resultSet5});
        module.getPreparedStatementResultSetHandler().prepareResultSet("select name", resultSet4, new String[] {"test"});
        module.getCallableStatementResultSetHandler().prepareResultSet("call set", resultSet5);
        module.getCallableStatementResultSetHandler().prepareResultSets("call set", new MockResultSet[] {resultSet6, resultSet7, resultSet1}, new String[] {"xyz"});
        MockStatement statement = module.getStatement(0);
        statement.executeQuery("select name");
        statement.executeQuery("select id");
        statement.executeQuery("select xyz");
        List<MockResultSet[]> list = module.getReturnedResultSets();
        assertEquals(3, list.size());
        assertEquals("1", list.get(0)[0].getId());
        assertEquals("2", list.get(1)[0].getId());
        assertEquals("3", list.get(2)[0].getId());
        assertEquals("5", list.get(2)[1].getId());
        MockPreparedStatement preparedStatement = (MockPreparedStatement)mockfactory.getMockConnection().prepareStatement("SELECT NAME");
        preparedStatement.setString(1, "test");
        preparedStatement.executeQuery();
        list = module.getReturnedResultSets();
        assertEquals(4, list.size());
        assertEquals("4", list.get(3)[0].getId());
        MockCallableStatement callableStatement = module.getCallableStatement("call set");
        callableStatement.executeQuery();
        list = module.getReturnedResultSets();
        assertEquals(5, list.size());
        assertEquals("5", list.get(4)[0].getId());
        callableStatement.setString(1, "xyz");
        callableStatement.executeQuery();
        list = module.getReturnedResultSets();
        assertEquals(6, list.size());
        assertEquals("6", list.get(5)[0].getId());
        assertEquals("7", list.get(5)[1].getId());
        assertEquals("1", list.get(5)[2].getId());
        List<MockResultSet> list2 = module.getReturnedResultSets("1");
        assertEquals(2, list2.size());
        MockResultSet returned1 = list2.get(0);
        MockResultSet returned2 = list2.get(1);
        assertEquals("1", returned1.getId());
        assertEquals("1", returned2.getId());
        assertNotSame(returned1, returned2);
        assertNotSame(returned1, resultSet1);
        MockResultSet returned = module.getReturnedResultSet("1");
        assertEquals("1", returned.getId());
        assertSame(returned1, returned);
    }
    
    @Test
    public void testReturnedResultSetsClosed() throws Exception
    {
        prepareStatements();
        preparePreparedStatements();
        prepareCallableStatements();
        MockResultSet resultSet1 = module.getStatementResultSetHandler().createResultSet("1");
        MockResultSet resultSet2 = module.getStatementResultSetHandler().createResultSet("2");
        MockResultSet resultSet3 = module.getStatementResultSetHandler().createResultSet("3");
        MockResultSet resultSet4 = module.getStatementResultSetHandler().createResultSet("4");
        MockResultSet resultSet5 = module.getStatementResultSetHandler().createResultSet("5");
        module.getStatementResultSetHandler().prepareGlobalResultSet(resultSet1);
        module.getStatementResultSetHandler().prepareResultSet("select id", resultSet2);
        module.getStatementResultSetHandler().prepareResultSet("select xyz", resultSet3);
        module.getPreparedStatementResultSetHandler().prepareResultSet("select name", resultSet4, new String[] {"test"});
        module.getCallableStatementResultSetHandler().prepareResultSet("call set", resultSet5, new String[] {"xyz"});
        MockStatement statement = module.getStatement(0);
        statement.executeQuery("select name");
        statement.executeQuery("select id");
        List<MockResultSet[]> list = module.getReturnedResultSets();
        assertEquals(2, list.size());
        assertEquals("1", list.get(0)[0].getId());
        assertEquals("2", list.get(1)[0].getId());
        MockPreparedStatement preparedStatement = module.getPreparedStatement("insert");
        preparedStatement.execute();
        list = module.getReturnedResultSets();
        assertEquals(2, list.size());
        assertEquals("1", list.get(0)[0].getId());
        assertEquals("2", list.get(1)[0].getId());
        preparedStatement = (MockPreparedStatement)mockfactory.getMockConnection().prepareStatement("SELECT NAME");
        preparedStatement.setString(1, "test");
        preparedStatement.executeQuery();
        list = module.getReturnedResultSets();
        assertEquals(3, list.size());
        assertEquals("1", list.get(0)[0].getId());
        assertEquals("2", list.get(1)[0].getId());
        assertEquals("4", list.get(2)[0].getId());
        MockCallableStatement callableStatement = module.getCallableStatement("call set");
        callableStatement.setString(1, "test");
        callableStatement.executeQuery();
        list = module.getReturnedResultSets();
        assertEquals(3, list.size());
        assertEquals("1", list.get(0)[0].getId());
        assertEquals("2", list.get(1)[0].getId());
        assertEquals("4", list.get(2)[0].getId());
        callableStatement.setString(1, "xyz");
        callableStatement.executeQuery();
        list = module.getReturnedResultSets();
        assertEquals(4, list.size());
        assertEquals("1", list.get(0)[0].getId());
        assertEquals("2", list.get(1)[0].getId());
        assertEquals("4", list.get(2)[0].getId());
        assertEquals("5", list.get(3)[0].getId());
        list.get(0)[0].close();
        module.verifyResultSetClosed("1");
        try
        {
            module.verifyResultSetClosed("2");
            fail();
        }
        catch(VerifyFailedException exc)
        {
            //should throw exception
        }
        try
        {
            module.verifyAllResultSetsClosed();
            fail();
        }
        catch(VerifyFailedException exc)
        {
            //should throw exception
        }
        list.get(1)[0].close();
        list.get(2)[0].close();
        list.get(3)[0].close();
        module.verifyAllResultSetsClosed();
    }
    
    @Test
    public void testMultipleReturnedResultSetsClosed() throws Exception
    {
        prepareStatements();
        preparePreparedStatements();
        prepareCallableStatements();
        MockResultSet resultSet1 = module.getStatementResultSetHandler().createResultSet("1");
        MockResultSet resultSet2 = module.getStatementResultSetHandler().createResultSet("2");
        MockResultSet resultSet3 = module.getStatementResultSetHandler().createResultSet("3");
        MockResultSet resultSet4 = module.getStatementResultSetHandler().createResultSet("4");
        MockResultSet resultSet5 = module.getStatementResultSetHandler().createResultSet("5");
        MockResultSet resultSet6 = module.getStatementResultSetHandler().createResultSet("6");
        MockResultSet resultSet7 = module.getStatementResultSetHandler().createResultSet("7");
        module.getStatementResultSetHandler().prepareGlobalResultSet(resultSet1);
        module.getStatementResultSetHandler().prepareResultSet("select id", resultSet2);
        module.getStatementResultSetHandler().prepareResultSets("select xyz", new MockResultSet[] {resultSet3, resultSet5});
        module.getPreparedStatementResultSetHandler().prepareResultSet("select name", resultSet4, new String[] {"test"});
        module.getCallableStatementResultSetHandler().prepareResultSet("call set", resultSet5);
        module.getCallableStatementResultSetHandler().prepareResultSets("call set", new MockResultSet[] {resultSet6, resultSet7, resultSet1}, new String[] {"xyz"});
        MockStatement statement = module.getStatement(0);
        statement.executeQuery("select name");
        statement.executeQuery("select id");
        statement.executeQuery("select xyz");
        MockPreparedStatement preparedStatement = (MockPreparedStatement)mockfactory.getMockConnection().prepareStatement("SELECT NAME");
        preparedStatement.setString(1, "test");
        preparedStatement.executeQuery();
        MockCallableStatement callableStatement = module.getCallableStatement("call set");
        callableStatement.executeQuery();
        callableStatement.setString(1, "xyz");
        callableStatement.executeQuery();
        try
        {
            module.verifyAllStatementsClosed();
            fail();
        }
        catch(Exception exc)
        {
            //should throw Exception
        }
        List<MockResultSet[]> list = module.getReturnedResultSets();
        for(int ii = 0; ii < list.size() - 1; ii++) // last ResultSet is not to be closed
        {
            MockResultSet[] resultSets = list.get(ii);
            for (MockResultSet resultSet : resultSets) {
                resultSet.close();
            }
        }
        try
        {
            module.verifyAllStatementsClosed();
            fail();
        }
        catch(Exception exc)
        {
            //should throw Exception
        }
        MockResultSet[] resultSets = list.get(list.size() - 1);
        for (MockResultSet resultSet : resultSets) {
            resultSet.close();
        }
        module.verifyAllResultSetsClosed();
    }
    
    @Test
    public void testStatementsClosed() throws Exception
    {
        prepareStatements();
        preparePreparedStatements();
        prepareCallableStatements();
        MockStatement statement = module.getStatement(0);
        statement.close();
        module.verifyStatementClosed(0);
        MockPreparedStatement preparedStatement = module.getPreparedStatement("update");
        preparedStatement.close();
        module.verifyPreparedStatementClosed("update");
        try
        {
            module.verifyAllStatementsClosed();
            fail();
        }
        catch(Exception exc)
        {
            //should throw Exception
        }
        List<MockStatement> statements = new ArrayList<MockStatement>();
        statements.addAll(module.getStatements());
        statements.addAll(module.getPreparedStatements());
        statements.addAll(module.getCallableStatements());
        for (MockStatement statement1 : statements) {
            statement1.close();
        }
        module.verifyAllStatementsClosed();
        mockfactory.getMockConnection().close();
        module.verifyConnectionClosed();
    }
    
    @Test
    public void testSavepoints() throws Exception
    {
        mockfactory.getMockConnection().setSavepoint();
        mockfactory.getMockConnection().setSavepoint("test");
        Savepoint savepoint2 = mockfactory.getMockConnection().setSavepoint("xyz");
        Savepoint savepoint3 = mockfactory.getMockConnection().setSavepoint();
        module.verifySavepointNotReleased(0);
        module.verifySavepointNotReleased(1);
        module.verifySavepointNotReleased(2);
        module.verifySavepointNotReleased(3);
        module.verifySavepointNotRolledBack(0);
        module.verifySavepointNotRolledBack("test");
        module.verifySavepointNotRolledBack(2);
        module.verifySavepointNotRolledBack(3);
        mockfactory.getMockConnection().releaseSavepoint(savepoint2);
        mockfactory.getMockConnection().rollback(savepoint3);
        module.verifySavepointNotReleased(0);
        module.verifySavepointNotReleased(1);
        module.verifySavepointReleased("xyz");
        module.verifySavepointNotReleased(3);
        module.verifySavepointNotRolledBack(0);
        module.verifySavepointNotRolledBack(1);
        module.verifySavepointNotRolledBack("xyz");
        module.verifySavepointRolledBack(3);
        try
        {
            module.verifySavepointReleased("test");
            fail();
        }
        catch(Exception exc)
        {
            //should throw Exception
        }
        try
        {
            module.verifySavepointNotRolledBack(3);
            fail();
        }
        catch(Exception exc)
        {
            //should throw Exception
        }
        List<MockSavepoint> savepoints = module.getSavepoints();
        int[] ids = new int[4];
        for(int ii = 0; ii < savepoints.size(); ii++)
        {
            ids[ii] += 1;
        }
        assertTrue(ids[0] == 1);
        assertTrue(ids[1] == 1);
        assertTrue(ids[2] == 1);
        assertTrue(ids[3] == 1);
        Savepoint savepoint = module.getSavepoint("xyz");
        assertTrue(savepoint == savepoint2);
    }
    
    @Test
    public void testVerifyNumberCommitsAndRollbacks() throws Exception
    {
        try
        {
            module.verifyCommitted();
            fail();
        }
        catch(Exception exc)
        {
            //should throw Exception
        }
        try
        {
            module.verifyRolledBack();
            fail();
        }
        catch(Exception exc)
        {
            //should throw Exception
        }
        Savepoint savepoint = mockfactory.getMockConnection().setSavepoint();
        mockfactory.getMockConnection().commit();
        mockfactory.getMockConnection().rollback();
        mockfactory.getMockConnection().rollback(savepoint);
        module.verifyCommitted();
        module.verifyRolledBack();
        module.verifyNumberCommits(1);
        module.verifyNumberRollbacks(2);
    }
    
    @Test
    public void testVerifyResultSet()
    {
        MockResultSet resultSet1 = module.getStatementResultSetHandler().createResultSet("test");
        resultSet1.addRow(new Integer[] {1, 2, 3});
        resultSet1.addRow(new Integer[] {4, 5, 6});
        resultSet1.addRow(new Integer[] {7, 8, 9});
        module.getStatementResultSetHandler().addReturnedResultSet(resultSet1);
        MockResultSet resultSet2 = module.getStatementResultSetHandler().createResultSet("xyz");
        resultSet2.addColumn("column", new String[] {"1", "2", "3"});
        module.getStatementResultSetHandler().addReturnedResultSet(resultSet2);
        module.verifyResultSetRow("test", 2, new Integer[] {4, 5, 6});
        try
        {
            module.verifyResultSetRow(resultSet1, 3, new Integer[] {4, 5, 6});
            fail();
        }
        catch(VerifyFailedException exc)
        {
            //should throw exception
        }
        module.verifyResultSetColumn("test", 1, new Integer[] {1, 4, 7});
        module.verifyResultSetColumn(resultSet2, 1, new String[] {"1", "2", "3"});
        module.verifyResultSetColumn(resultSet2, "column", new String[] {"1", "2", "3"});
        module.verifyResultSetRow("xyz", 3, new String[] {"3"});
        try
        {
            module.verifyResultSetRow(resultSet2, 3, new String[] {"3", "4"});
            fail();
        }
        catch(VerifyFailedException exc)
        {
            //should throw exception
        }
        try
        {
            module.verifyResultSetColumn("xyz", "testColumn", new String[] {"1"});
            fail();
        }
        catch(VerifyFailedException exc)
        {
            //should throw exception
        }
        try
        {
            module.verifyResultSetColumn("xyz", 2, new String[] {"1", "2", "3"});
            fail();
        }
        catch(VerifyFailedException exc)
        {
            //should throw exception
        }
        try
        {
            module.verifyResultSetRow(resultSet2, 5, new String[] {"1", "2", "3"});
            fail();
        }
        catch(VerifyFailedException exc)
        {
            //should throw exception
        }
        try
        {
            module.verifyResultSetEquals(resultSet1, resultSet2);
            fail();
        }
        catch(VerifyFailedException exc)
        {
            //should throw exception
        }
        module.verifyResultSetEquals(resultSet1, resultSet1);
        module.verifyResultSetEquals(resultSet2, resultSet2);
        resultSet2 = module.getStatementResultSetHandler().createResultSet("test2");
        resultSet2.addRow(new Integer[] {1, 2, 3});
        resultSet2.addRow(new Integer[] {4, 5, 6});
        resultSet2.addRow(new Integer[] {7, 8, 9});
        module.getStatementResultSetHandler().addReturnedResultSet(resultSet2);
        module.getStatementResultSetHandler().addReturnedResultSet(resultSet1);
        module.verifyResultSetEquals(resultSet1, resultSet2);
        module.verifyResultSetEquals("test", resultSet2);
        module.verifyResultSetEquals("test2", resultSet1);
    }
    
    @Test
    public void testVerifyResultSetRowModified() throws Exception
    {  
        MockResultSet resultSet = module.getStatementResultSetHandler().createResultSet("test");
        resultSet.addRow(new Integer[] {1, 2, 3});
        resultSet.addRow(new Integer[] {4, 5, 6});
        resultSet.addRow(new Integer[] {7, 8, 9});
        module.getStatementResultSetHandler().prepareResultSet("select", resultSet);
        Statement statement = mockfactory.getMockConnection().createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
        MockResultSet returnedResultSet = (MockResultSet)statement.executeQuery("select");
        resultSet = module.getStatementResultSetHandler().getReturnedResultSets().get(0)[0];
        module.verifyResultSetRowNotDeleted(resultSet, 1);
        module.verifyResultSetRowNotDeleted("test", 2);
        module.verifyResultSetRowNotInserted("test", 2);
        module.verifyResultSetRowNotUpdated(resultSet, 3);
        try
        {
            module.verifyResultSetRowUpdated(resultSet, 1);
            fail();
        }
        catch(VerifyFailedException exc)
        {
            //should throw exception
        }
        resultSet.setResultSetConcurrency(ResultSet.CONCUR_UPDATABLE);
        returnedResultSet.next();
        returnedResultSet.updateRow();
        module.verifyResultSetRowUpdated(resultSet, 1);
        returnedResultSet.next();
        returnedResultSet.deleteRow();
        module.verifyResultSetRowDeleted(resultSet, 2);
        returnedResultSet.next();
        returnedResultSet.moveToInsertRow();
        returnedResultSet.updateString(1, "test");
        returnedResultSet.insertRow();
        returnedResultSet.moveToCurrentRow();
        module.verifyResultSetRowInserted("test", 3);
        returnedResultSet.first();
        returnedResultSet.moveToInsertRow();
        returnedResultSet.updateString(1, "test");
        returnedResultSet.insertRow();
        returnedResultSet.moveToCurrentRow();
        module.verifyResultSetRowInserted("test", 1);
        module.verifyResultSetRowDeleted(resultSet, 3);
        module.verifyResultSetRowNotUpdated(resultSet, 4);
    }
    
    @Test
    public void testGetExecutedSQLStatementParameter() throws Exception
    {
		prepareStatements();
		preparePreparedStatements();
		prepareCallableStatements();
		module.getPreparedStatement(0).setString(1, "test");
		module.getPreparedStatement(0).setShort(2, (short)2);
		module.getPreparedStatement(1).setBytes(1, new byte[]{1});
		module.getCallableStatement(1).setBoolean("name", false);
		module.getStatement(0).execute("select mydata");
		module.getStatement(1).execute("select mydata");
		module.getPreparedStatement(0).execute();
		module.getPreparedStatement(1).execute();
		module.getPreparedStatement(2).execute();
		module.getCallableStatement(0).execute();
		module.getCallableStatement(1).execute();
		Map<String, ParameterSets> parameterMap = module.getExecutedSQLStatementParameterMap();
		assertEquals(5, parameterMap.size());
		MockParameterMap preparedStatementMap1 = parameterMap.get("INSERT INTO TEST (COL1, COL2) VALUES(?, ?)").getParameterSet(0);
		assertEquals(2, preparedStatementMap1.size());
		assertEquals("test", preparedStatementMap1.get(1));
		assertEquals((short)2, preparedStatementMap1.get(2));
		MockParameterMap preparedStatementMap2 = parameterMap.get("insert into test (col1, col2, col3) values(?, ?, ?)").getParameterSet(0);
		assertEquals(1, preparedStatementMap2.size());
		assertTrue(Arrays.equals(new byte[]{1}, (byte[])preparedStatementMap2.get(1)));
		MockParameterMap preparedStatementMap3 = parameterMap.get("update mytable set test = test + ? where id = ?").getParameterSet(0);
		assertEquals(0, preparedStatementMap3.size());
		MockParameterMap callableStatementMap1 = parameterMap.get("{call getData(?, ?, ?, ?)}").getParameterSet(0);
		assertEquals(0, callableStatementMap1.size());
		MockParameterMap callableStatementMap2 = parameterMap.get("{call setData(?, ?, ?, ?)}").getParameterSet(0);
		assertEquals(1, callableStatementMap2.size());
		assertEquals(Boolean.FALSE, callableStatementMap2.get("name"));
    }
    
    @Test
    public void testGetExecutedSQLStatementParameterSets() throws Exception
    {
		preparePreparedStatements();
		prepareCallableStatements();
		module.getPreparedStatement(0).setString(1, "test");
		module.getPreparedStatement(0).setShort(2, (short)2);
		module.getPreparedStatement(1).setBytes(1, new byte[]{1});
		module.getCallableStatement(1).setBoolean("name", false);
		module.getPreparedStatement(0).execute();
		module.getPreparedStatement(1).execute();
		module.getPreparedStatement(2).execute();
		module.getCallableStatement(0).execute();
		module.getCallableStatement(1).execute();
		module.getPreparedStatement(0).setString(1, "test1");
		module.getPreparedStatement(0).setShort(2, (short)3);
		module.getPreparedStatement(0).execute();
		ParameterSets sets1 = module.getExecutedSQLStatementParameterSets("INSERT INTO TEST (COL1, COL2)");
		assertEquals(2, sets1.getNumberParameterSets());
		MockParameterMap parameterSet1 = sets1.getParameterSet(0);
		assertEquals(2, parameterSet1.size());
		assertEquals("test", parameterSet1.get(1));
		assertEquals((short)2, parameterSet1.get(2));
		MockParameterMap parameterSet2 = sets1.getParameterSet(1);
		assertEquals(2, parameterSet2.size());
		assertEquals("test1", parameterSet2.get(1));
		assertEquals((short)3, parameterSet2.get(2));
		module.setUseRegularExpressions(true);
		ParameterSets sets2 = module.getExecutedSQLStatementParameterSets("insert into test \\(col1, col2, col3\\) .*");
		assertEquals(1, sets2.getNumberParameterSets());
		parameterSet1 = sets2.getParameterSet(0);
		assertEquals(1, parameterSet1.size());
		assertTrue(Arrays.equals(new byte[]{1}, (byte[])parameterSet1.get(1)));
		ParameterSets sets3 = module.getExecutedSQLStatementParameterSets("{call setData\\(\\?, \\?, \\?, \\?\\)}");
		assertEquals(1, sets3.getNumberParameterSets());
		parameterSet1 = sets3.getParameterSet(0);
		assertEquals(1, parameterSet1.size());
		assertEquals(Boolean.FALSE, parameterSet1.get("name"));
		ParameterSets sets4 = module.getExecutedSQLStatementParameterSets("{call getData\\(\\?, \\?, \\?, \\?\\)}");
		assertEquals(1, sets4.getNumberParameterSets());
		parameterSet1 = sets4.getParameterSet(0);
		assertEquals(0, parameterSet1.size());
		assertNull(module.getExecutedSQLStatementParameterSets("{call xyz"));
    }
    
    @Test
    public void testSQLStatementParameterNoParameterSets() throws Exception
    {
        prepareStatements();
        module.getStatement(0).execute("test");
        try
        {
            module.verifySQLStatementParameterNumber("test", 0, 0);
            fail();
        }
        catch (VerifyFailedException exc)
        {
            //should throw exception
        }
        preparePreparedStatements();
        module.getPreparedStatement(0).execute();
        try
        {
            module.verifySQLStatementParameterNumber("INSERT INTO TEST (COL1, COL2) VALUES(?,", 1, 0);
            fail();
        }
        catch (VerifyFailedException exc)
        {
            //should throw exception
        }
        try
        {
            module.verifySQLStatementParameter("INSERT INTO TEST (COL1, COL2) VALUES(?,", 1, new MockParameterMap());
            fail();
        }
        catch (VerifyFailedException exc)
        {
            //should throw exception
        }
    }
    
    @Test
	public void testSQLStatementParameterNumber() throws Exception
	{
        preparePreparedStatements();
		prepareCallableStatements();
		module.getPreparedStatement(0).setString(1, "test");
		module.getPreparedStatement(0).setString(2, "test");
		module.getCallableStatement(0).setString("name", "test");
		module.getCallableStatement(1).setString(1, "test");
		module.getPreparedStatement(0).execute();
		module.getPreparedStatement(1).execute();
		module.getPreparedStatement(2).execute();
		module.getCallableStatement(0).execute();
		module.getCallableStatement(1).execute();
		module.verifySQLStatementParameterNumber("INSERT INTO TEST (COL1, COL2) VALUES(?,", 0, 2);
		module.verifySQLStatementParameterNumber("insert into test (col1, col2, col3) values(?, ?, ?)", 0, 0);
		module.verifySQLStatementParameterNumber("update mytable set test = test + ? where id = ?", 0, 0);
		module.verifySQLStatementParameterNumber("{call getData(?, ?, ?, ?)}", 0, 1);
		module.verifySQLStatementParameterNumber("{call setData(?, ", 0, 1);
		try
		{
			module.verifySQLStatementParameterNumber("{call getData(?, ?, ?, ?)}", 0, 3);
			fail();
		}
		catch (VerifyFailedException exc)
		{
            //should throw exception
		}
		try
		{
			module.verifySQLStatementParameterNumber("insert into test (col1, col2, col3) values(?, ?, ?)", 0, 1);
			fail();
		}
		catch (VerifyFailedException exc)
		{
			//should throw exception
		}
		try
		{
			module.verifySQLStatementParameterNumber("xyz", 0, 0);
			fail();
		}
		catch(VerifyFailedException exc)
		{
			//should throw exception
		}
	}
	
    @Test
	public void testSQLStatementParameterPreparedStatement() throws Exception
	{
		preparePreparedStatements();
		module.getPreparedStatement(1).setString(1, "test1");
		module.getPreparedStatement(1).setInt(2, 3);
		module.getPreparedStatement(0).execute();
		module.getPreparedStatement(1).execute();
		module.getPreparedStatement(2).execute();
		MockParameterMap emptyMap = new MockParameterMap();
		MockParameterMap okTestMap = new MockParameterMap();
		okTestMap.put(1, "test1");
		okTestMap.put(2, 3);
		MockParameterMap failureTestMap1 = new MockParameterMap();
		failureTestMap1.put(1, "test1");
		failureTestMap1.put(2, 2);
		MockParameterMap failureTestMap2 = new MockParameterMap();
		failureTestMap2.put(1, "test1");
		failureTestMap2.put(2, 3);
		failureTestMap2.put(3, 3);
		module.verifySQLStatementParameter("update mytable set test = test", 0, emptyMap);
		try
		{
		    module.setUseRegularExpressions(true);
		    module.verifySQLStatementParameter("update mytable set test = test", 0, emptyMap);
			fail();
		}
		catch(VerifyFailedException exc)
		{
			//should throw exception
		}
		module.verifySQLStatementParameter("update mytable set test = test.*", 0, emptyMap);
		module.setUseRegularExpressions(false);
		module.verifySQLStatementParameter("insert into test (col1, col2, col3)", 0, okTestMap);
        module.verifySQLStatementParameter("insert into test (col1, col2, col3)", 0, 2, 3);
		try
		{
			module.verifySQLStatementParameter("insert into test (col1, col2, col3) values(?, ?, ?)", 0, failureTestMap1);
			fail();
		}
		catch(VerifyFailedException exc)
		{
			//should throw exception
		}
        try
        {
            module.verifySQLStatementParameter("insert into test (col1, col2, col3) values(?, ?, ?)", 0, 1, "test2");
            fail();
        }
        catch(VerifyFailedException exc)
        {
            //should throw exception
        }
		try
		{
			module.verifySQLStatementParameter("insert into test (col1, col2, col3) values(?, ?, ?)", 0, failureTestMap2);
			fail();
		}
		catch(VerifyFailedException exc)
		{
			//should throw exception
		}
		try
		{
			module.verifySQLStatementParameter("INSERT INTO TEST (COL1, COL2) VALUES(?, ?)", 0, okTestMap);
			fail();
		}
		catch(VerifyFailedException exc)
		{
			//should throw exception
		}
	}
    
    @Test
    public void testSQLStatementNullParameterPreparedStatement() throws Exception
    {
        preparePreparedStatements();
        module.getPreparedStatement(0).setString(1, null);
        module.getPreparedStatement(0).execute();
        module.verifySQLStatementParameter("INSERT INTO TEST (COL1, COL2) VALUES(?, ?)", 0, 1, null);
        try
        {
            module.verifySQLStatementParameter("INSERT INTO TEST (COL1, COL2) VALUES(?, ?)", 0, 1, "test");
            fail();
        }
        catch(VerifyFailedException exc)
        {
            //should throw exception
        }
    }
	
    @Test
	public void testSQLStatementParameterCallableStatement() throws Exception
	{
		prepareCallableStatements();
		module.getCallableStatement(0).setString(1, "test1");
		module.getCallableStatement(0).setBytes(2, new byte[] {1});
		module.getCallableStatement(0).setInt("name", 1);
		module.getCallableStatement(0).execute();
		module.getCallableStatement(1).execute();
		module.verifySQLStatementParameter("{call getData(?, ?", 0, 1, "test1");
		module.setUseRegularExpressions(true);
		module.verifySQLStatementParameter(".*getData\\(\\?, \\?.*", 0, 1, "test1");
		module.setUseRegularExpressions(false);
		try
        {
		    module.verifySQLStatementParameter(".*getData\\(\\?, \\?.*", 0, 1, "test1");
            fail();
        } 
		catch(VerifyFailedException exc)
        {
		    //should throw exception
        }
		module.verifySQLStatementParameter("{call getData(?, ?, ?, ?)}", 0, 2, new byte[] {1});
		module.verifySQLStatementParameter("{call getData(?, ?, ?, ?)}", 0, "name", 1);
		try
		{
			module.verifySQLStatementParameter("{call getData(?, ?, ?, ?)}", 0, 2, new byte[] {1, 2});
			fail();
		}
		catch(VerifyFailedException exc)
		{
		    //should throw exception
		}
		try
		{
			module.verifySQLStatementParameter("{call setData(?, ?, ?, ?)}", 0, 1, "");
			fail();
		}
		catch(VerifyFailedException exc)
		{
			//should throw exception
		}
		try
		{
			module.verifySQLStatementParameter("select", 0, 1, "");
			fail();
		}
		catch(VerifyFailedException exc)
		{
			//should throw exception
		}
		module.setCaseSensitive(true);
		try
		{
			module.verifySQLStatementParameter("{CALL getData(?, ?", 0, 1, "test1");
			fail();
		}
		catch(VerifyFailedException exc)
		{
			//should throw exception
		}
	}
    
    @Test
    public void testSQLStatementNullParameterCallableStatement() throws Exception
    {
        prepareCallableStatements();
        module.getCallableStatement(0).setString("1", null);
        module.getCallableStatement(0).execute();
        module.verifySQLStatementParameter("{call getData(?, ?, ?, ?)}", 0, "1", null);
        try
        {
            module.verifySQLStatementParameter("{call getData(?, ?, ?, ?)}", 0, "1", "test");
            fail();
        }
        catch(VerifyFailedException exc)
        {
            //should throw exception
        }
    }
	
    @Test
	public void testSQLStatementParameterMultipleParameterSets() throws Exception
	{
		prepareCallableStatements();
		module.getCallableStatement(0).setString(1, "test1");
		module.getCallableStatement(0).execute();
		module.getCallableStatement(0).setString(1, "xyz");
		module.getCallableStatement(0).setBoolean("name", true);
		module.getCallableStatement(0).execute();
		module.getCallableStatement(0).execute();
		module.verifySQLStatementParameterNumber("{call getData(?, ?, ?, ?)}", 0, 1);
		module.verifySQLStatementParameterNumber("{call getData(?, ?, ?, ?)}", 1, 2);
		module.verifySQLStatementParameterNumber("{call getData(?, ?, ?, ?)}", 2, 2);
		module.setUseRegularExpressions(true);
		module.verifySQLStatementParameterNumber("{call getData\\(\\?, \\?, \\?, \\?\\)}", 2, 2);
		module.verifySQLStatementParameterNumber(".call getData.*}", 2, 2);
		module.setUseRegularExpressions(false);
		module.verifySQLStatementParameter("{call getData(?, ?, ?, ?)}", 1, 1, "xyz");
		module.verifySQLStatementParameter("{call getData(?, ?, ?, ?)}", 1, "name", Boolean.TRUE);
		MockParameterMap testMap = new MockParameterMap();
		testMap.put(1, "test1");
		module.verifySQLStatementParameter("{call getData(?, ?, ?, ?)}", 0, testMap);
		try
		{
			module.verifySQLStatementParameterNumber("{call getData(?, ?, ?, ?)}", 2, 0);
			fail();
		}
		catch (VerifyFailedException exc)
		{
			//should throw exception
		}
		try
		{
			module.verifySQLStatementParameter("{call getData(?, ?, ?, ?)}", 0, new MockParameterMap());
			fail();
		}
		catch (VerifyFailedException exc)
		{
			//should throw exception
		}
	}
    
    @Test
  public void testSQLStatementParameterMockParameterMapWithNullValues() throws Exception
  {
    prepareCallableStatements();
    module.getCallableStatement(0).setString(1, "test1");
    module.getCallableStatement(0).setString(2, null);
    module.getCallableStatement(0).setBoolean(3, Boolean.TRUE);
    module.getCallableStatement(0).setBigDecimal(4, BigDecimal.ZERO);
    module.getCallableStatement(0).execute();
    MockParameterMap testMap = new MockParameterMap();
    testMap.put(1, "test1");
    testMap.put(2, null);
    testMap.put(3, Boolean.TRUE);
    testMap.put(4,  BigDecimal.ZERO);
    module.verifySQLStatementParameter("{call getData(?, ?, ?, ?)}", 0, testMap);
    module.verifySQLStatementParameter("{call getData(?, ?, ?, ?)}", 0, 1, "test1");
    module.verifySQLStatementParameter("{call getData(?, ?, ?, ?)}", 0, 2, null);
    module.verifySQLStatementParameter("{call getData(?, ?, ?, ?)}", 0, 3, Boolean.TRUE);
    module.verifySQLStatementParameter("{call getData(?, ?, ?, ?)}", 0, 4, BigDecimal.ZERO);
  }
    
    @Test
    public void testSQLStatementParameterPreparedStatementBatchParameterSets() throws Exception
    {
        MockPreparedStatement preparedStatement = (MockPreparedStatement)mockfactory.getMockConnection().prepareStatement("insert into test");
        preparedStatement.setString(1, "test1");
        preparedStatement.setInt(2, 3);
        preparedStatement.addBatch();
        preparedStatement.setString(1, "test2");
        preparedStatement.setInt(2, 4);
        preparedStatement.addBatch();
        preparedStatement.executeBatch();
        module.verifySQLStatementParameter("insert into test", 0, 1, "test1");
        module.verifySQLStatementParameter("insert into test", 0, 2, 3);
        module.verifySQLStatementParameter("insert into test", 1, 1, "test2");
        module.verifySQLStatementParameter("insert into test", 1, 2, 4);
        MockParameterMap testMap = new MockParameterMap();
        testMap.put(1, "test1");
        testMap.put(2, 3);
        module.verifySQLStatementParameter("insert into test", 0, testMap);
        testMap = new MockParameterMap();
        testMap.put(1, "test2");
        testMap.put(2, 4);
        module.verifySQLStatementParameter("insert into test", 1, testMap);
    }
    
    @Test
    public void testSQLStatementParameterCallableStatementBatchParameterSets() throws Exception
    {
        MockCallableStatement callableStatement = (MockCallableStatement)mockfactory.getMockConnection().prepareCall("call getData");
        callableStatement.setString("xyz1", "test1");
        callableStatement.setLong(1, 3);
        callableStatement.addBatch();
        callableStatement.setString(1, "test2");
        callableStatement.setInt("xyz1", 4);
        callableStatement.setInt("xyz2", 7);
        callableStatement.addBatch();
        callableStatement.executeBatch();
        module.verifySQLStatementParameter("call getData", 0, "xyz1", "test1");
        module.verifySQLStatementParameter("call getData", 0, 1, 3L);
        module.verifySQLStatementParameter("call getData", 1, "xyz1", 4);
        module.verifySQLStatementParameter("call getData", 1, "xyz2", 7);
        module.verifySQLStatementParameter("call getData", 1, 1, "test2");
        MockParameterMap testMap = new MockParameterMap();
        testMap.put("xyz1", "test1");
        testMap.put(1, 3L);
        module.verifySQLStatementParameter("call getData", 0, testMap);
        testMap = new MockParameterMap();
        testMap.put("xyz1", 4);
        testMap.put("xyz2", 7);
        testMap.put(1, "test2");
        module.verifySQLStatementParameter("call getData", 1, testMap);
    }
    
    @Test
    public void testPreparedStatementsAndCallableStatementsSQLOrdered() throws Exception
    {
        preparePreparedStatements();
        MockPreparedStatement preparedStatement = module.getPreparedStatement("insert");
        assertSame(module.getPreparedStatementResultSetHandler().getPreparedStatements().get(0), preparedStatement);
        prepareCallableStatements();
        MockPreparedStatement callableStatement = module.getCallableStatement("call");
        assertSame(module.getCallableStatementResultSetHandler().getCallableStatements().get(0), callableStatement);
    }
}
