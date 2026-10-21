package com.mentify.tests.communication;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CommunicationAttachmentAuthenticationIT extends CommunicationIntegrationTestSupport {

    private static final String UNKNOWN_ID = "00000000-0000-0000-0000-000000000000";

    @Test
    void shouldRejectUnauthenticatedMessageAttachmentUploadAndDownload() {
        assertEquals(401, communicationClient.uploadMessageAttachment(UNKNOWN_ID, null, null).statusCode());
        assertEquals(401, communicationClient.downloadMessageAttachment(
                UNKNOWN_ID, UNKNOWN_ID, UNKNOWN_ID, null).statusCode());
    }
}
