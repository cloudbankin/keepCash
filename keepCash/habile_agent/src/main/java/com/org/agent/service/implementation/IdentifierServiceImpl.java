package com.org.agent.service.implementation;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.org.agent.command.CommandProcessingResultBuilder;
import com.org.agent.command.DocumentCommand;
import com.org.agent.content.ContentRepository;
import com.org.agent.content.ContentRepositoryFactory;
import com.org.agent.exception.InvalidEntityTypeForDocumentManagementException;
import com.org.agent.exception.PlatformDataIntegrityException;
import com.org.agent.model.Identifier;
import com.org.agent.repository.IdentifierRepository;
import com.org.agent.service.IdentifierService;
import com.org.agent.validation.DocumentCommandValidator;
@Component
public class IdentifierServiceImpl implements IdentifierService{

	private final static Logger logger = LoggerFactory.getLogger(IdentifierServiceImpl.class);
	
	private final IdentifierRepository identifierRepository;
	private final ContentRepositoryFactory contentRepositoryFactory;

    @Autowired
    public IdentifierServiceImpl(final IdentifierRepository identifierRepository, final ContentRepositoryFactory contentRepositoryFactory) {
        this.identifierRepository = identifierRepository;
        this.contentRepositoryFactory = contentRepositoryFactory;
    }

    @Transactional
    @Override
    public Long createIdentifier(final DocumentCommand documentCommand, final InputStream inputStream, String identifierType, String IdentifierId) {
        try {
            //this.context.authenticatedUser();

            final DocumentCommandValidator validator = new DocumentCommandValidator(documentCommand);
            validateParentEntityType(documentCommand);
            validator.validateForCreate();

            final ContentRepository contentRepository = this.contentRepositoryFactory.getRepository();
            final String fileLocation = contentRepository.saveFile(inputStream, documentCommand);
            final Identifier identifier = Identifier.createNew(documentCommand.getParentEntityType(), documentCommand.getParentEntityId(),identifierType, IdentifierId, 
                    documentCommand.getName(), documentCommand.getFileName(), documentCommand.getSize(), documentCommand.getType(),
                    documentCommand.getDescription(), fileLocation, contentRepository.getStorageType());

            this.identifierRepository.save(identifier);
            
            return identifier.getId();
            
        } catch (final DataIntegrityViolationException dve) {
            logger.error(dve.getMessage(), dve);
            throw new PlatformDataIntegrityException("error.msg.document.unknown.data.integrity.issue",
                    "Unknown data integrity issue with resource.");
        }
    }

  
    private void validateParentEntityType(final DocumentCommand documentCommand) {
        if (!checkValidEntityType(documentCommand.getParentEntityType())) { throw new InvalidEntityTypeForDocumentManagementException(
                documentCommand.getParentEntityType()); }
    }

    private static boolean checkValidEntityType(final String entityType) {
        for (final DOCUMENT_MANAGEMENT_ENTITY entities : DOCUMENT_MANAGEMENT_ENTITY.values()) {
            if (entities.name().equalsIgnoreCase(entityType)) { return true; }
        }
        return false;
    }

    public static enum DOCUMENT_MANAGEMENT_ENTITY {
        CLIENTS, CLIENT_IDENTIFIERS, STAFF, LOANS, SAVINGS, GROUPS,IMPORT, AGENT;

        @Override
        public String toString() {
            return name().toString().toLowerCase();
        }
    }
}
