package com.org.customer.jdbcsupport;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.joda.time.LocalDate;
import org.springframework.jdbc.support.JdbcUtils;

public class jdbcsupport {
	
    public static Long getLong(final ResultSet rs, final String columnName) throws SQLException {
    	return (Long) JdbcUtils.getResultSetValue(rs, rs.findColumn(columnName), Long.class);
    }
    
    public static LocalDate getLocalDate(final ResultSet rs, final String columnName) throws SQLException {
        LocalDate localDate = null;
        final Date dateValue = rs.getDate(columnName);
        if (dateValue != null) {
            localDate = new LocalDate(dateValue);
        }
        return localDate;
    }
    public static Integer getInteger(final ResultSet rs, final String columnName) throws SQLException {
        return (Integer) JdbcUtils.getResultSetValue(rs, rs.findColumn(columnName), Integer.class);
    }

	   
}
