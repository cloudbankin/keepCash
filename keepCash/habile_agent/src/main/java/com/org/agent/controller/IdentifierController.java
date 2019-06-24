package com.org.agent.controller;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
import com.org.agent.command.CommandProcessingResultBuilder;
import com.org.agent.command.DocumentCommand;
import com.org.agent.service.IdentifierService;

@RestController
@RequestMapping("/identifier")
public class IdentifierController {
	
	@Autowired   
    private final IdentifierService identifierService;
    //private final ToApiJsonSerializer<DocumentManagementData> toApiJsonSerializer;

    @Autowired
    public IdentifierController(final IdentifierService identifierService) {
        this.identifierService = identifierService;
       // this.toApiJsonSerializer = toApiJsonSerializer;
    }

   

    @PostMapping(value= "/upload", consumes = {MediaType.MULTIPART_FORM_DATA})
    public CommandProcessingResult createDocument(@QueryParam("entityType") final String entityType, @QueryParam("entityId") final Long entityId,
            @HeaderParam("Content-Length") final Long fileSize, @FormDataParam("user-file") final InputStream inputStream,
            @RequestParam("user-file") MultipartFile fileDetails,
            @FormDataParam("user-file") final FormDataBodyPart bodyPart,
            @FormDataParam("identifierType") final String identifierType, @FormDataParam("identifierId") final String identifierId) {

        final DocumentCommand documentCommand = new DocumentCommand(null, null, entityType, entityId, identifierType, fileDetails.getOriginalFilename(),
                fileSize, bodyPart.getMediaType().toString(), identifierType, null);

        final Long documentId = this.identifierService.createIdentifier(documentCommand, inputStream, identifierType, identifierId);
        return CommandProcessingResult.resourceResult(documentId, null);
        
    }
    
}
