package com.org.agent.service;

import java.io.InputStream;

import org.springframework.stereotype.Service;

import com.org.agent.command.DocumentCommand;

@Service
public interface IdentifierService {

	Long createIdentifier(DocumentCommand documentCommand, InputStream inputStream,  String identifierType, String IdentifierId);
}
