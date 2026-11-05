package org.phoebus.olog.ai;

import org.phoebus.olog.AuthorizationService;
import org.phoebus.olog.AuthorizationService.ROLES;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/Olog/ai/ingest")
public class IngestController {

    private static final Logger logger = LoggerFactory.getLogger(IngestController.class);
    
    private final IngestService ingestService;
    private final AuthorizationService authorizationService;

    public IngestController(IngestService ingestService,
                            AuthorizationService authorizationService) {
        this.ingestService = ingestService;
        this.authorizationService = authorizationService;
    }

    // curl -u admin:adminPass -X POST "http://localhost:8080/Olog/ai/ingest/logs"
    @PostMapping("/logs")
    public ResponseEntity<?> ingestOperationLogs(Authentication authentication) {
        if (authentication == null
                || !authorizationService.isAuthorizedRole(authentication, ROLES.OLOG_ADMIN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only Olog administrators may run a full ingest.");
        }
 
        try {
            IngestService.IngestStatus status = ingestService.startIngestAll();
            logger.info("Full ingest started by '{}'", authentication.getName());
            return ResponseEntity.accepted().body(status);
        } catch (IngestService.IngestAlreadyRunningException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ingestService.getStatus());
        } catch (Exception e) {
            logger.error("Could not start full ingest", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Could not start ingest: " + e.getMessage());
        }
    }

    @GetMapping("/logs/status")
    public IngestService.IngestStatus ingestStatus() {
        return ingestService.getStatus();
    }
} 
