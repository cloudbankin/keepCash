package com.org.agent.service.implementation;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.org.agent.data.FaceAthenticationData;
import com.org.agent.service.FaceAthenticationService;	

@Component
public class FaceAthenticationServiceImpl implements FaceAthenticationService{

	@Autowired
	private final JdbcTemplate jdbcTemplate;
	
	@Autowired
	public FaceAthenticationServiceImpl(final JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	@Override
	public FaceAthenticationData getFaceIdValues() {
		final FaceAthenticationMapper mapper = new FaceAthenticationMapper();
		final String sql = "select " + mapper. Schema();
		return jdbcTemplate.queryForObject(sql, mapper, new Object[] {});	
	}
	
	private static class FaceAthenticationMapper implements RowMapper<FaceAthenticationData>{
		
		public String Schema() {
			return " fa.license_key as licenseId, fa.public_key as publicId from hab_face_authentication fa where id = 1 ";
		}
		
		@Override
		public FaceAthenticationData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {
			final String licenseId = rs.getString("licenseId");
			final String publicId = rs.getString("publicId");			
			final FaceAthenticationData faceAthenticationData = new FaceAthenticationData(licenseId, publicId);

			return faceAthenticationData;
		}

	}
}
