package com.mockrunner.example.jdbc;

import java.util.ArrayList;
import java.util.List;

import com.mockrunner.jdbc.FileResultSetFactory;
import com.mockrunner.jdbc.JDBCTestCaseAdapter;
import com.mockrunner.mock.jdbc.MockResultSet;

/**
 * Example test for {@link Bookstore}. Demonstrates the usage of 
 * {@link com.mockrunner.jdbc.JDBCTestModule} 
 * resp. {@link com.mockrunner.jdbc.JDBCTestCaseAdapter}.
 * This is an example for the handling of {@link com.mockrunner.mock.jdbc.MockResultSet}.
 * The data that the JDBC code should receive when executing the <i>select</i>
 * statement is specified in the file <i>bookstore.txt</i>. Please note that we
 * do not pass a filled <code>List</code> to the order method in the
 * succesful order test, because the choice if a table row should
 * be in the result is done by SQL. The framework does not execute any
 * SQL. In the second test, we check the correct SQL string.
 * In the third test, we specify that the statement should raise an
 * SQL exception (to simulate a database error) and verify, that
 * the transaction is rolled back.
 */
public class BookstoreTest extends JDBCTestCaseAdapter
{
    public void testSuccessfulOrder() throws Exception
    {
        FileResultSetFactory factory = new FileResultSetFactory("src/com/mockrunner/example/jdbc/bookstore.txt");
        factory.setFirstLineContainsColumnNames(true);
        MockResultSet result = getPreparedStatementResultSetHandler().createResultSet("bookresult", factory);    
        //System.out.println(result.toString());
        getStatementResultSetHandler().prepareResultSet("select isbn, quantity", result);
        List resultList = Bookstore.order(getJDBCMockObjectFactory().getMockConnection(), new ArrayList());
        assertTrue(resultList.size() == 4);
        assertTrue(resultList.contains("1234567890"));
        assertTrue(resultList.contains("1111111111"));
        assertTrue(resultList.contains("1212121212"));
        assertTrue(resultList.contains("3333333333"));
        verifyResultSetRow("bookresult", 1, new String[] {"1234567890", "0"});
        verifyResultSetRow("bookresult", 2, new String[] {"1111111111", "4"});
        verifyResultSetRow("bookresult", 3, new String[] {"0987654321", "0"});
        verifyResultSetRow("bookresult", 4, new String[] {"1212121212", "2"});
        verifyResultSetRow("bookresult", 5, new String[] {"3333333333", "0"});
        verifyCommitted();
        verifyAllResultSetsClosed();
        verifyAllStatementsClosed();
    }
    
    public void testCorrectSQL() throws Exception
    {
        MockResultSet result = getStatementResultSetHandler().createResultSet();    
        getStatementResultSetHandler().prepareResultSet("select isbn, quantity", result);
        List orderList = new ArrayList();
        orderList.add("1234567890");
        orderList.add("1111111111");
        Bookstore.order(getJDBCMockObjectFactory().getMockConnection(), orderList);
        verifySQLStatementExecuted("select isbn, quantity from books where (isbn=1234567890 or isbn=1111111111)");
    }
    
    public void testException() throws Exception    
    {
        getStatementResultSetHandler().prepareThrowsSQLException("select isbn, quantity");
        Bookstore.order(getJDBCMockObjectFactory().getMockConnection(), new ArrayList());
        verifyRolledBack();
        verifyAllResultSetsClosed();
        verifyAllStatementsClosed();
    }
}
