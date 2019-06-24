
package com.org.agent.controller;

import java.io.InputStream;
import java.net.URISyntaxException;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.org.agent.command.CommandProcessingResult;
import com.org.agent.command.DocumentCommand;
import com.org.agent.service.DocumentManagementService;

@RestController
@RequestMapping("/document")
public class DocumentManagementController {

@Autowired   
    private final DocumentManagementService documentManagementService;
    //private final ToApiJsonSerializer<DocumentManagementData> toApiJsonSerializer;

    @Autowired
    public DocumentManagementController(final DocumentManagementService documentManagementService) {
        this.documentManagementService = documentManagementService;
       // this.toApiJsonSerializer = toApiJsonSerializer;
    }

    @PostMapping(value= "/upload", consumes = {MediaType.MULTIPART_FORM_DATA}, produces = {MediaType.APPLICATION_JSON})
    public CommandProcessingResult createDocument(@RequestParam("user-file") MultipartFile fileDetails, @FormDataParam("user-file") final InputStream inputStream,
    		@FormDataParam("user-file") final FormDataBodyPart bodyPart, @PathParam("entityType") final String entityType, @PathParam("entityId") final Long entityId,
            @HeaderParam("Content-Length") final Long fileSize,  @FormDataParam("name") final String name, @FormDataParam("name") final String description) throws URISyntaxException{
    	
        final DocumentCommand documentCommand = new DocumentCommand(null, null, entityType, entityId, name, fileDetails.getOriginalFilename(),
                fileSize, bodyPart.getMediaType().toString(), description, null);

        final Long documentId = this.documentManagementService.createDocument(documentCommand, inputStream);
        return CommandProcessingResult.resourceResult(documentId, null);

    }

   
}