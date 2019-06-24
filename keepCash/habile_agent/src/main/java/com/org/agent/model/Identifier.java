package com.org.agent.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;

import com.org.agent.command.DocumentCommand;
import com.org.agent.core.AbstractPersistableCustom;
import com.org.agent.core.StorageType;


@Entity
@Table(name = "hab_agent_identifier")
public class Identifier extends AbstractPersistableCustom{

	 	@Column(name = "parent_entity_type", length = 50)
	    private String parentEntityType;

	    @Column(name = "parent_entity_id", length = 50)
	    private Long parentEntityId;

	    @Column(name = "identifier_type", length = 50)
	    private String identifierType;

	    @Column(name = "identifier_id", length = 50)
	    private String identifierId;
	    
	    @Column(name = "name", length = 250)
	    private String name;

	    @Column(name = "file_name", length = 250)
	    private String fileName;

	    @Column(name = "size")
	    private Long size;

	    @Column(name = "type", length = 50)
	    private String type;

	    @Column(name = "description", length = 1000)
	    private String description;

	    @Column(name = "location", length = 500)
	    private String location;

	    @Column(name = "storage_type_enum")
	    private Integer storageType;

	    public Identifier() {}

	    public static Identifier createNew(final String parentEntityType, final Long parentEntityId, final String identifierType, final String identifierId
	    		,final String name, final String fileName,
	            final Long size, final String type, final String description, final String location, final StorageType storageType) {
	        return new Identifier(parentEntityType, parentEntityId, identifierType, identifierId, name, fileName, size, type, description, location, storageType);
	    }

	    private Identifier(final String parentEntityType, final Long parentEntityId,final String identifierType, final String identifierId, final String name, final String fileName, final Long size,
	            final String type, final String description, final String location, final StorageType storageType) {
	        this.parentEntityType = StringUtils.defaultIfEmpty(parentEntityType, null);
	        this.parentEntityId = parentEntityId;
	        this.identifierType = identifierType;
	        this.identifierId = identifierId;
	        this.name = StringUtils.defaultIfEmpty(name, null);
	        this.fileName = StringUtils.defaultIfEmpty(fileName, null);
	        this.size = size;
	        this.type = StringUtils.defaultIfEmpty(type, null);
	        this.description = StringUtils.defaultIfEmpty(description, null);
	        this.location = StringUtils.defaultIfEmpty(location, null);
	        this.storageType = 1;
	    }

	    public void update(final DocumentCommand command) {
	        if (command.isDescriptionChanged()) {
	            this.description = command.getDescription();
	        }
	        if (command.isFileNameChanged()) {
	            this.fileName = command.getFileName();
	        }
	        if (command.isFileTypeChanged()) {
	            this.type = command.getType();
	        }
	        if (command.isLocationChanged()) {
	            this.location = command.getLocation();
	        }
	        if (command.isNameChanged()) {
	            this.name = command.getName();
	        }
	        if (command.isSizeChanged()) {
	            this.size = command.getSize();
	        }
	    }

	    public String getParentEntityType() {
	        return this.parentEntityType;
	    }

	    public void setParentEntityType(final String parentEntityType) {
	        this.parentEntityType = parentEntityType;
	    }

	    public Long getParentEntityId() {
	        return this.parentEntityId;
	    }

	    public void setParentEntityId(final Long parentEntityId) {
	        this.parentEntityId = parentEntityId;
	    }
	    
	    

	    public String getIdentifierType() {
			return identifierType;
		}

		public void setIdentifierType(String identifierType) {
			this.identifierType = identifierType;
		}

	

		public String getIdentifierId() {
			return identifierId;
		}

		public void setIdentifierId(String identifierId) {
			this.identifierId = identifierId;
		}

		public String getName() {
	        return this.name;
	    }

	    public void setName(final String name) {
	        this.name = name;
	    }

	    public String getFileName() {
	        return this.fileName;
	    }

	    public void setFileName(final String fileName) {
	        this.fileName = fileName;
	    }

	    public Long getSize() {
	        return this.size;
	    }

	    public void setSize(final Long size) {
	        this.size = size;
	    }

	    public String getType() {
	        return this.type;
	    }

	    public void setType(final String type) {
	        this.type = type;
	    }

	    public String getDescription() {
	        return this.description;
	    }

	    public void setDescription(final String description) {
	        this.description = description;
	    }

	    public String getLocation() {
	        return this.location;
	    }

	    public void setLocation(final String location) {
	        this.location = location;
	    }

	    public StorageType storageType() {
	        return StorageType.fromInt(this.storageType);
	    }	    
	    
	    
}



