package com.mockrunner.jdbc;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.mockrunner.base.NestedApplicationException;
import com.mockrunner.base.VerifyFailedException;
import com.mockrunner.mock.jdbc.JDBCMockObjectFactory;
import com.mockrunner.mock.jdbc.MockCallableStatement;
import com.mockrunner.mock.jdbc.MockParameterMap;
import com.mockrunner.mock.jdbc.MockPreparedStatement;
import com.mockrunner.mock.jdbc.MockResultSet;
import com.mockrunner.mock.jdbc.MockSavepoint;
import com.mockrunner.mock.jdbc.MockStatement;
import com.mockrunner.mock.jdbc.ParameterIndex;
import com.mockrunner.mock.jdbc.ParameterName;
import com.mockrunner.mock.jdbc.ParameterReference;
import com.mockrunner.util.common.StringUtil;

/**
 * Module for JDBC tests.
 */
public class JDBCTestModule
{
    private final JDBCMockObjectFactory mockFactory;
    private boolean caseSensitive = false;
    private boolean exactMatch = false;
    private boolean useRegularExpressions = false;
      
    public JDBCTestModule(JDBCMockObjectFactory mockFactory)
    {
        this.mockFactory = mockFactory;
    }
    
    /**
     * Set if specified SQL statements should be handled case sensitive.
     * Defaults to to <code>false</code>, i.e. <i>INSERT</i> is the same
     * as <i>insert</i>. 
     * Please note that this method controls SQL statement
     * matching for this class, e.g. what statements the method
     * {@link #getPreparedStatements(String)} returns or what statements
     * are taken into account by the method {@link #verifySQLStatementExecuted(String)}.
     * In contrast to {@link AbstractResultSetHandler#setCaseSensitive(boolean)} it does 
     * not control the prepared results that are returned when the tested application
     * executes a matching statement.
     * @param caseSensitive enable or disable case sensitivity
     */
    public void setCaseSensitive(boolean caseSensitive)
    {
        this.caseSensitive = caseSensitive;
    }
    
    /**
     * Set if specified SQL statements must match exactly.
     * Defaults to <code>false</code>, i.e. the SQL statement used
     * to create a <code>PreparedStatement</code> must contain
     * the specified SQL statement, but does not have to match
     * exactly. If the original statement is <i>insert into mytable values(?, ?, ?)</i>
     * <code>verifyPreparedStatementPresent("insert into mytable")</code>
     * will pass.
     * Please note that this method controls SQL statement
     * matching for this class, e.g. what statements the method
     * {@link #getPreparedStatements(String)} returns or what statements
     * are taken into account by the method {@link #verifySQLStatementExecuted(String)}.
     * In contrast to {@link AbstractResultSetHandler#setExactMatch(boolean)} it does 
     * not control the prepared results that are returned when the tested application
     * executes a matching statement.
     * @param exactMatch enable or disable exact matching
     */
    public void setExactMatch(boolean exactMatch)
    {
        this.exactMatch = exactMatch;
    }
    
    /**
     * Set if regular expressions should be used when matching
     * SQL statements. Irrelevant if <code>exactMatch</code> is
     * <code>true</code>. Default is <code>false</code>, i.e. you
     * cannot use regular expressions and matching is based
     * on string comparison.
     * Please note that this method controls SQL statement
     * matching for the methods of this class, e.g. what statements the method
     * {@link #getPreparedStatements(String)} returns or what statements
     * are taken into account by the method {@link #verifySQLStatementExecuted(String)}.
     * In contrast to {@link AbstractResultSetHandler#setUseRegularExpressions(boolean)} it does 
     * not control the prepared results that are returned when the tested application
     * executes a matching statement.
     * @param useRegularExpressions should regular expressions be used
     */
    public void setUseRegularExpressions(boolean useRegularExpressions)
    {
        this.useRegularExpressions = useRegularExpressions;
    }
    
    /**
     * Returns the {@link StatementResultSetHandler}. 
     * The {@link StatementResultSetHandler}
     * contains methods that can be used to specify the 
     * {@link com.mockrunner.mock.jdbc.MockResultSet} objects
     * and update counts that a {@link com.mockrunner.mock.jdbc.MockStatement} 
     * should return when executing an SQL statement.
     * @return the {@link StatementResultSetHandler}
     */
    public StatementResultSetHandler getStatementResultSetHandler()
    {
        return mockFactory.getMockConnection().getStatementResultSetHandler();
    }
    
    /**
     * Returns the {@link PreparedStatementResultSetHandler}.
     * The {@link PreparedStatementResultSetHandler}
     * contains methods that can be used to specify the 
     * {@link com.mockrunner.mock.jdbc.MockResultSet} objects
     * and update counts that a {@link com.mockrunner.mock.jdbc.MockPreparedStatement} 
     * should return when executing an SQL statement.
     * @return the {@link PreparedStatementResultSetHandler}
     */
    public PreparedStatementResultSetHandler getPreparedStatementResultSetHandler()
    {
        return mockFactory.getMockConnection().getPreparedStatementResultSetHandler();
    }
    
    /**
     * Returns the {@link CallableStatementResultSetHandler}.
     * The {@link CallableStatementResultSetHandler}
     * contains methods that can be used to specify the 
     * {@link com.mockrunner.mock.jdbc.MockResultSet} objects
     * and update counts that a {@link com.mockrunner.mock.jdbc.MockCallableStatement} 
     * should return when executing an SQL statement.
     * @return the {@link CallableStatementResultSetHandler}
     */
    public CallableStatementResultSetHandler getCallableStatementResultSetHandler()
    {
        return mockFactory.getMockConnection().getCallableStatementResultSetHandler();
    }
    
    /**
     * Returns a {@link com.mockrunner.mock.jdbc.MockStatement} by its index.
     * @param index the index of the <code>Statement</code>
     * @return the <code>Statement</code> or <code>null</code>, if there is no such
     *         <code>Statement</code>
     */
    public MockStatement getStatement(int index)
    {
        List<MockStatement> statements = getStatements();
        if(index < statements.size()) return statements.get(index);
        return null;
    }
    
    /**
     * Returns all {@link com.mockrunner.mock.jdbc.MockStatement} objects.
     * @return the <code>List</code> of <code>Statement</code> objects
     */
    public List<MockStatement> getStatements()
    {
        return mockFactory.getMockConnection().getStatementResultSetHandler().getStatements();
    }
    
    /**
     * Returns a <code>List</code> of all SQL statements that were executed
     * by calling an <code>execute</code> method of a {@link com.mockrunner.mock.jdbc.MockStatement},
     * {@link com.mockrunner.mock.jdbc.MockPreparedStatement} or
     * {@link com.mockrunner.mock.jdbc.MockCallableStatement}.
     * @return the <code>List</code> of SQL statements
     */
    public List<String> getExecutedSQLStatements()
    {
        List<String> list = new ArrayList<String>();
        list.addAll(mockFactory.getMockConnection().getStatementResultSetHandler().getExecutedStatements());
        list.addAll(mockFactory.getMockConnection().getPreparedStatementResultSetHandler().getExecutedStatements());
        list.addAll(mockFactory.getMockConnection().getCallableStatementResultSetHandler().getExecutedStatements());
        return list;
    }
    
    /**
     * @deprecated use {@link #getExecutedSQLStatementParameterMap}
     */
    @Deprecated
    public Map<String, ParameterSets> getExecutedSQLStatementParameter()
    {
        return getExecutedSQLStatementParameterMap();
    }
    
    /**
     * Returns a <code>Map</code> of all parameters that were used when
     * executing a {@link com.mockrunner.mock.jdbc.MockPreparedStatement} or
     * {@link com.mockrunner.mock.jdbc.MockCallableStatement}.
     * The keys are the corresponding SQL statements. The values are the 
     * {@link ParameterSets} objects.
     * @return the <code>Map</code> of parameters
     */
    public Map<String, ParameterSets> getExecutedSQLStatementParameterMap()
    {
        Map<String, ParameterSets> map = new TreeMap<String, ParameterSets>();
        map.putAll(mockFactory.getMockConnection().getPreparedStatementResultSetHandler().getExecutedStatementParameterMap());
        map.putAll(mockFactory.getMockConnection().getCallableStatementResultSetHandler().getExecutedStatementParameterMap());
        return map;
    }
    
    /**
     * Returns the {@link ParameterSets} object for the specified SQL statement.
     * If more than one {@link ParameterSets} object is found, the first one
     * will be returned.
     * @param sql the the SQL statement
     * @return the {@link ParameterSets} object or <code>null</code> if no
     *         matching object is found
     */
    public ParameterSets getExecutedSQLStatementParameterSets(String sql)
    {
        Map<String, ParameterSets> map = getExecutedSQLStatementParameterMap();
        SQLStatementMatcher matcher = new SQLStatementMatcher(caseSensitive, exactMatch, useRegularExpressions);
        List<ParameterSets> list = matcher.getMatchingObjects(map, sql, false);
        if(list != null && list.size() > 0)
        {
            return list.get(0);
        }
        return null;
    }
    
    /**
     * Returns the <code>ResultSet</code> objects with the specified id. 
     * If there are more than one <code>ResultSet</code> objects with the
     * specified id, the first one is returned. If there is no
     * <code>ResultSet</code> with the specified id, this method
     * returns <code>null</code>.
     * Please also see {@link #getReturnedResultSets(String)}.
     * @return the <code>ResultSet</code> with the specified id
     */
    public MockResultSet getReturnedResultSet(String id)
    {
        List<MockResultSet> list = getReturnedResultSets(id);
        if(list != null && list.size() > 0)
        {
            return list.get(0);
        }
        return null;
    }
    
    /**
     * Returns a <code>List</code> of the <code>ResultSet</code> objects with
     * the specified id. In contrast to {@link #getReturnedResultSets}, the
     * returned <code>List</code> never contains <code>ResultSet[]</code> objects,
     * only single result sets. If {@link #getReturnedResultSets} returns an
     * array, every <code>ResultSet</code> is checked and added to the returned <code>List</code>,
     * if the id matches.
     * Please note that <code>ResultSet</code> objects are cloned when executing 
     * statements. The <code>ResultSet</code> objects in the <code>List</code>
     * returned by this method are really the instances the statement returned
     * and not the instances you have used when preparing them.
     * @return the <code>List</code> of <code>ResultSet</code> objects
     */
    public List<MockResultSet> getReturnedResultSets(String id)
    {
        List<MockResultSet[]> list = getReturnedResultSets();
        List<MockResultSet> resultList = new ArrayList<MockResultSet>();
        for (MockResultSet[] resultSets : list) {
            for (MockResultSet resultSet : resultSets) {
                if(null != id  && id.equals(resultSet.getId())){
                    resultList.add(resultSet);
                }
            }
        }
        return resultList;
    }
    
    /**
     * Returns a <code>List</code> of all <code>ResultSet</code> objects that were returned
     * by calling an <code>executeQuery</code> method of a {@link com.mockrunner.mock.jdbc.MockStatement},
     * {@link com.mockrunner.mock.jdbc.MockPreparedStatement} or
     * {@link com.mockrunner.mock.jdbc.MockCallableStatement}.
     * Please note that the <code>List</code> may contain arrays of <code>ResultSet</code> objects,
     * if a query returned multiple result sets. The <code>List</code> may
     * contain <code>ResultSet</code> objects and <code>ResultSet[]</code> objects.
     * If a query returned multiple result sets, the list will always contain
     * the full array of <code>ResultSet</code> objects that were prepared, even
     * if {@link com.mockrunner.mock.jdbc.MockStatement#getMoreResults()} was
     * not called for all the result sets.
     * Please note that <code>ResultSet</code> objects are cloned when executing 
     * statements. The <code>ResultSet</code> objects in the <code>List</code>
     * returned by this method are really the instances the statement returned
     * and not the instances you have used when preparing them.
     * @return the <code>List</code> of <code>ResultSet</code> or <code>ResultSet[]</code> objects
     */
    public List<MockResultSet[]> getReturnedResultSets()
    {
        List<MockResultSet[]> list = new ArrayList<MockResultSet[]>();
        list.addAll(mockFactory.getMockConnection().getStatementResultSetHandler().getReturnedResultSets());
        list.addAll(mockFactory.getMockConnection().getPreparedStatementResultSetHandler().getReturnedResultSets());
        list.addAll(mockFactory.getMockConnection().getCallableStatementResultSetHandler().getReturnedResultSets());
        return list;
    }
    
    /**
     * Returns a {@link com.mockrunner.mock.jdbc.MockPreparedStatement} that was 
     * created using a {@link com.mockrunner.mock.jdbc.MockConnection} by its index.
     * @param index the index of the <code>PreparedStatement</code>
     * @return the <code>PreparedStatement</code> or <code>null</code>, if there is no such
     *         <code>PreparedStatement</code>
     */
    public MockPreparedStatement getPreparedStatement(int index)
    {
        List<MockPreparedStatement> statements = getPreparedStatements();
        if(index < statements.size()) return statements.get(index);
        return null;
    }
    
    /**
     * Returns a {@link com.mockrunner.mock.jdbc.MockPreparedStatement} that was 
     * created using a {@link com.mockrunner.mock.jdbc.MockConnection} by its SQL statement.
     * If there are more than one {@link com.mockrunner.mock.jdbc.MockPreparedStatement}
     * objects with the specified SQL, the first one will be returned.
     * Please note that you can modify the search parameters with 
     * {@link #setCaseSensitive}, {@link #setExactMatch} and {@link #setUseRegularExpressions}.
     * @param sql the SQL statement used to create the <code>PreparedStatement</code>
     * @return the <code>PreparedStatement</code> or <code>null</code>, if there is no macth
     */
    public MockPreparedStatement getPreparedStatement(String sql)
    {
        List<MockPreparedStatement> list = getPreparedStatements(sql);
        if(null != list && list.size() > 0)
        {
            return list.get(0);
        }
        return null;
    }
    
    /**
     * Returns all {@link com.mockrunner.mock.jdbc.MockPreparedStatement} objects.
     * @return the <code>List</code> of <code>PreparedStatement</code> objects
     */
    public List<MockPreparedStatement> getPreparedStatements()
    {
        return mockFactory.getMockConnection().getPreparedStatementResultSetHandler().getPreparedStatements();
    }
    
    /**
     * Returns all {@link com.mockrunner.mock.jdbc.MockPreparedStatement} objects with
     * the specified SQL statement as a <code>List</code>. If there are no matches, an empty
     * <code>List</code> will be returned.
     * Please note that you can modify the search parameters with 
     * {@link #setCaseSensitive}, {@link #setExactMatch} and {@link #setUseRegularExpressions}.
     * @param sql the SQL statement used to create the <code>PreparedStatement</code>
     * @return the <code>List</code> of <code>PreparedStatement</code> objects
     */
    public List<MockPreparedStatement> getPreparedStatements(String sql)
    {
        Map<String, List<MockPreparedStatement>> sqlStatements = mockFactory.getMockConnection().getPreparedStatementResultSetHandler().getPreparedStatementMap();
        SQLStatementMatcher matcher = new SQLStatementMatcher(caseSensitive, exactMatch, useRegularExpressions);
        return matcher.getMatchingObjectsFromCollections(sqlStatements, sql, false); 
    }
    
    /**
     * Returns a {@link com.mockrunner.mock.jdbc.MockCallableStatement} that was 
     * created using a {@link com.mockrunner.mock.jdbc.MockConnection} by its index.
     * @param index the index of the <code>CallableStatement</code>
     * @return the <code>CallableStatement</code> or <code>null</code>, if there is no such
     *         <code>CallableStatement</code>
     */
    public MockCallableStatement getCallableStatement(int index)
    {
        List<MockCallableStatement> statements = getCallableStatements();
        if(index < statements.size()) return statements.get(index);
        return null;
    }
    
    /**
     * Returns a {@link com.mockrunner.mock.jdbc.MockCallableStatement} that was 
     * created using a {@link com.mockrunner.mock.jdbc.MockConnection} by its SQL statement.
     * If there are more than one {@link com.mockrunner.mock.jdbc.MockCallableStatement}
     * objects with the specified SQL, the first one will be returned.
     * Please note that you can modify the search parameters with 
     * {@link #setCaseSensitive}, {@link #setExactMatch} and {@link #setUseRegularExpressions}.
     * @param sql the SQL statement used to create the <code>CallableStatement</code>
     * @return the <code>CallableStatement</code> or <code>null</code>, if there is no macth
     */
    public MockCallableStatement getCallableStatement(String sql)
    {
        List<MockCallableStatement> list = getCallableStatements(sql);
        if(null != list && list.size() > 0)
        {
            return list.get(0);
        }
        return null;
    }
    
    /**
     * Returns all {@link com.mockrunner.mock.jdbc.MockCallableStatement} objects.
     * @return the <code>List</code> of <code>CallableStatement</code> objects
     */
    public List<MockCallableStatement> getCallableStatements()
    {
        return mockFactory.getMockConnection().getCallableStatementResultSetHandler().getCallableStatements();
    }
    
    /**
     * Returns all {@link com.mockrunner.mock.jdbc.MockCallableStatement} objects with
     * the specified SQL statement as a <code>List</code>. 
     * If there are no matches, an empty <code>List</code> will be returned. 
     * Please note that you can modify the search parameters with 
     * {@link #setCaseSensitive}, {@link #setExactMatch} and {@link #setUseRegularExpressions}.
     * @param sql the SQL statement used to create the <code>CallableStatement</code>
     * @return the <code>List</code> of <code>CallableStatement</code> objects
     */
    public List<MockCallableStatement> getCallableStatements(String sql)
    {
        Map<String, List<MockCallableStatement>> sqlStatements = mockFactory.getMockConnection().getCallableStatementResultSetHandler().getCallableStatementMap();
        SQLStatementMatcher matcher = new SQLStatementMatcher(caseSensitive, exactMatch, useRegularExpressions);
        return matcher.getMatchingObjectsFromCollections(sqlStatements, sql, false); 
    }
    
    /**
     * Returns a parameter that was added to a <code>PreparedStatement</code>
     * using its <code>set</code> methods. Simple data types are returned as
     * the corresponsing wrapper type.
     * @param statement the <code>PreparedStatement</code>
     * @param indexOfParameter the index used to set the parameter
     * @return the corresponding object
     */
    public Object getPreparedStatementParameter(PreparedStatement statement, int indexOfParameter)
    {
        if(null == statement) return null;
        return ((MockPreparedStatement)statement).getParameter(indexOfParameter);
    }
    
    /**
     * Returns a parameter that was added to a <code>PreparedStatement</code>
     * using its <code>set</code> methods. Uses the first <code>PreparedStatement</code>
     * with the specified SQL statement. Simple data types are returned as
     * the corresponsing wrapper type.
     * @param sql the SQL statement
     * @param indexOfParameter the index used to set the object
     * @return the corresponding object
     */
    public Object getPreparedStatementParameter(String sql, int indexOfParameter)
    {
        return getPreparedStatementParameter(getPreparedStatement(sql), indexOfParameter);
    }
    
    /**
     * Returns an object that was added to a <code>PreparedStatement</code>
     * using its <code>set</code> methods. Simple data types are returned as
     * the corresponsing wrapper type.
     * @param indexOfStatement the index of the statement
     * @param indexOfParameter the index used to set the object
     * @return the corresponding object
     */
    public Object getPreparedStatementParameter(int indexOfStatement, int indexOfParameter)
    {
        return getPreparedStatementParameter(getPreparedStatement(indexOfStatement), indexOfParameter);
    }
    
    /**
     * Returns a parameter that was added to a <code>CallableStatement</code>
     * using its <code>set</code> methods. Simple data types are returned as
     * the corresponsing wrapper type.
     * @param statement the <code>CallableStatement</code>
     * @param indexOfParameter the index used to set the parameter
     * @return the corresponding object
     */
    public Object getCallableStatementParameter(CallableStatement statement, int indexOfParameter)
    {
        if(null == statement) return null;
        return ((MockCallableStatement)statement).getParameter(indexOfParameter);
    }

    /**
     * Returns a parameter that was added to a <code>CallableStatement</code>
     * using its <code>set</code> methods. Uses the first <code>CallableStatement</code>
     * with the specified SQL statement. Simple data types are returned as
     * the corresponsing wrapper type.
     * @param sql the SQL statement
     * @param indexOfParameter the index used to set the object
     * @return the corresponding object
     */
    public Object getCallableStatementParameter(String sql, int indexOfParameter)
    {
        return getCallableStatementParameter(getCallableStatement(sql), indexOfParameter);
    }

    /**
     * Returns an object that was added to a <code>CallableStatement</code>
     * using its <code>set</code> methods. Simple data types are returned as
     * the corresponsing wrapper type.
     * @param indexOfStatement the index of the statement
     * @param indexOfParameter the index used to set the object
     * @return the corresponding object
     */
    public Object getCallableStatementParameter(int indexOfStatement, int indexOfParameter)
    {
        return getCallableStatementParameter(getCallableStatement(indexOfStatement), indexOfParameter);
    }
    
    /**
     * Returns a parameter that was added to a <code>CallableStatement</code>
     * using its <code>set</code> methods.
     * @param statement the <code>CallableStatement</code>
     * @param nameOfParameter the name of the parameter
     * @return the corresponding object
     */
    public Object getCallableStatementParameter(CallableStatement statement, String nameOfParameter)
    {
        if(null == statement) return null;
        return ((MockCallableStatement)statement).getParameter(nameOfParameter);
    }

    /**
     * Returns a parameter that was added to a <code>CallableStatement</code>
     * using its <code>set</code> methods. Uses the first <code>CallableStatement</code>
     * with the specified SQL statement.
     * @param sql the SQL statement
     * @param nameOfParameter the name of the parameter
     * @return the corresponding object
     */
    public Object getCallableStatementParameter(String sql, String nameOfParameter)
    {
        return getCallableStatementParameter(getCallableStatement(sql), nameOfParameter);
    }

    /**
     * Returns an object that was added to a <code>CallableStatement</code>
     * using its <code>set</code> methods.
     * @param indexOfStatement the index of the statement
     * @param nameOfParameter the name of the parameter
     * @return the corresponding object
     */
    public Object getCallableStatementParameter(int indexOfStatement, String nameOfParameter)
    {
        return getCallableStatementParameter(getCallableStatement(indexOfStatement), nameOfParameter);
    }
    
    /**
     * Returns a list of all <code>Savepoint</code> objects.
     * @return the <code>List</code> of {@link com.mockrunner.mock.jdbc.MockSavepoint} objects
     */
    public List<MockSavepoint> getSavepoints()
    {
        return new ArrayList<MockSavepoint>(mockFactory.getMockConnection().getSavepointMap().values());
    }
    
    /**
     * Returns the <code>Savepoint</code> with the specified index.
     * The index is the number of the created <code>Savepoint</code>
     * starting with 0 for the first <code>Savepoint</code>.
     * @param index the index
     * @return the {@link com.mockrunner.mock.jdbc.MockSavepoint}
     */
    public MockSavepoint getSavepoint(int index)
    {
        List<MockSavepoint> savepoints = getSavepoints();
        for (MockSavepoint currentSavepoint : savepoints) {
            if(currentSavepoint.getNumber() == index) return currentSavepoint;
        }
        return null;
    }
    
    /**
     * Returns the first <code>Savepoint</code> with the specified name.
     * Unnamed <code>Savepoint</code> objects get the name <i>""</i>.
     * @param name the name
     * @return the {@link com.mockrunner.mock.jdbc.MockSavepoint}
     */
    public MockSavepoint getSavepoint(String name)
    {
        List<MockSavepoint> savepoints = getSavepoints();
        for (MockSavepoint currentSavepoint : savepoints) {
            try
            {
                if(currentSavepoint.getSavepointName().equals(name)) return currentSavepoint;
            }
            catch(SQLException exc) //never thrown
            {
                throw new NestedApplicationException(exc);
            }
        }
        return null;
    }
    
    /**
     * Verifies that an SQL statement was executed.
     * @param sql the expected SQL string
     * @throws VerifyFailedException if verification fails
     */
    public void verifySQLStatementExecuted(String sql)
    {
        SQLStatementMatcher matcher = new SQLStatementMatcher(caseSensitive, exactMatch, useRegularExpressions);
        if(!matcher.contains(getExecutedSQLStatements(), sql, false))
        {
            throw new VerifyFailedException("Statement " + sql + " not executed.");
        }
    }
    
    /**
     * Verifies that an SQL statement was not executed.
     * @param sql the SQL string
     * @throws VerifyFailedException if verification fails
     */
    public void verifySQLStatementNotExecuted(String sql)
    {
        SQLStatementMatcher matcher = new SQLStatementMatcher(caseSensitive, exactMatch, useRegularExpressions);
        if(matcher.contains(getExecutedSQLStatements(), sql, false))
        {
            throw new VerifyFailedException("Statement " + sql + " was executed.");
        }
    }
    
    /**
     * Verifies the number of parameters for the specified SQL statement.
     * If more than one SQL statement is found, this method uses the
     * first one. You can specify the index of the parameter set. If
     * if a <code>PreparedStatement</code> or <code>CallableStatement</code> 
     * is executed N times, it has N parameter sets. Each parameter set
     * can contain any number of parameters (possibly 0 parameters).
     * Ordinary statements do not have parameter sets, of course. If
     * the specified SQL has been executed by an ordinary statements,
     * a <code>VerifyFailedException</code> is thrown stating the reason.
     * @param sql the SQL string
     * @param indexOfParameterSet the number of the parameter set
     * @param number the expected number of parameters
     * @throws VerifyFailedException if verification fails
     */
    public void verifySQLStatementParameterNumber(String sql, int indexOfParameterSet, int number)
    {
        MockParameterMap actualParameterMap = verifyAndGetParametersForSQL(sql, indexOfParameterSet);
        if(actualParameterMap.size() != number)
        {
            throw new VerifyFailedException("Expected " + number + " parameter, actual " + actualParameterMap.size() + " parameter");
        }
    }
    
    /**
     * Verifies the parameters for the specified SQL statement.
     * If more than one SQL statement is found, this method uses the
     * first one. The parameter map must match in size and the
     * parameters must be equal (by comparing them with
     * {com.mockrunner.jdbc.ParameterUtil#compareParameter}).
     * You can specify the index of the parameter set. If
     * if a <code>PreparedStatement</code> or <code>CallableStatement</code> 
     * is executed N times, it has N parameter sets. Each parameter set
     * can contain any number of parameters (possibly 0 parameters).
     * Ordinary statements do not have parameter sets, of course. If
     * the specified SQL has been executed by an ordinary statements,
     * a <code>VerifyFailedException</code> is thrown stating the reason.
     * @param sql the SQL string
     * @param indexOfParameterSet the number of the parameter set
     * @param parameterMap the map of expected parameters
     * @throws VerifyFailedException if verification fails
     */
    public void verifySQLStatementParameter(String sql, int indexOfParameterSet, MockParameterMap parameterMap)
    {
        verifySQLStatementParameterNumber(sql, indexOfParameterSet, parameterMap.size());
        MockParameterMap actualParameterMap = verifyAndGetParametersForSQL(sql, indexOfParameterSet);
        
        for(Entry<ParameterReference, Object> expectedParam : parameterMap.entrySet()){
          Object nextExpectedParameter = expectedParam.getValue();
          if(!actualParameterMap.containsKey(expectedParam.getKey()))
          {
              throw new VerifyFailedException("No parameter " + expectedParam.getKey() + " found.");
          }
          Object nextActualParameter = actualParameterMap.get(expectedParam.getKey());
          if(!ParameterUtil.compareParameter(nextExpectedParameter, nextActualParameter))
          {
                throw new VerifyFailedException("Expected " + nextExpectedParameter + " for parameter " + expectedParam.getKey() + ", but was " + nextActualParameter);
          }
        }
    }
    
    /**
     * Verifies the parameter for the specified SQL statement.
     * If more than one SQL statement is found, this method uses the
     * first one.
     * You can specify the index of the parameter set. If
     * if a <code>PreparedStatement</code> or <code>CallableStatement</code> 
     * is executed N times, it has N parameter sets. Each parameter set
     * can contain any number of parameters (possibly 0 parameters).
     * Ordinary statements do not have parameter sets, of course. If
     * the specified SQL has been executed by an ordinary statements,
     * a <code>VerifyFailedException</code> is thrown stating the reason.
     * @param sql the SQL string
     * @param indexOfParameterSet the number of the parameter set
     * @param indexOfParameter the index of the parameter
     * @param expectedParameter the expected parameter
     * @throws VerifyFailedException if verification fails
     */
    public void verifySQLStatementParameter(String sql, int indexOfParameterSet, int indexOfParameter, Object expectedParameter)
    {
        MockParameterMap actualParameterMap = verifyAndGetParametersForSQL(sql, indexOfParameterSet);
        if(!actualParameterMap.contains(indexOfParameter))
        {
            throw new VerifyFailedException("No parameter " + indexOfParameter + " found.");
        }
        Object actualParameter = actualParameterMap.get(indexOfParameter);
        if(!ParameterUtil.compareParameter(expectedParameter, actualParameter))
        {
            throw new VerifyFailedException("Expected " + expectedParameter + " for parameter " + indexOfParameter + ", but was " + actualParameter);
        }
    }
    
    /**
     * Verifies the parameter for the specified SQL statement.
     * If more than one SQL statement is found, this method uses the
     * first one.
     * You can specify the index of the parameter set. If
     * if a <code>PreparedStatement</code> or <code>CallableStatement</code> 
     * is executed N times, it has N parameter sets. Each parameter set
     * can contain any number of parameters (possibly 0 parameters).
     * Ordinary statements do not have parameter sets, of course. If
     * the specified SQL has been executed by an ordinary statements,
     * a <code>VerifyFailedException</code> is thrown stating the reason.
     * @param sql the SQL string
     * @param indexOfParameterSet the number of the parameter set
     * @param nameOfParameter the name of the parameter
     * @param expectedParameter the expected parameter
     * @throws VerifyFailedException if verification fails
     */
    public void verifySQLStatementParameter(String sql, int indexOfParameterSet, String nameOfParameter, Object expectedParameter)
    {
        MockParameterMap actualParameterMap = verifyAndGetParametersForSQL(sql, indexOfParameterSet);
        if(!actualParameterMap.contains(nameOfParameter))
        {
            throw new VerifyFailedException("No parameter " + nameOfParameter + " found.");
        }
        Object actualParameter = actualParameterMap.get(nameOfParameter);
        if(!ParameterUtil.compareParameter(expectedParameter, actualParameter))
        {
            throw new VerifyFailedException("Expected " + expectedParameter + " for parameter " + nameOfParameter + ", but was " + actualParameter);
        }
    }

    private MockParameterMap verifyAndGetParametersForSQL(String sql, int indexOfParameterSet)
    {
        verifySQLStatementExecuted(sql);
        SQLStatementMatcher matcher = new SQLStatementMatcher(caseSensitive, exactMatch, useRegularExpressions);
        List<ParameterSets> matchingParameterList = matcher.getMatchingObjects(getExecutedSQLStatementParameterMap(), sql, false);
        if(null == matchingParameterList || matchingParameterList.isEmpty())
        {
            throw new VerifyFailedException("No parameter sets for SQL " + sql + " found. Maybe the SQL has been executed by a regular " +
                                            "statement instead of a prepared statement or callable statement.");
        }
        ParameterSets actualParameterSets = matchingParameterList.get(0);
        if(null == actualParameterSets || indexOfParameterSet >= actualParameterSets.getNumberParameterSets())
        {
            throw new VerifyFailedException("Statement " + sql + " has no parameter set with index " + indexOfParameterSet +
                                            ". Maybe it has been executed less than " + (indexOfParameterSet + 1) + " times.");
        }
        return actualParameterSets.getParameterSet(indexOfParameterSet);
    }
    
    /**
     * Verifies that the connection is closed.
     * @throws VerifyFailedException if verification fails
     */
    public void verifyConnectionClosed()
    {
        try
        {
            if(!mockFactory.getMockConnection().isClosed())
            {
                throw new VerifyFailedException("Connection not closed.");
            }
        }
        catch(SQLException exc)
        {
            throw new NestedApplicationException(exc);
        }
    }
    
    /**
     * Verifies that all statements, all prepared statements and
     * all callable statements are closed.
     * @throws VerifyFailedException if verification fails
     */
    public void verifyAllStatementsClosed()
    {
        List<MockStatement> statements = getStatements();
        for(int ii = 0; ii < statements.size(); ii++)
        {
            MockStatement statement = statements.get(ii);
            if(!statement.isClosed())
            {
                throw new VerifyFailedException("Statement with index " + ii + " not closed.");
            }
        }
        List<MockPreparedStatement> preparedStatements = getPreparedStatements();
        for(int ii = 0; ii < preparedStatements.size(); ii++)
        {
            MockPreparedStatement preparedStatement = preparedStatements.get(ii);
            if(!preparedStatement.isClosed())
            {
                throw new VerifyFailedException("Prepared statement with index " + ii + " (SQL " + preparedStatement.getSQL() + ") not closed.");
            }
        }
        List<MockCallableStatement> callableStatements = getCallableStatements();
        for(int ii = 0; ii < callableStatements.size(); ii++)
        {
            MockCallableStatement callableStatement = callableStatements.get(ii);
            if(!callableStatement.isClosed())
            {
                throw new VerifyFailedException("Callable statement with index " + ii + " (SQL " + callableStatement.getSQL() + ") not closed.");
            }
        }
    }
    
    /**
     * Verifies that the <code>ResultSet</code> with the
     * specified id is closed. Only recognizes <code>ResultSet</code>
     * objects that were actually returned when executing a statement
     * and that were explicitly closed. Implicit closed <code>ResultSet</code>
     * objects (when closing a statement) are not recognized.
     * @param id
     * @throws VerifyFailedException if verification fails
     */
    public void verifyResultSetClosed(String id)
    {
        MockResultSet resultSet = getReturnedResultSet(id);
        if(null == resultSet)
        {
            throw new VerifyFailedException("ResultSet with id " + id + " not present.");
        }
        if(!resultSet.isClosed())
        {
            throw new VerifyFailedException("ResultSet with id " + id + " not closed.");
        }
    }
    
    /**
     * Verifies that the specified row of a <code>ResultSet</code> was
     * inserted.
     * @param resultSet the <code>ResultSet</code>
     * @param number the number of the row
     * @throws VerifyFailedException if verification fails
     */
    public void verifyResultSetRowInserted(MockResultSet resultSet, int number)
    {
        if(!resultSet.rowInserted(number))
        {
            throw new VerifyFailedException("Row number " + number + " of ResultSet " + resultSet.getId() + " not inserted.");
        }
    }
    
    /**
     * Verifies that the specified row of a <code>ResultSet</code> was
     * inserted.
     * @param id the id of the <code>ResultSet</code>
     * @param number the number of the row
     * @throws VerifyFailedException if verification fails
     */
    public void verifyResultSetRowInserted(String id, int number)
    {
        MockResultSet resultSet = getReturnedResultSet(id);
        if(null == resultSet)
        {
            throw new VerifyFailedException("ResultSet with id " + id + " not present.");
        }
        verifyResultSetRowInserted(resultSet, number);
    }
    
    /**
     * Verifies that the specified row of a <code>ResultSet</code> was
     * not inserted.
     * @param resultSet the <code>ResultSet</code>
     * @param number the number of the row
     * @throws VerifyFailedException if verification fails
     */
    public void verifyResultSetRowNotInserted(MockResultSet resultSet, int number)
    {
        if(resultSet.rowInserted(number))
        {
            throw new VerifyFailedException("Row number " + number + " of ResultSet " + resultSet.getId() + " was inserted.");
        }
    }
    
    /**
     * Verifies that the specified row of a <code>ResultSet</code> was
     * not inserted.
     * @param id the id of the <code>ResultSet</code>
     * @param number the number of the row
     * @throws VerifyFailedException if verification fails
     */
    public void verifyResultSetRowNotInserted(String id, int number)
    {
        MockResultSet resultSet = getReturnedResultSet(id);
        if(null == resultSet)
        {
            throw new VerifyFailedException("ResultSet with id " + id + " not present.");
        }
        verifyResultSetRowNotInserted(resultSet, number);
    }
    
    /**
     * Verifies that the specified row of a <code>ResultSet</code> was
     * updated.
     * @param resultSet the <code>ResultSet</code>
     * @param number the number of the row
     * @throws VerifyFailedException if verification fails
     */
    public void verifyResultSetRowUpdated(MockResultSet resultSet, int number)
    {
        if(!resultSet.rowUpdated(number))
        {
            throw new VerifyFailedException("Row number " + number + " of ResultSet " + resultSet.getId() + " not updated.");
        }
    }
    
    /**
     * Verifies that the specified row of a <code>ResultSet</code> was
     * updated.
     * @param id the id of the <code>ResultSet</code>
     * @param number the number of the row
     * @throws VerifyFailedException if verification fails
     */
    public void verifyResultSetRowUpdated(String id, int number)
    {
        MockResultSet resultSet = getReturnedResultSet(id);
        if(null == resultSet)
        {
            throw new VerifyFailedException("ResultSet with id " + id + " not present.");
        }
        verifyResultSetRowUpdated(resultSet, number);
    }
    
    /**
     * Verifies that the specified row of a <code>ResultSet</code> was
     * not updated.
     * @param resultSet the <code>ResultSet</code>
     * @param number the number of the row
     * @throws VerifyFailedException if verification fails
     */
    public void verifyResultSetRowNotUpdated(MockResultSet resultSet, int number)
    {
        if(resultSet.rowUpdated(number))
        {
            throw new VerifyFailedException("Row number " + number + " of ResultSet " + resultSet.getId() + " was updated.");
        }
    }

    /**
     * Verifies that the specified row of a <code>ResultSet</code> was
     * not updated.
     * @param id the id of the <code>ResultSet</code>
     * @param number the number of the row
     * @throws VerifyFailedException if verification fails
     */
    public void verifyResultSetRowNotUpdated(String id, int number)
    {
        MockResultSet resultSet = getReturnedResultSet(id);
        if(null == resultSet)
        {
            throw new VerifyFailedException("ResultSet with id " + id + " not present.");
        }
        verifyResultSetRowNotUpdated(resultSet, number);
    }
    
    /**
     * Verifies that the specified row of a <code>ResultSet</code> was
     * deleted.
     * @param resultSet the <code>ResultSet</code>
     * @param number the number of the row
     * @throws VerifyFailedException if verification fails
     */
    public void verifyResultSetRowDeleted(MockResultSet resultSet, int number)
    {
        if(!resultSet.rowDeleted(number))
        {
            throw new VerifyFailedException("Row number " + number + " of ResultSet " + resultSet.getId() + " not deleted.");
        }
    }
    
    /**
     * Verifies that the specified row of a <code>ResultSet</code> was
     * deleted.
     * @param id the id of the <code>ResultSet</code>
     * @param number the number of the row
     * @throws VerifyFailedException if verification fails
     */
    public void verifyResultSetRowDeleted(String id, int number)
    {
        MockResultSet resultSet = getReturnedResultSet(id);
        if(null == resultSet)
        {
            throw new VerifyFailedException("ResultSet with id " + id + " not present.");
        }
        verifyResultSetRowDeleted(resultSet, number);
    }
    
    /**
     * Verifies that the specified row of a <code>ResultSet</code> was
     * not deleted.
     * @param resultSet the <code>ResultSet</code>
     * @param number the number of the row
     * @throws VerifyFailedException if verification fails
     */
    public void verifyResultSetRowNotDeleted(MockResultSet resultSet, int number)
    {
        if(resultSet.rowDeleted(number))
        {
            throw new VerifyFailedException("Row number " + number + " of ResultSet " + resultSet.getId() + " was deleted.");
        }
    }

    /**
     * Verifies that the specified row of a <code>ResultSet</code> was
     * not deleted.
     * @param id the id of the <code>ResultSet</code>
     * @param number the number of the row
     * @throws VerifyFailedException if verification fails
     */
    public void verifyResultSetRowNotDeleted(String id, int number)
    {
        MockResultSet resultSet = getReturnedResultSet(id);
        if(null == resultSet)
        {
            throw new VerifyFailedException("ResultSet with id " + id + " not present.");
        }
        verifyResultSetRowNotDeleted(resultSet, number);
    }
    
    /**
     * Verifies that all <code>ResultSet</code> objects are closed.
     * Only recognizes <code>ResultSet</code> objects that were actually 
     * returned when executing a statement and that were explicitly closed. 
     * Implicit closed <code>ResultSet</code> objects (when closing a statement) 
     * are not recognized.
     * @throws VerifyFailedException if verification fails
     */
    public void verifyAllResultSetsClosed()
    {
        List<MockResultSet[]> allResultSets = getReturnedResultSets();
        for (MockResultSet[] resultSets : allResultSets) {
            for (MockResultSet resultSet : resultSets) {
                throwExceptionIfNotClosed(resultSet);
            }
        }
    }

    private void throwExceptionIfNotClosed(MockResultSet resultSet)
    {
        if(!resultSet.isClosed())
        {
            throw new VerifyFailedException("ResultSet with id " + resultSet.getId() + " not closed.");
        }
    }
    
    /**
     * Verifies that the changes were commited, i.e. the <code>commit</code>
     * method of <code>Connection</code> was at least called once.
     * Makes only sense, if the <code>Connection</code> is not in
     * autocommit mode. Automatic commits are not recognized.
     * @throws VerifyFailedException if verification fails
     */
    public void verifyCommitted()
    {
        int number = mockFactory.getMockConnection().getNumberCommits();
        if(number <= 0)
        {
            throw new VerifyFailedException("Connection received no commits.");
        }
    }
    
    /**
     * Verifies that the changes were not commited.
     * Makes only sense, if the <code>Connection</code> is not in
     * autocommit mode. Automatic commits are not recognized.
     * @throws VerifyFailedException if verification fails
     */
    public void verifyNotCommitted()
    {
        int number = mockFactory.getMockConnection().getNumberCommits();
        if(number > 0)
        {
            throw new VerifyFailedException("Connection was committed");
        }
    }
    
    /**
     * Verifies that the changes were rolled back, i.e. the <code>rollback</code>
     * method of <code>Connection</code> was at least called once.
     * Makes only sense, if the <code>Connection</code> is not in
     * autocommit mode.
     * @throws VerifyFailedException if verification fails
     */
    public void verifyRolledBack()
    {
        int number = mockFactory.getMockConnection().getNumberRollbacks();
        if(number <= 0)
        {
            throw new VerifyFailedException("Connection received no rollbacks.");
        }
    }
    
    /**
     * Verifies that the changes were not rolled back.
     * Makes only sense, if the <code>Connection</code> is not in
     * autocommit mode.
     * @throws VerifyFailedException if verification fails
     */
    public void verifyNotRolledBack()
    {
        int number = mockFactory.getMockConnection().getNumberRollbacks();
        if(number > 0)
        {
            throw new VerifyFailedException("Connection was rolled back.");
        }
    }
    
    /**
     * Verifies the number of <code>commit</code> calls.
     * Makes only sense, if the <code>Connection</code> is not in
     * autocommit mode.
     * @param number the expected number of commits
     * @throws VerifyFailedException if verification fails
     */
    public void verifyNumberCommits(int number)
    {
        int actualNumber = mockFactory.getMockConnection().getNumberCommits();
        if(actualNumber != number)
        {
            throw new VerifyFailedException("Connection received " + actualNumber + " commits, expected " + number);
        }
    }
    
    /**
     * Verifies the number of <code>rollback</code> calls.
     * Makes only sense, if the <code>Connection</code> is not in
     * autocommit mode.
     * @param number the expected number of rollbacks
     * @throws VerifyFailedException if verification fails
     */
    public void verifyNumberRollbacks(int number)
    {
        int actualNumber = mockFactory.getMockConnection().getNumberRollbacks();
        if(actualNumber != number)
        {
            throw new VerifyFailedException("Connection received " + actualNumber + " rollbacks, expected " + number);
        }
    }
    
    /**
     * Verifies the number of statements.
     * @param number the expected number
     * @throws VerifyFailedException if verification fails
     */
    public void verifyNumberStatements(int number)
    {
        verifyNumberStatements(number, getStatements());
    }
    
    /**
     * Verifies the number of prepared statements.
     * @param number the expected number
     * @throws VerifyFailedException if verification fails
     */
    public void verifyNumberPreparedStatements(int number)
    {
        verifyNumberStatements(number, getPreparedStatements());
    }
    
    /**
     * Verifies the number of prepared statements with the specified
     * SQL.
     * @param number the expected number
     * @param sql the SQL
     * @throws VerifyFailedException if verification fails
     */
    public void verifyNumberPreparedStatements(int number, String sql)
    {
        verifyNumberStatements(number, getPreparedStatements(sql));
    }
    
    /**
     * Verifies the number of callable statements.
     * @param number the expected number
     * @throws VerifyFailedException if verification fails
     */
    public void verifyNumberCallableStatements(int number)
    {
        verifyNumberStatements(number, getCallableStatements());
    }

    /**
     * Verifies the number of callable statements with the specified
     * SQL.
     * @param number the expected number
     * @param sql the SQL
     * @throws VerifyFailedException if verification fails
     */
    public void verifyNumberCallableStatements(int number, String sql)
    {
        verifyNumberStatements(number, getCallableStatements(sql));
    }
    
    private void verifyNumberStatements(int number, List<? extends MockStatement> statements)
    {
        if(null == statements || statements.isEmpty())
        {
            if(number == 0) return;
            throw new VerifyFailedException("Expected " + number + " statements, received 0 statements");
        }
        if(statements.size() != number)
        {
            throw new VerifyFailedException("Expected " + number + " statements, received " + statements.size()+ " statements");
        }
    }
    
    /**
     * Verifies that a statement is closed.
     * @param index the index of the statement
     * @throws VerifyFailedException if verification fails
     */
    public void verifyStatementClosed(int index)
    {
        MockStatement statement = getStatement(index);
        if(null == statement)
        {
            throw new VerifyFailedException("No statement with index " + index + " present.");
        }
        if(!statement.isClosed())
        {
            throw new VerifyFailedException("Statement with index " + index + " not closed.");
        }
    }
    
    /**
     * Verifies that a prepared statement is closed.
     * @param index the index of the prepared statement
     * @throws VerifyFailedException if verification fails
     */
    public void verifyPreparedStatementClosed(int index)
    {
        MockPreparedStatement statement = getPreparedStatement(index);
        if(null == statement)
        {
            throw new VerifyFailedException("No prepared statement with index " + index + " present.");
        }
        if(!statement.isClosed())
        {
            throw new VerifyFailedException("Prepared statement with index " + index + " not closed.");
        }
    }
    
    /**
     * Verifies that a prepared statement is closed.
     * @param sql the SQL statement
     * @throws VerifyFailedException if verification fails
     */
    public void verifyPreparedStatementClosed(String sql)
    {
        MockPreparedStatement statement = getPreparedStatement(sql);
        if(null == statement)
        {
            throw new VerifyFailedException("No prepared statement with SQL " + sql + " present.");
        }
        if(!statement.isClosed())
        {
            throw new VerifyFailedException("Prepared statement with SQL " + sql + " not closed.");
        }
    }
    
    /**
     * Verifies that a callable statement is closed.
     * @param index the index of the callable statement
     * @throws VerifyFailedException if verification fails
     */
    public void verifyCallableStatementClosed(int index)
    {
        MockCallableStatement statement = getCallableStatement(index);
        if(null == statement)
        {
            throw new VerifyFailedException("No callable statement with index " + index + " present.");
        }
        if(!statement.isClosed())
        {
            throw new VerifyFailedException("Callable statement with index " + index + " not closed.");
        }
    }

    /**
     * Verifies that a callable statement is closed.
     * @param sql the SQL statement
     * @throws VerifyFailedException if verification fails
     */
    public void verifyCallableStatementClosed(String sql)
    {
        MockCallableStatement statement = getCallableStatement(sql);
        if(null == statement)
        {
            throw new VerifyFailedException("No callable statement with SQL " + sql + " present.");
        }
        if(!statement.isClosed())
        {
            throw new VerifyFailedException("Callable statement with SQL " + sql + " not closed.");
        }
    }
    
    /**
     * Verifies that a row of a <code>ResultSet</code> is equal to the
     * entries in the specified <code>List</code>. Uses {@link com.mockrunner.mock.jdbc.MockResultSet#isRowEqual}.
     * You can verify the data of returned <code>ResultSet</code> objects if
     * the tested JDBC code makes updates.
     * @param resultSet the <code>ResultSet</code>
     * @param number the number of the row
     * @param rowData the row data
     * @throws VerifyFailedException if verification fails
     */
    public void verifyResultSetRow(MockResultSet resultSet, int number, List<Object> rowData)
    {
        if(null == resultSet.getRow(number))
        {
            throw new VerifyFailedException("ResultSet " + resultSet.getId() + " has no row " + number);
        }
        if(!resultSet.isRowEqual(number, rowData))
        {
            StringBuffer buffer = new StringBuffer("Actual row data:\n");
            StringUtil.appendObjectsAsString(buffer, resultSet.getRow(number));
            buffer.append("\n");
            buffer.append("Expected row data:\n");
            StringUtil.appendObjectsAsString(buffer, rowData);
            throw new VerifyFailedException("Mismatch in row data.\n" + buffer.toString());
        }
    }
    
    /**
     * Verifies that a row of a <code>ResultSet</code> is equal to the
     * entries in the specified array. Uses {@link com.mockrunner.mock.jdbc.MockResultSet#isRowEqual}.
     * You can verify the data of returned <code>ResultSet</code> objects if
     * the tested JDBC code makes updates.
     * @param resultSet the <code>ResultSet</code>
     * @param number the number of the row
     * @param rowData the row data
     * @throws VerifyFailedException if verification fails
     */
    public void verifyResultSetRow(MockResultSet resultSet, int number, Object[] rowData)
    {
        List<Object> dataList = Arrays.asList(rowData);
        verifyResultSetRow(resultSet, number, dataList);
    }
    
    /**
     * Verifies that a row of a <code>ResultSet</code> is equal to the
     * entries in the specified <code>List</code>. Uses {@link com.mockrunner.mock.jdbc.MockResultSet#isRowEqual}.
     * You can verify the data of returned <code>ResultSet</code> objects if
     * the tested JDBC code makes updates.
     * @param id the id of the <code>ResultSet</code>
     * @param number the number of the row
     * @param rowData the row data
     * @throws VerifyFailedException if verification fails
     */
    public void verifyResultSetRow(String id, int number, List<Object> rowData)
    {
        MockResultSet resultSet = getReturnedResultSet(id);
        if(null == resultSet)
        {
            throw new VerifyFailedException("ResultSet with id " + id + " not present.");
        }
        verifyResultSetRow(resultSet, number, rowData);
    }

    /**
     * Verifies that a row of a <code>ResultSet</code> is equal to the
     * entries in the specified array. Uses {@link com.mockrunner.mock.jdbc.MockResultSet#isRowEqual}.
     * You can verify the data of returned <code>ResultSet</code> objects if
     * the tested JDBC code makes updates.
     * @param id the id of the <code>ResultSet</code>
     * @param number the number of the row
     * @param rowData the row data
     * @throws VerifyFailedException if verification fails
     */
    public void verifyResultSetRow(String id, int number, Object[] rowData)
    {
        List<Object> dataList = Arrays.asList(rowData);
        verifyResultSetRow(id, number, dataList);
    }
    
    /**
     * Verifies that a column of a <code>ResultSet</code> is equal to the
     * entries in the specified <code>List</code>. Uses {@link com.mockrunner.mock.jdbc.MockResultSet#isColumnEqual(int, List)}.
     * You can verify the data of returned <code>ResultSet</code> objects if
     * the tested JDBC code makes updates.
     * @param resultSet the <code>ResultSet</code>
     * @param number the number of the column
     * @param columnData the column data
     * @throws VerifyFailedException if verification fails
     */
    public void verifyResultSetColumn(MockResultSet resultSet, int number, List<Object> columnData)
    {
        if(null == resultSet.getColumn(number))
        {
            throw new VerifyFailedException("ResultSet " + resultSet.getId() + " has no column " + number);
        }
        if(!resultSet.isColumnEqual(number, columnData))
        {
            StringBuffer buffer = new StringBuffer("Actual column data:\n");
            StringUtil.appendObjectsAsString(buffer, resultSet.getColumn(number));
            buffer.append("\n");
            buffer.append("Expected column data:\n");
            StringUtil.appendObjectsAsString(buffer, columnData);
            throw new VerifyFailedException("Mismatch in column data.\n" + buffer.toString());
        }
    }

    /**
     * Verifies that a column of a <code>ResultSet</code> is equal to the
     * entries in the specified array. Uses {@link com.mockrunner.mock.jdbc.MockResultSet#isColumnEqual(int, List)}.
     * You can verify the data of returned <code>ResultSet</code> objects if
     * the tested JDBC code makes updates.
     * @param resultSet the <code>ResultSet</code>
     * @param number the number of the column
     * @param columnData the column data
     * @throws VerifyFailedException if verification fails
     */
    public void verifyResultSetColumn(MockResultSet resultSet, int number, Object[] columnData)
    {
        List<Object> dataList = Arrays.asList(columnData);
        verifyResultSetColumn(resultSet, number, dataList);
    }

    /**
     * Verifies that a column of a <code>ResultSet</code> is equal to the
     * entries in the specified <code>List</code>. Uses {@link com.mockrunner.mock.jdbc.MockResultSet#isColumnEqual(int, List)}.
     * You can verify the data of returned <code>ResultSet</code> objects if
     * the tested JDBC code makes updates.
     * @param id the id of the <code>ResultSet</code>
     * @param number the number of the column
     * @param columnData the column data
     * @throws VerifyFailedException if verification fails
     */
    public void verifyResultSetColumn(String id, int number, List<Object> columnData)
    {
        MockResultSet resultSet = getReturnedResultSet(id);
        if(null == resultSet)
        {
            throw new VerifyFailedException("ResultSet with id " + id + " not present.");
        }
        verifyResultSetColumn(resultSet, number, columnData);
    }

    /**
     * Verifies that a column of a <code>ResultSet</code> is equal to the
     * entries in the specified array. Uses {@link com.mockrunner.mock.jdbc.MockResultSet#isColumnEqual(int, List)}.
     * You can verify the data of returned <code>ResultSet</code> objects if
     * the tested JDBC code makes updates.
     * @param id the id of the <code>ResultSet</code>
     * @param number the number of the column
     * @param columnData the column data
     * @throws VerifyFailedException if verification fails
     */
    public void verifyResultSetColumn(String id, int number, Object[] columnData)
    {
        List<Object> dataList = Arrays.asList(columnData);
        verifyResultSetColumn(id, number, dataList);
    }
    
    /**
     * Verifies that a column of a <code>ResultSet</code> is equal to the
     * entries in the specified <code>List</code>. Uses {@link com.mockrunner.mock.jdbc.MockResultSet#isColumnEqual(String, List)}.
     * You can verify the data of returned <code>ResultSet</code> objects if
     * the tested JDBC code makes updates.
     * @param resultSet the <code>ResultSet</code>
     * @param name the name of the column
     * @param columnData the column data
     * @throws VerifyFailedException if verification fails
     */
    public void verifyResultSetColumn(MockResultSet resultSet, String name, List<Object> columnData)
    {
        if(null == resultSet.getColumn(name))
        {
            throw new VerifyFailedException("ResultSet " + resultSet.getId() + " has no column " + name);
        }
        if(!resultSet.isColumnEqual(name, columnData))
        {
            StringBuffer buffer = new StringBuffer("Actual column data:\n");
            StringUtil.appendObjectsAsString(buffer, resultSet.getColumn(name));
            buffer.append("\n");
            buffer.append("Expected column data:\n");
            StringUtil.appendObjectsAsString(buffer, columnData);
            throw new VerifyFailedException("Mismatch in column data.\n" + buffer.toString());
        }
    }

    /**
     * Verifies that a column of a <code>ResultSet</code> is equal to the
     * entries in the specified array. Uses {@link com.mockrunner.mock.jdbc.MockResultSet#isColumnEqual(String, List)}.
     * You can verify the data of returned <code>ResultSet</code> objects if
     * the tested JDBC code makes updates.
     * @param resultSet the <code>ResultSet</code>
     * @param name the name of the column
     * @param columnData the column data
     * @throws VerifyFailedException if verification fails
     */
    public void verifyResultSetColumn(MockResultSet resultSet, String name, Object[] columnData)
    {
        List<Object> dataList = Arrays.asList(columnData);
        verifyResultSetColumn(resultSet, name, dataList);
    }

    /**
     * Verifies that a column of a <code>ResultSet</code> is equal to the
     * entries in the specified <code>List</code>. Uses {@link com.mockrunner.mock.jdbc.MockResultSet#isColumnEqual(String, List)}.
     * You can verify the data of returned <code>ResultSet</code> objects if
     * the tested JDBC code makes updates.
     * @param id the id of the <code>ResultSet</code>
     * @param name the name of the column
     * @param columnData the column data
     * @throws VerifyFailedException if verification fails
     */
    public void verifyResultSetColumn(String id, String name, List<Object> columnData)
    {
        MockResultSet resultSet = getReturnedResultSet(id);
        if(null == resultSet)
        {
            throw new VerifyFailedException("ResultSet with id " + id + " not present.");
        }
        verifyResultSetColumn(resultSet, name, columnData);
    }

    /**
     * Verifies that a column of a <code>ResultSet</code> is equal to the
     * entries in the specified array. Uses {@link com.mockrunner.mock.jdbc.MockResultSet#isColumnEqual(String, List)}.
     * You can verify the data of returned <code>ResultSet</code> objects if
     * the tested JDBC code makes updates.
     * @param id the id of the <code>ResultSet</code>
     * @param name the name of the column
     * @param columnData the column data
     * @throws VerifyFailedException if verification fails
     */
    public void verifyResultSetColumn(String id, String name, Object[] columnData)
    {
        List<Object> dataList = Arrays.asList(columnData);
        verifyResultSetColumn(id, name, dataList);
    }
    
    /**
     * Verifies that a <code>ResultSet</code> is equal to another one.
     * Compares all the rows with {@link com.mockrunner.mock.jdbc.MockResultSet#isEqual}.
     * @param source the source <code>ResultSet</code>
     * @param target the target <code>ResultSet</code>
     * @throws VerifyFailedException if verification fails
     */
    public void verifyResultSetEquals(MockResultSet source, MockResultSet target)
    {
        if(!source.isEqual(target))
        {
            String buffer = "Source data:\n" + source.toString() +
                    "\n" +
                    "Target data:\n" +
                    target.toString();
            throw new VerifyFailedException("Mismatch in ResultSet data.\n" + buffer);
        }
    }
    
    /**
     * Verifies that a <code>ResultSet</code> is equal to another one.
     * Compares all the rows with {@link com.mockrunner.jdbc.ParameterUtil#compareParameter}.
     * @param id the id of the source <code>ResultSet</code>
     * @param target the target <code>ResultSet</code>
     * @throws VerifyFailedException if verification fails
     */
    public void verifyResultSetEquals(String id, MockResultSet target)
    {
        MockResultSet resultSet = getReturnedResultSet(id);
        if(null == resultSet)
        {
            throw new VerifyFailedException("ResultSet with id " + id + " not present.");
        }
        verifyResultSetEquals(resultSet, target);
    }
    
    /**
     * Verifies that a <code>PreparedStatement</code> with the specified 
     * SQL statement is present.
     * @param sql the SQL statement
     * @throws VerifyFailedException if verification fails
     */
    public void verifyPreparedStatementPresent(String sql)
    {
        if(null == getPreparedStatement(sql))
        {
            throw new VerifyFailedException("Prepared statement with SQL " +  sql + " present.");
        }
    }
    
    /**
     * Verifies that a <code>PreparedStatement</code> with the specified 
     * SQL statement is not present.
     * @param sql the SQL statement
     * @throws VerifyFailedException if verification fails
     */
    public void verifyPreparedStatementNotPresent(String sql)
    {
        if(null != getPreparedStatement(sql) )
        {
            throw new VerifyFailedException("Prepared statement with SQL " +  sql + " not present.");
        }
    }
    
    /**
     * Verifies that a <code>CallableStatement</code> with the specified 
     * SQL statement is present.
     * @param sql the SQL statement
     * @throws VerifyFailedException if verification fails
     */
    public void verifyCallableStatementPresent(String sql)
    {
        if(null == getCallableStatement(sql))
        {
            throw new VerifyFailedException("Callable statement with SQL " +  sql + " present.");
        }
    }
    
    /**
     * Verifies that a <code>CallableStatement</code> with the specified 
     * SQL statement is not present.
     * @param sql the SQL statement
     * @throws VerifyFailedException if verification fails
     */
    public void verifyCallableStatementNotPresent(String sql)
    {
        if(null != getCallableStatement(sql))
        {
            throw new VerifyFailedException("Callable statement with SQL " +  sql + " not present.");
        }
    }
    
    /**
     * Verifies that a parameter was added to a <code>PreparedStatement</code> with
     * the specified index.
     * @param statement the <code>PreparedStatement</code>
     * @param indexOfParameter the index used to set the object
     * @throws VerifyFailedException if verification fails
     */
    public void verifyPreparedStatementParameterPresent(PreparedStatement statement, int indexOfParameter)
    {
        if(!containsPreparedStatementParameter(statement, indexOfParameter))
        {
            throw new VerifyFailedException("Prepared statement parameter with index " + indexOfParameter + " not present.");
        }
    }

    /**
     * Verifies that a parameter was added to a <code>PreparedStatement</code> with
     * the specified index. Uses the first <code>PreparedStatement</code> with
     * the specified SQL.
     * @param sql the SQL statement of the <code>PreparedStatement</code>
     * @param indexOfParameter the index used to set the object
     * @throws VerifyFailedException if verification fails
     */
    public void verifyPreparedStatementParameterPresent(String sql, int indexOfParameter)
    {
        if(!containsPreparedStatementParameter(sql, indexOfParameter))
        {
            throw new VerifyFailedException("Prepared statement parameter with index " + indexOfParameter + " not present.");
        }
    }
    
    /**
     * Verifies that a parameter was added to a <code>PreparedStatement</code> with
     * the specified index.
     * @param indexOfStatement the index of the statement
     * @param indexOfParameter the index used to set the object
     * @throws VerifyFailedException if verification fails
     */
    public void verifyPreparedStatementParameterPresent(int indexOfStatement, int indexOfParameter)
    {
        if(!containsPreparedStatementParameter(indexOfStatement, indexOfParameter))
        {
            throw new VerifyFailedException("Prepared statement parameter with index " + indexOfParameter + " not present.");
        }
    }
    
    /**
     * Verifies that a parameter with the specified index is not present.
     * @param statement the <code>PreparedStatement</code>
     * @param indexOfParameter the index used to set the object
     * @throws VerifyFailedException if verification fails
     */
    public void verifyPreparedStatementParameterNotPresent(PreparedStatement statement, int indexOfParameter)
    {
        if(containsPreparedStatementParameter(statement, indexOfParameter))
        {
            throw new VerifyFailedException("Prepared statement parameter with index " + indexOfParameter + " present.");
        }
    }

    /**
     * Verifies that a parameter with the specified index is not present.
     * Uses the first <code>PreparedStatement</code> with the specified SQL.
     * @param sql the SQL statement of the <code>PreparedStatement</code>
     * @param indexOfParameter the index used to set the object
     * @throws VerifyFailedException if verification fails
     */
    public void verifyPreparedStatementParameterNotPresent(String sql, int indexOfParameter)
    {
        if(containsPreparedStatementParameter(sql, indexOfParameter))
        {
            throw new VerifyFailedException("Prepared statement parameter with index " + indexOfParameter + " present.");
        }
    }

    /**
     * Verifies that a parameter with the specified index is not present.
     * @param indexOfStatement the index of the statement
     * @param indexOfParameter the index used to set the object
     * @throws VerifyFailedException if verification fails
     */
    public void verifyPreparedStatementParameterNotPresent(int indexOfStatement, int indexOfParameter)
    {
        if(containsPreparedStatementParameter(indexOfStatement, indexOfParameter))
        {
            throw new VerifyFailedException("Prepared statement parameter with index " + indexOfParameter + " present.");
        }
    }
    
    private boolean containsPreparedStatementParameter(int indexOfStatement, int indexOfParameter)
    {
        MockPreparedStatement statement = getPreparedStatement(indexOfStatement);
        if(null == statement) return false;
        return containsPreparedStatementParameter(statement, indexOfParameter);
    }
    
    private boolean containsPreparedStatementParameter(String sql, int indexOfParameter)
    {
        MockPreparedStatement statement = getPreparedStatement(sql);
        if(null == statement) return false;
        return containsPreparedStatementParameter(statement, indexOfParameter);
    }
    
    private boolean containsPreparedStatementParameter(PreparedStatement statement, int indexOfParameter)
    {
        return ((MockPreparedStatement)statement).getParameterMap().containsKey(new ParameterIndex(indexOfParameter));
    }
    
    /**
     * Verifies that a parameter was added to a <code>CallableStatement</code> with
     * the specified index.
     * @param statement the <code>CallableStatement</code>
     * @param indexOfParameter the index used to set the object
     * @throws VerifyFailedException if verification fails
     */
    public void verifyCallableStatementParameterPresent(CallableStatement statement, int indexOfParameter)
    {
        if(!containsCallableStatementParameter(statement, indexOfParameter))
        {
            throw new VerifyFailedException("Callable statement parameter with index " + indexOfParameter + " not present.");
        }
    }

    /**
     * Verifies that a parameter was added to a <code>CallableStatement</code> with
     * the specified index. Uses the first <code>CallableStatement</code> with
     * the specified SQL.
     * @param sql the SQL statement of the <code>CallableStatement</code>
     * @param indexOfParameter the index used to set the object
     * @throws VerifyFailedException if verification fails
     */
    public void verifyCallableStatementParameterPresent(String sql, int indexOfParameter)
    {
        if(!containsCallableStatementParameter(sql, indexOfParameter))
        {
            throw new VerifyFailedException("Callable statement parameter with index " + indexOfParameter + " not present.");
        }
    }
    
    /**
     * Verifies that a parameter was added to a <code>CallableStatement</code> with
     * the specified index.
     * @param indexOfStatement the index of the statement
     * @param indexOfParameter the index used to set the object
     * @throws VerifyFailedException if verification fails
     */
    public void verifyCallableStatementParameterPresent(int indexOfStatement, int indexOfParameter)
    {
        if(!containsCallableStatementParameter(indexOfStatement, indexOfParameter))
        {
            throw new VerifyFailedException("Callable statement parameter with index " + indexOfParameter + " not present.");
        }
    }
    
    /**
     * Verifies that a parameter with the specified index is not present.
     * @param statement the <code>CallableStatement</code>
     * @param indexOfParameter the index used to set the object
     * @throws VerifyFailedException if verification fails
     */
    public void verifyCallableStatementParameterNotPresent(CallableStatement statement, int indexOfParameter)
    {
        if(containsCallableStatementParameter(statement, indexOfParameter))
        {
            throw new VerifyFailedException("Callable statement parameter with index " + indexOfParameter + " present.");
        }
    }

    /**
     * Verifies that a parameter with the specified index is not present.
     * Uses the first <code>CallableStatement</code> with the specified SQL.
     * @param sql the SQL statement of the <code>CallableStatement</code>
     * @param indexOfParameter the index used to set the object
     * @throws VerifyFailedException if verification fails
     */
    public void verifyCallableStatementParameterNotPresent(String sql, int indexOfParameter)
    {
        if(containsCallableStatementParameter(sql, indexOfParameter))
        {
            throw new VerifyFailedException("Callable statement parameter with index " + indexOfParameter + " present.");
        }
    }

    /**
     * Verifies that a parameter with the specified index is not present.
     * @param indexOfStatement the index of the statement
     * @param indexOfParameter the index used to set the object
     * @throws VerifyFailedException if verification fails
     */
    public void verifyCallableStatementParameterNotPresent(int indexOfStatement, int indexOfParameter)
    {
        if(containsCallableStatementParameter(indexOfStatement, indexOfParameter))
        {
            throw new VerifyFailedException("Callable statement parameter with index " + indexOfParameter + " present.");
        }
    }
    
    /**
     * Verifies that a parameter was added to a <code>CallableStatement</code> with
     * the specified index.
     * @param statement the <code>CallableStatement</code>
     * @param nameOfParameter the name of the parameter
     * @throws VerifyFailedException if verification fails
     */
    public void verifyCallableStatementParameterPresent(CallableStatement statement, String nameOfParameter)
    {
        if(!containsCallableStatementParameter(statement, nameOfParameter))
        {
            throw new VerifyFailedException("Callable statement parameter with index " + nameOfParameter + " not present.");
        }
    }

    /**
     * Verifies that a parameter was added to a <code>CallableStatement</code> with
     * the specified index. Uses the first <code>CallableStatement</code> with
     * the specified SQL.
     * @param sql the SQL statement of the <code>CallableStatement</code>
     * @param nameOfParameter the name of the parameter
     * @throws VerifyFailedException if verification fails
     */
    public void verifyCallableStatementParameterPresent(String sql, String nameOfParameter)
    {
        if(!containsCallableStatementParameter(sql, nameOfParameter))
        {
            throw new VerifyFailedException("Callable statement parameter with index " + nameOfParameter + " not present.");
        }
    }
    
    /**
     * Verifies that a parameter was added to a <code>CallableStatement</code> with
     * the specified index.
     * @param indexOfStatement the index of the statement
     * @param nameOfParameter the name of the parameter
     * @throws VerifyFailedException if verification fails
     */
    public void verifyCallableStatementParameterPresent(int indexOfStatement, String nameOfParameter)
    {
        if(!containsCallableStatementParameter(indexOfStatement, nameOfParameter))
        {
            throw new VerifyFailedException("Callable statement parameter with index " + nameOfParameter + " not present.");
        }
    }
    
    /**
     * Verifies that a parameter with the specified index is not present.
     * @param statement the <code>CallableStatement</code>
     * @param nameOfParameter the name of the parameter
     * @throws VerifyFailedException if verification fails
     */
    public void verifyCallableStatementParameterNotPresent(CallableStatement statement, String nameOfParameter)
    {
        if(containsCallableStatementParameter(statement, nameOfParameter))
        {
            throw new VerifyFailedException("Callable statement parameter with index " + nameOfParameter + " present.");
        }
    }

    /**
     * Verifies that a parameter with the specified index is not present.
     * Uses the first <code>CallableStatement</code> with the specified SQL.
     * @param sql the SQL statement of the <code>CallableStatement</code>
     * @param nameOfParameter the name of the parameter
     * @throws VerifyFailedException if verification fails
     */
    public void verifyCallableStatementParameterNotPresent(String sql, String nameOfParameter)
    {
        if(containsCallableStatementParameter(sql, nameOfParameter))
        {
            throw new VerifyFailedException("Callable statement parameter with index " + nameOfParameter + " present.");
        }
    }

    /**
     * Verifies that a parameter with the specified index is not present.
     * @param indexOfStatement the index of the statement
     * @param nameOfParameter the name of the parameter
     * @throws VerifyFailedException if verification fails
     */
    public void verifyCallableStatementParameterNotPresent(int indexOfStatement, String nameOfParameter)
    {
        if(containsCallableStatementParameter(indexOfStatement, nameOfParameter))
        {
            throw new VerifyFailedException("Callable statement parameter with index " + nameOfParameter + " present.");
        }
    }
    
    private boolean containsCallableStatementParameter(int indexOfStatement, int indexOfParameter)
    {
        MockCallableStatement statement = getCallableStatement(indexOfStatement);
        if(null == statement) return false;
        return containsCallableStatementParameter(statement, indexOfParameter);
    }
    
    private boolean containsCallableStatementParameter(String sql, int indexOfParameter)
    {
        MockCallableStatement statement = getCallableStatement(sql);
        if(null == statement) return false;
        return containsCallableStatementParameter(statement, indexOfParameter);
    }
    
    private boolean containsCallableStatementParameter(CallableStatement statement, int indexOfParameter)
    {
        return ((MockCallableStatement)statement).getParameterMap().containsKey(new ParameterIndex(indexOfParameter));
    }
    
    private boolean containsCallableStatementParameter(int indexOfStatement, String nameOfParameter)
    {
        MockCallableStatement statement = getCallableStatement(indexOfStatement);
        if(null == statement) return false;
        return containsCallableStatementParameter(statement, nameOfParameter);
    }
    
    private boolean containsCallableStatementParameter(String sql, String nameOfParameter)
    {
        MockCallableStatement statement = getCallableStatement(sql);
        if(null == statement) return false;
        return containsCallableStatementParameter(statement, nameOfParameter);
    }
    
    private boolean containsCallableStatementParameter(CallableStatement statement, String nameOfParameter)
    {
        return ((MockCallableStatement)statement).getParameterMap().containsKey(new ParameterName(nameOfParameter));
    }
    
    /**
     * Verifies that a parameter from the specified <code>PreparedStatement</code> is equal
     * to the specified object. Please use the corresponding wrapper type for
     * primitive data types.
     * @param statement the <code>PreparedStatement</code>
     * @param indexOfParameter the index used to set the object
     * @param object the expected object
     * @throws VerifyFailedException if verification fails
     */
    public void verifyPreparedStatementParameter(PreparedStatement statement, int indexOfParameter, Object object)
    {
        verifyPreparedStatementParameterPresent(statement, indexOfParameter);
        Object actualObject = getPreparedStatementParameter(statement, indexOfParameter);
        if(!ParameterUtil.compareParameter(actualObject, object))
        {
            throw new VerifyFailedException("Prepared statement parameter with index " + indexOfParameter + " has the value " +
                                            String.valueOf(actualObject) + ", expected " + String.valueOf(object));
        }
    }
    
    /**
     * Verifies that a parameter from the <code>PreparedStatement</code> with the
     * specified SQL statement is equal to the specified object.
     * Uses the first <code>PreparedStatement</code> with the specified SQL.
     * Please use the corresponding wrapper type for primitive data types.
     * @param sql the SQL statement of the <code>PreparedStatement</code>
     * @param indexOfParameter the index used to set the object
     * @param object the expected object
     * @throws VerifyFailedException if verification fails
     */
    public void verifyPreparedStatementParameter(String sql, int indexOfParameter, Object object)
    {
        verifyPreparedStatementParameterPresent(sql, indexOfParameter);
        Object actualObject = getPreparedStatementParameter(sql, indexOfParameter);
        if(!ParameterUtil.compareParameter(actualObject, object))
        {
            throw new VerifyFailedException("Prepared statement parameter with index " + indexOfParameter + " has the value " +
                                            String.valueOf(actualObject) + ", expected " + String.valueOf(object));
        }
    }
    
    /**
     * Verifies that a parameter from the <code>PreparedStatement</code> with the
     * specified SQL statement is equal to the specified object.
     * Please use the corresponding wrapper type for primitive data types.
     * @param indexOfStatement the index of the statement
     * @param indexOfParameter the index used to set the object
     * @param object the expected object
     * @throws VerifyFailedException if verification fails
     */
    public void verifyPreparedStatementParameter(int indexOfStatement, int indexOfParameter, Object object)
    {
        verifyPreparedStatementParameterPresent(indexOfStatement, indexOfParameter);
        Object actualObject = getPreparedStatementParameter(indexOfStatement, indexOfParameter);
        if(!ParameterUtil.compareParameter(actualObject, object))
        {
            throw new VerifyFailedException("Prepared statement parameter with index " + indexOfParameter + " has the value " +
                                            String.valueOf(actualObject) + ", expected " + String.valueOf(object));
        }
    }
    
    /**
     * Verifies that a parameter from the specified <code>CallableStatement</code> is equal
     * to the specified object. Please use the corresponding wrapper type 
     * for primitive data types.
     * @param statement the <code>CallableStatement</code>
     * @param indexOfParameter the index used to set the object
     * @param object the expected object
     * @throws VerifyFailedException if verification fails
     */
    public void verifyCallableStatementParameter(CallableStatement statement, int indexOfParameter, Object object)
    {
        verifyCallableStatementParameterPresent(statement, indexOfParameter);
        Object actualObject = getCallableStatementParameter(statement, indexOfParameter);
        if(!ParameterUtil.compareParameter(actualObject, object))
        {
            throw new VerifyFailedException("Callable statement parameter with index " + indexOfParameter + " has the value " +
                                            String.valueOf(actualObject) + ", expected " + String.valueOf(object));
        }
    }
    
    /**
     * Verifies that a parameter from the <code>CallableStatement</code> with the
     * specified SQL statement is equal to the specified object.
     * Uses the first <code>CallableStatement</code> with the specified SQL.
     * Please use the corresponding wrapper type for primitive data types.
     * @param sql the SQL statement of the <code>CallableStatement</code>
     * @param indexOfParameter the index used to set the object
     * @param object the expected object
     * @throws VerifyFailedException if verification fails
     */
    public void verifyCallableStatementParameter(String sql, int indexOfParameter, Object object)
    {
        verifyCallableStatementParameterPresent(sql, indexOfParameter);
        Object actualObject = getCallableStatementParameter(sql, indexOfParameter);
        if(!ParameterUtil.compareParameter(actualObject, object))
        {
            throw new VerifyFailedException("Callable statement parameter with index " + indexOfParameter + " has the value " +
                                            String.valueOf(actualObject) + ", expected " + String.valueOf(object));
        }
    }
    
    /**
     * Verifies that a parameter from the <code>CallableStatement</code> with the
     * specified SQL statement is equal to the specified object.
     * Please use the corresponding wrapper type for primitive data types.
     * @param indexOfStatement the index of the statement
     * @param indexOfParameter the index used to set the object
     * @param object the expected object
     * @throws VerifyFailedException if verification fails
     */
    public void verifyCallableStatementParameter(int indexOfStatement, int indexOfParameter, Object object)
    {
        verifyCallableStatementParameterPresent(indexOfStatement, indexOfParameter);
        Object actualObject = getCallableStatementParameter(indexOfStatement, indexOfParameter);
        if(!ParameterUtil.compareParameter(actualObject, object))
        {
            throw new VerifyFailedException("Callable statement parameter with index " + indexOfParameter + " has the value " +
                                            String.valueOf(actualObject) + ", expected " + String.valueOf(object));
        }
    }
    
    /**
     * Verifies that a parameter from the specified <code>CallableStatement</code> is equal
     * to the specified object. Please use the corresponding wrapper type 
     * for primitive data types.
     * @param statement the <code>CallableStatement</code>
     * @param nameOfParameter the name of the parameter
     * @param object the expected object
     * @throws VerifyFailedException if verification fails
     */
    public void verifyCallableStatementParameter(CallableStatement statement, String nameOfParameter, Object object)
    {
        verifyCallableStatementParameterPresent(statement, nameOfParameter);
        Object actualObject = getCallableStatementParameter(statement, nameOfParameter);
        if(!ParameterUtil.compareParameter(actualObject, object))
        {
            throw new VerifyFailedException("Callable statement parameter with name " + nameOfParameter + " has the value " +
                                            String.valueOf(actualObject) + ", expected " + String.valueOf(object));
        }
    }
    
    /**
     * Verifies that a parameter from the <code>CallableStatement</code> with the
     * specified SQL statement is equal to the specified object.
     * Uses the first <code>CallableStatement</code> with the specified SQL.
     * Please use the corresponding wrapper type for primitive data types.
     * @param sql the SQL statement of the <code>CallableStatement</code>
     * @param nameOfParameter the name of the parameter
     * @param object the expected object
     * @throws VerifyFailedException if verification fails
     */
    public void verifyCallableStatementParameter(String sql, String nameOfParameter, Object object)
    {
        verifyCallableStatementParameterPresent(sql, nameOfParameter);
        Object actualObject = getCallableStatementParameter(sql, nameOfParameter);
        if(!ParameterUtil.compareParameter(actualObject, object))
        {
            throw new VerifyFailedException("Callable statement parameter with name " + nameOfParameter + " has the value " +
                                            String.valueOf(actualObject) + ", expected " + String.valueOf(object));
        }
    }
    
    /**
     * Verifies that a parameter from the <code>CallableStatement</code> with the
     * specified SQL statement is equal to the specified object.
     * Please use the corresponding wrapper type for primitive data types.
     * @param indexOfStatement the index of the statement
     * @param nameOfParameter the name of the parameter
     * @param object the expected object
     * @throws VerifyFailedException if verification fails
     */
    public void verifyCallableStatementParameter(int indexOfStatement, String nameOfParameter, Object object)
    {
        verifyCallableStatementParameterPresent(indexOfStatement, nameOfParameter);
        Object actualObject = getCallableStatementParameter(indexOfStatement, nameOfParameter);
        if(!ParameterUtil.compareParameter(actualObject, object))
        {
            throw new VerifyFailedException("Callable statement parameter with name " + nameOfParameter + " has the value " +
                                            String.valueOf(actualObject) + ", expected " + String.valueOf(object));
        }
    }
    
    /**
     * Verifies that an out parameter was registered on the specified
     * <code>CallableStatement</code>.
     * @param statement the <code>CallableStatement</code>
     * @param indexOfParameter the index of the parameter
     * @throws VerifyFailedException if verification fails
     */
    public void verifyCallableStatementOutParameterRegistered(CallableStatement statement, int indexOfParameter)
    {
        if(!((MockCallableStatement)statement).isOutParameterRegistered(indexOfParameter))
        {
            throw new VerifyFailedException("Out parameter with index " + indexOfParameter +
                                            " not registered in callable statement ");
        }
    }

    /**
     * Verifies that an out parameter was registered on the
     * <code>CallableStatement</code> with the specified SQL.
     * @param sql the SQL statement
     * @param indexOfParameter the index of the parameter
     * @throws VerifyFailedException if verification fails
     */
    public void verifyCallableStatementOutParameterRegistered(String sql, int indexOfParameter)
    {
        MockCallableStatement statement = getCallableStatement(sql);
        if(null == statement)
        {
            throw new VerifyFailedException("No callable statement " + sql + " present");
        }
        if(!statement.isOutParameterRegistered(indexOfParameter))
        {
            throw new VerifyFailedException("Out parameter with index " + indexOfParameter +
                                            " not registered in callable statement " + sql);
        }
    }

    /**
     * Verifies that an out parameter was registered on the
     * <code>CallableStatement</code> with the specified index.
     * @param indexOfStatement the index of the <code>CallableStatement</code>
     * @param indexOfParameter the index of the parameter
     * @throws VerifyFailedException if verification fails
     */
    public void verifyCallableStatementOutParameterRegistered(int indexOfStatement, int indexOfParameter)
    {
        MockCallableStatement statement = getCallableStatement(indexOfStatement);
        if(null == statement)
        {
            throw new VerifyFailedException("No callable statement with index " + indexOfStatement + " present");
        }
        if(!statement.isOutParameterRegistered(indexOfParameter))
        {
            throw new VerifyFailedException("Out parameter with index " + indexOfParameter +
                                            " not registered in callable statement with index " + indexOfStatement);
        }
    }
    
    /**
     * Verifies that an out parameter was registered on the specified
     * <code>CallableStatement</code>.
     * @param statement the <code>CallableStatement</code>
     * @param nameOfParameter the name of the parameter
     * @throws VerifyFailedException if verification fails
     */
    public void verifyCallableStatementOutParameterRegistered(CallableStatement statement, String nameOfParameter)
    {
        if(!((MockCallableStatement)statement).isOutParameterRegistered(nameOfParameter))
        {
            throw new VerifyFailedException("Out parameter with name " + nameOfParameter +
                                            " not registered in callable statement ");
        }
    }
    
    /**
     * Verifies that an out parameter was registered on the
     * <code>CallableStatement</code> with the specified SQL.
     * @param sql the SQL statement
     * @param nameOfParameter the name of the parameter
     * @throws VerifyFailedException if verification fails
     */
    public void verifyCallableStatementOutParameterRegistered(String sql, String nameOfParameter)
    {
        MockCallableStatement statement = getCallableStatement(sql);
        if(null == statement)
        {
            throw new VerifyFailedException("No callable statement " + sql + " present");
        }
        if(!statement.isOutParameterRegistered(nameOfParameter))
        {
            throw new VerifyFailedException("Out parameter with name " + nameOfParameter +
                                            " not registered in callable statement " + sql);
        }
    }
    
    /**
     * Verifies that an out parameter was registered on the
     * <code>CallableStatement</code> with the specified index.
     * @param indexOfStatement the index of the <code>CallableStatement</code>
     * @param nameOfParameter the name of the parameter
     * @throws VerifyFailedException if verification fails
     */
    public void verifyCallableStatementOutParameterRegistered(int indexOfStatement, String nameOfParameter)
    {
        MockCallableStatement statement = getCallableStatement(indexOfStatement);
        if(null == statement)
        {
            throw new VerifyFailedException("No callable statement with index " + indexOfStatement + " present");
        }
        if(!statement.isOutParameterRegistered(nameOfParameter))
        {
            throw new VerifyFailedException("Out parameter with name " + nameOfParameter +
                                            " not registered in callable statement with index " + indexOfStatement);
        }
    }
    
    /**
     * Verifies that a <code>Savepoint</code> with the specified index
     * is present. The index is the number of the created <code>Savepoint</code>
     * starting with 0 for the first <code>Savepoint</code>.
     * @param index the index of the <code>Savepoint</code>
     */
    public void verifySavepointPresent(int index)
    {
        MockSavepoint savepoint = getSavepoint(index);
        if(null == savepoint)
        {
            throw new VerifyFailedException("No savepoint with index " + index + " present.");
        }
    }
    
    /**
     * Verifies that a <code>Savepoint</code> with the specified name
     * is present.
     * @param name the name of the <code>Savepoint</code>
     */
    public void verifySavepointPresent(String name)
    {
        MockSavepoint savepoint = getSavepoint(name);
        if(null == savepoint)
        {
            throw new VerifyFailedException("No savepoint with name " + name + " present.");
        }
    }
    
    /**
     * Verifies that the <code>Savepoint</code> with the specified index
     * is released. The index is the number of the created <code>Savepoint</code>
     * starting with 0 for the first <code>Savepoint</code>.
     * @param index the index of the <code>Savepoint</code>
     */
    public void verifySavepointReleased(int index)
    {
        verifySavepointPresent(index);
        if(!getSavepoint(index).isReleased())
        {
            throw new VerifyFailedException("Savepoint with index " + index + " not released.");
        }
    }
    
    /**
     * Verifies that the <code>Savepoint</code> with the specified name
     * is released.
     * @param name the name of the <code>Savepoint</code>
     */
    public void verifySavepointReleased(String name)
    {
        verifySavepointPresent(name);
        if(!getSavepoint(name).isReleased())
        {
            throw new VerifyFailedException("Savepoint with name " + name + " not released.");
        }
    }
    
    /**
     * Verifies that the <code>Savepoint</code> with the specified index
     * is not released. The index is the number of the created <code>Savepoint</code>
     * starting with 0 for the first <code>Savepoint</code>.
     * @param index the index of the <code>Savepoint</code>
     */
    public void verifySavepointNotReleased(int index)
    {
        verifySavepointPresent(index);
        if(getSavepoint(index).isReleased())
        {
            throw new VerifyFailedException("Savepoint with index " + index + " is released.");
        }
    }

    /**
     * Verifies that the <code>Savepoint</code> with the specified name
     * is not released.
     * @param name the name of the <code>Savepoint</code>
     */
    public void verifySavepointNotReleased(String name)
    {
        verifySavepointPresent(name);
        if(getSavepoint(name).isReleased())
        {
            throw new VerifyFailedException("Savepoint with name " + name + " is released.");
        }
    }
    
    /**
     * Verifies that the <code>Savepoint</code> with the specified index
     * is rolled back. The index is the number of the created <code>Savepoint</code>
     * starting with 0 for the first <code>Savepoint</code>.
     * @param index the index of the <code>Savepoint</code>
     */
    public void verifySavepointRolledBack(int index)
    {
        verifySavepointPresent(index);
        if(!getSavepoint(index).isRolledBack())
        {
            throw new VerifyFailedException("Savepoint with index " + index + " not rolled back.");
        }
    }

    /**
     * Verifies that the <code>Savepoint</code> with the specified name
     * is rolled back.
     * @param name the name of the <code>Savepoint</code>
     */
    public void verifySavepointRolledBack(String name)
    {
        verifySavepointPresent(name);
        if(!getSavepoint(name).isRolledBack())
        {
            throw new VerifyFailedException("Savepoint with name " + name + " not rolled back.");
        }
    }
    
    /**
     * Verifies that the <code>Savepoint</code> with the specified index
     * is not rolled back. The index is the number of the created <code>Savepoint</code>
     * starting with 0 for the first <code>Savepoint</code>.
     * @param index the index of the <code>Savepoint</code>
     */
    public void verifySavepointNotRolledBack(int index)
    {
        verifySavepointPresent(index);
        if(getSavepoint(index).isRolledBack())
        {
            throw new VerifyFailedException("Savepoint with index " + index + " is rolled back.");
        }
    }

    /**
     * Verifies that the <code>Savepoint</code> with the specified name
     * is not rolled back.
     * @param name the name of the <code>Savepoint</code>
     */
    public void verifySavepointNotRolledBack(String name)
    {
        verifySavepointPresent(name);
        if(getSavepoint(name).isRolledBack())
        {
            throw new VerifyFailedException("Savepoint with name " + name + " is rolled back.");
        }
    }
    
    /**
     * @deprecated use {@link #verifySavepointRolledBack(int)}
     */
    @Deprecated
    public void verifySavepointRollbacked(int index)
    {
        verifySavepointRolledBack(index);
    }

    /**
     * @deprecated use {@link #verifySavepointRolledBack(String)}
     */
    @Deprecated
    public void verifySavepointRollbacked(String name)
    {
        verifySavepointRolledBack(name);
    }

    /**
     * @deprecated use {@link #verifySavepointNotRolledBack(int)}
     */
    @Deprecated
    public void verifySavepointNotRollbacked(int index)
    {
        verifySavepointNotRolledBack(index);
    }

    /**
     * @deprecated use {@link #verifySavepointNotRolledBack(String)}
     */
    @Deprecated
    public void verifySavepointNotRollbacked(String name)
    {
        verifySavepointNotRolledBack(name);
    }
}
