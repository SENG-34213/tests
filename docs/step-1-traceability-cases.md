# Traceability cases added from the Step 1 audit

These cases map existing JUnit integration methods that were present in the test suite but missing from the case mapping CSV. Requirement references below use the supplied Mentify SRS. `SRS-NOT-EXPLICIT` means the SRS does not define that behavior as a functional requirement; it is retained as an API/security/operational test, not forced into an unrelated requirement. The mappings establish traceability only; they do not assert that live tests have run or passed.

| Case ID | Test class | Test method | Type | Service/package |
|---|---|---|---|---|
| TC-TRACE-001 | ApiClientIT | shouldReadGatewayHealthAfterReadinessPasses | integration | api |
| TC-TRACE-002 | AttendanceAdminIT | adminCanReadStudentSummary | integration | attendance |
| TC-TRACE-003 | AttendanceAdminIT | adminCanCreateAndRetrieveAttendanceSessionWhenWritesAreEnabled | integration | attendance |
| TC-TRACE-004 | AttendanceAuthenticationIT | protectedAttendanceRequestsRejectMissingAuthentication | security | attendance |
| TC-TRACE-005 | AttendanceAuthenticationIT | studentCannotManageSessionsOrReadAnotherStudentSummary | security | attendance |
| TC-TRACE-006 | AttendanceValidationIT | rejectsInvalidSessionAndRecordRequests | integration | attendance |
| TC-TRACE-007 | AttendanceValidationIT | unknownAttendanceResourcesReturnNotFound | integration | attendance |
| TC-TRACE-008 | CalendarAuthenticationIT | shouldRejectUpcomingCalendarRequestsWithoutAuthentication | security | calendar |
| TC-TRACE-009 | CalendarAuthenticationIT | shouldRejectEventCreationWithoutAuthentication | security | calendar |
| TC-TRACE-010 | CalendarAuthenticationIT | shouldAllowAnAuthenticatedStudentToQueryUpcomingEvents | security | calendar |
| TC-TRACE-011 | CalendarEventWorkflowIT | shouldAllowAdminToCreateAnEvent | integration | calendar |
| TC-TRACE-012 | CalendarEventWorkflowIT | shouldAllowTeacherToCancelAnEvent | integration | calendar |
| TC-TRACE-013 | CalendarEventWorkflowIT | shouldRejectStudentEventCreation | integration | calendar |
| TC-TRACE-014 | CalendarEventWorkflowIT | shouldRejectStudentEventUpdate | integration | calendar |
| TC-TRACE-015 | CalendarEventWorkflowIT | shouldRejectStudentEventCancellation | integration | calendar |
| TC-TRACE-016 | CalendarEventWorkflowIT | shouldRejectInvalidEventRequest | integration | calendar |
| TC-TRACE-017 | CalendarEventWorkflowIT | shouldRejectEventWithInvalidDateRange | integration | calendar |
| TC-TRACE-018 | CalendarVenueAuthorizationIT | shouldRequireAuthenticationToListVenues | security | calendar |
| TC-TRACE-019 | CommunicationAuthenticationIT | protectedCommunicationRequestsRejectMissingAuthentication | security | communication |
| TC-TRACE-020 | CommunicationAuthenticationIT | studentCannotManageGroups | security | communication |
| TC-TRACE-021 | CommunicationGroupWorkflowIT | teacherCanCreateInspectManageAndArchiveAGroup | integration | communication |
| TC-TRACE-022 | CommunicationGroupWorkflowIT | courseMembershipUsesTheCourseEndpoint | integration | communication |
| TC-TRACE-023 | CommunicationMessageWorkflowIT | teacherCanSendPageUpdateAndDeleteMessages | integration | communication |
| TC-TRACE-024 | CommunicationSettingsIT | authenticatedUserCanReadAndUpdateOwnSettings | integration | communication |
| TC-TRACE-025 | CommunicationValidationIT | invalidRequestsReturnClientErrors | integration | communication |
| TC-TRACE-026 | CommunicationValidationIT | unknownCommunicationIdsNeverProduceServerErrors | integration | communication |
| TC-TRACE-027 | CourseEnrollmentJourneyIT | studentCourseAndEnrollmentIdentifiersRemainConsistentThroughGateway | e2e | e2e |
| TC-TRACE-028 | CrossServiceAuthorizationIT | gatewayPreservesRoleRestrictionsAcrossUserAndEnrollmentServices | security | e2e |
| TC-TRACE-029 | GatewaySmokeIT | authenticatedUserTravelsThroughGatewayToUserService | e2e | e2e |
| TC-TRACE-030 | GatewaySmokeIT | missingGatewayTokenIsRejected | e2e | e2e |
| TC-TRACE-031 | ProviderJourneyIT | paymentNotificationAndLearningProviderJourneysRequireExplicitFixtures | e2e | e2e |
| TC-TRACE-032 | EnrollmentAuthenticationIT | shouldRejectStudentFromAnotherStudentsCourseList | security | enrollment |
| TC-TRACE-033 | BackendReadinessIT | shouldKeepRequiredBackendServicesReady | integration | environment |
| TC-TRACE-034 | ExamAuthorizationIT | nonStudentsCannotUseStudentExamDetailRoute | security | exam |
| TC-TRACE-035 | ExamStudentResultIT | enrolledStudentCanRetrieveOwnExamResults | integration | exam |
| TC-TRACE-036 | ExamValidationIT | invalidExamAndMarksPayloadsReturnClientErrors | integration | exam |
| TC-TRACE-037 | ExamValidationIT | unknownExamIdentifiersNeverProduceServerErrors | integration | exam |
| TC-TRACE-038 | MaterialAiSourcesIT | shouldRejectStudentAndUnauthenticatedAiSourceReads | integration | material |
| TC-TRACE-039 | MaterialAuthenticationIT | rejectsProtectedMaterialRequestsWithoutAuthentication | security | material |
| TC-TRACE-040 | MaterialAuthenticationIT | studentCannotManageMaterialsOrRunMaintenance | security | material |
| TC-TRACE-041 | MaterialAuthenticationIT | teacherCannotRunMaintenance | security | material |
| TC-TRACE-042 | MaterialLifecycleIT | enrolledStudentCanUploadSubmissionAttachmentMaterial | integration | material |
| TC-TRACE-043 | MaterialLifecycleIT | rejectsInvalidMaterialUploadsAndUpdates | integration | material |
| TC-TRACE-044 | MaterialLifecycleIT | unknownMaterialOperationsAreNotReportedAsServerErrors | integration | material |
| TC-TRACE-045 | MaterialLifecycleIT | adminCanAuditAndRunNonDestructiveCleanup | integration | material |
| TC-TRACE-046 | NotificationAuthorizationIT | unauthenticatedNotificationRequestsReturnUnauthorized | security | notification |
| TC-TRACE-047 | NotificationAuthorizationIT | notificationAccessIsAvailableToAuthenticatedUsersOnly | security | notification |
| TC-TRACE-048 | NotificationOwnershipIT | twoAuthenticatedUsersReceiveOnlyTheirOwnNotificationCollections | integration | notification |
| TC-TRACE-049 | PaymentAuthorizationIT | unauthenticatedPaymentRequestsReturnUnauthorized | security | payment |
| TC-TRACE-050 | PaymentAuthorizationIT | studentCanUseOwnEndpointsButCannotUseAdministratorEndpoints | security | payment |
| TC-TRACE-051 | PaymentAuthorizationIT | teacherCannotUseStudentOrAdministratorPaymentEndpoints | security | payment |
| TC-TRACE-052 | PaymentValidationIT | webhookWithoutSignatureIsRejected | integration | payment |
| TC-TRACE-053 | QuizAuthenticationIT | teacherCannotStartOrSubmitStudentAttempt | security | quiz |
| TC-TRACE-054 | QuizManagementIT | teacherManagesQuestionsAndPublishesQuiz | integration | quiz |
| TC-TRACE-055 | QuizManagementIT | unknownQuizAndQuestionResourcesReturnNotFound | integration | quiz |
| TC-TRACE-056 | QuizValidationIT | rejectsInvalidQuizRequests | integration | quiz |
| TC-TRACE-057 | QuizValidationIT | rejectsInvalidQuestionAndAnswerRequests | integration | quiz |
| TC-TRACE-058 | DownstreamFailureIT | controlledDownstreamFailureRequiresApprovedFaultInjection | integration | resilience |
| TC-TRACE-059 | ProviderFailureIT | providerFailureRequiresExplicitProviderFailureMode | integration | resilience |
| TC-TRACE-060 | AuthenticationIT | publicAndProtectedAuthenticationProbesFollowTheirSecurityRules | security | user |
| TC-TRACE-061 | AuthenticationIT | roleProbesMatchStudentAndAdministratorTokens | security | user |
| TC-TRACE-062 | AuthenticationIT | invalidAuthenticationPayloadsAreRejectedWithoutSensitiveOutput | security | user |
| TC-TRACE-063 | UserAuthorizationIT | studentsAndTeachersCannotPerformAdministratorOperations | security | user |
| TC-TRACE-064 | UserAuthorizationIT | protectedUserEndpointsRejectMissingAuthentication | security | user |
| TC-TRACE-065 | UserProfileIT | authenticatedUserCanRetrieveOwnProfileWithoutSensitiveFields | integration | user |

