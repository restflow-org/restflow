package org.restflow.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

public class PortableDB {

	public static int executeSqlScript(Connection connection, String sqlScript) 
			throws SQLException {
		
		int statementCount = 0;
		Statement statement = connection.createStatement();

		try {
			for (String sqlStatement : sqlScript.split(";")) {
				if (sqlStatement.trim().length() > 0) {
					statement.execute(sqlStatement);
					statementCount++;
				}
			}
			
		} catch (SQLException e) {
			throw e;
		} finally {
			statement.close();
		}
		
		return statementCount;
	}
	
	public static void setLongParameter(PreparedStatement statement, int parameter, Long value) 
			throws SQLException {
		
		statement.setLong(parameter, value);
	}

	public static void setNullableLongParameter(PreparedStatement statement, int parameter, Long value) 
			throws SQLException {
	
		if (value == null) {
			statement.setNull(parameter, java.sql.Types.BIGINT);
		} else {
			statement.setLong(parameter, value);
		}
	}

	public static void setBooleanParameter(PreparedStatement statement, int parameter, boolean value) 
		throws SQLException {
	
		statement.setBoolean(parameter, value);
	}

	public static void setStringParameter(PreparedStatement statement, int parameter, String value) 
			throws SQLException {
	
		statement.setString(parameter, value);
	}

	public static void setObjectParameter(PreparedStatement statement, int parameter, Object value) 
		throws SQLException {
	
		statement.setObject(parameter, value);
	}
	
	public static void setTimeStamp(PreparedStatement statement, int parameter, Timestamp value) 
			throws SQLException {
		
		statement.setTimestamp(parameter, value);
	}
	
	public static void setNullableTimeStamp(PreparedStatement statement, int parameter, Timestamp value) 
			throws SQLException {
	
		if (value == null) {
			statement.setNull(parameter, java.sql.Types.TIMESTAMP);
		} else {
			statement.setTimestamp(parameter, value);
		}
	}
	
	public static boolean databaseHasTable(Connection connection, String tableName) 
			throws SQLException {
		
		SQLException exception = null;
		
		try {
			PortableDB.getRowCountForTable(connection, tableName);	
		} catch (SQLException e) {
			exception = e;
		}
		
		return (exception == null);
	}

	public static int getRowCountForTable(Connection connection, String tableName) 
			throws SQLException {
		
		int count = -1;
	
		Statement statement = connection.createStatement();
		
		String sql = "SELECT COUNT(*) AS NumberOfRows FROM " + tableName;
		
		try {
			ResultSet result = statement.executeQuery(sql);
			result.next();
			count = result.getInt("NumberOfRows");
		} catch (SQLException e) {
			throw e;
		} finally {
			statement.close();
		}
		
		return count;
	}

	public static long getGeneratedID(Statement statement) throws SQLException {
		ResultSet resultSet = statement.getGeneratedKeys();
		resultSet.next();
		return resultSet.getLong(1);
	}
	
	public static String formatResultSet(ResultSet resultSet, String columns[]) throws SQLException {

		StringBuilder buffer = new StringBuilder();
		
		int widths[] = printHeadings(columns, buffer);
		while (resultSet.next()) {
			
			Object valueArray[] = new Object[columns.length];
			for (int i = 0; i < columns.length; i++) {
				String key = columns[i].trim();
				String value = resultSet.getString(key);
				if (value != null && value.length() > columns[i].length()) {
					valueArray[i] = value.substring(0, columns[i].length());
				} else { 
					valueArray[i] = value;
				}
			}
			
			printRow(widths, valueArray, buffer);
		}

		return buffer.toString();
	}
	
	public static final String dashes = "--------------------------------------------------------------------------------------------------";
	public static final String spaces = "                                                                                        ";

	public static int[] printHeadings(String columnTitle[], StringBuilder buffer) {
		
		int columnWidth[] = new int[columnTitle.length];
		StringBuilder titleLine = new StringBuilder();
		StringBuilder dashLine = new StringBuilder();
		
		for (int i = 0; i < columnTitle.length; i++) {
			titleLine.append(columnTitle[i]).append(' ');
			dashLine.append(dashes.substring(0, columnTitle[i].length())).append(' ');
			columnWidth[i] = columnTitle[i].length();
		}
		titleLine.append(PortableIO.EOL);
		dashLine.append(PortableIO.EOL);

		buffer.append(titleLine);
		buffer.append(dashLine);
		
		return columnWidth;
	}
	
	public static void printRow(int columnWidths[], Object values[], StringBuilder buffer) {
		
		for (int i = 0; i < values.length; i++) {
			String value = ""; 
			if (values[i] != null) {
				value = values[i].toString();
			}
			buffer.append(value);
			int spacing = columnWidths[i] - value.length() + 1;
			if (spacing > 0) {
				buffer.append(spaces.substring(0, spacing));
			}
		}
		
		buffer.append(PortableIO.EOL);
	}

	
	public static ResultSet queryTable(Statement statement, String tableName, String[] columns, String qualifier) throws SQLException {

		StringBuilder sqlBuilder = new StringBuilder("SELECT ");
		appendTrimmedStrings(sqlBuilder, columns, ", ");
		sqlBuilder.append(" FROM ")
			.append(tableName)
			.append(" ")
			.append(qualifier);

		ResultSet resultSet = statement.executeQuery(sqlBuilder.toString());

		return resultSet;
	}
	
	public static String dumpTable(Statement statement, String tableName, String[] columns, String qualifier) throws SQLException {
		ResultSet rs = queryTable(statement, tableName, columns, qualifier);
		return PortableDB.formatResultSet(rs, columns);
	}

	public static String dumpTable(Statement statement, String tableName, String[] columns) throws SQLException {
		return dumpTable(statement, tableName, columns, "");
	}
	
	public static StringBuilder appendTrimmedStrings(StringBuilder builder, String[] strings, String separator) {
		
		boolean isFirst = true;
		for (String string : strings) {
			if (!isFirst) {
				builder.append(separator);
			} else {
				isFirst = false;
			}
			builder.append(string.trim());
		}
		
		return builder;
	}

	public static String formatTwoColumnResultSetAsYamlMap(ResultSet resultSet) throws SQLException {
		
		StringBuilder buffer = new StringBuilder();
		
		while (resultSet.next()) {
			String key = resultSet.getString(1);
			String value = resultSet.getString(2);
			buffer.append(key).append(": ").append(value).append(PortableIO.EOL);
		}
		
		return buffer.toString();
	}
}
