/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.org.agent.service.implementation;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.org.agent.command.DocumentCommand;
import com.org.agent.content.ContentRepository;
import com.org.agent.content.ContentRepositoryFactory;
import com.org.agent.exception.InvalidEntityTypeForDocumentManagementException;
import com.org.agent.exception.PlatformDataIntegrityException;
import com.org.agent.model.Document;
import com.org.agent.repository.DocumentManagementRepository;
import com.org.agent.service.DocumentManagementService;
import com.org.agent.validation.DocumentCommandValidator;

@Service
public class DocumentManagementServiceImpl implements DocumentManagementService {

    private final static Logger logger = LoggerFactory.getLogger(DocumentManagementServiceImpl.class);

    private final DocumentManagementRepository documentManagementRepository;
    private final ContentRepositoryFactory contentRepositoryFactory;

    @Autowired
    public DocumentManagementServiceImpl(final DocumentManagementRepository documentManagementRepository, final ContentRepositoryFactory contentRepositoryFactory) {
        this.documentManagementRepository = documentManagementRepository;
        this.contentRepositoryFactory = contentRepositoryFactory;
    }

    @Transactional
    @Override
    public Long createDocument(final DocumentCommand documentCommand, final InputStream inputStream) {
        try {
            //this.context.authenticatedUser();

            final DocumentCommandValidator validator = new DocumentCommandValidator(documentCommand);
            validateParentEntityType(documentCommand);
            validator.validateForCreate();

            final ContentRepository contentRepository = this.contentRepositoryFactory.getRepository();
            final String fileLocation = contentRepository.saveFile(inputStream, documentCommand);
            final Document document = Document.createNew(documentCommand.getParentEntityType(), documentCommand.getParentEntityId(),
                    documentCommand.getName(), documentCommand.getFileName(), documentCommand.getSize(), documentCommand.getType(),
                    documentCommand.getDescription(), fileLocation, contentRepository.getStorageType());

            this.documentManagementRepository.save(document);
            return document.getId();
            
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

    /*** Entities for document Management **/
    public static enum DOCUMENT_MANAGEMENT_ENTITY {
        CLIENTS, CLIENT_IDENTIFIERS, STAFF, LOANS, SAVINGS, GROUPS,IMPORT, AGENT;

        @Override
        public String toString() {
            return name().toString().toLowerCase();
        }
    }
}