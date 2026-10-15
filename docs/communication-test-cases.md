# TEST-016 communication test cases

| ID | Area | Expected result |
| --- | --- | --- |
| TC-TEST-016-01 | Readiness | Required services become reachable or fail with a clear readiness error. |
| TC-TEST-016-02 | `/me` | Authenticated user identity is returned. |
| TC-TEST-016-03 | `/me` auth | Missing authentication returns 401. |
| TC-TEST-016-04 | Groups auth | Protected group operations reject missing authentication. |
| TC-TEST-016-05 | Messages auth | Protected message operations reject missing authentication. |
| TC-TEST-016-06 | Settings auth | Settings reject missing authentication. |
| TC-TEST-016-07 | Group create | Teacher/admin group creation succeeds with valid data. |
| TC-TEST-016-08 | Group create role | Student group creation returns 403. |
| TC-TEST-016-09 | Group create validation | Missing course/name is rejected without a 5xx response. |
| TC-TEST-016-10 | Group list | Current user groups can be listed. |
| TC-TEST-016-11 | Group retrieve | A group can be retrieved by ID. |
| TC-TEST-016-12 | Group members | Active members can be listed. |
| TC-TEST-016-13 | Add student | An authorized teacher/admin can add a student. |
| TC-TEST-016-14 | Course membership | Course student membership uses the course endpoint. |
| TC-TEST-016-15 | Archive | An authorized user can archive a group. |
| TC-TEST-016-16 | Unarchive | An authorized user can unarchive a group. |
| TC-TEST-016-17 | Group unknown ID | Unknown group IDs do not produce 5xx responses. |
| TC-TEST-016-18 | Message send | A valid message is created. |
| TC-TEST-016-19 | Message validation | Blank or missing content is rejected. |
| TC-TEST-016-20 | Message history | History returns a paginated response. |
| TC-TEST-016-21 | Pagination | Invalid page/size values are rejected locally or by the service. |
| TC-TEST-016-22 | Message update | The sender can update message content. |
| TC-TEST-016-23 | Message delete ME | The message can be deleted for the current user. |
| TC-TEST-016-24 | Message delete EVERYONE | An authorized sender can delete a message for everyone. |
| TC-TEST-016-25 | Message unknown ID | Unknown message/group IDs do not produce 5xx responses. |
| TC-TEST-016-26 | Settings get | An authenticated user can read own settings. |
| TC-TEST-016-27 | Settings update | An authenticated user can update own settings. |
| TC-TEST-016-28 | Settings isolation | Settings are scoped to the authenticated user. |
| TC-TEST-016-29 | Role authorization | Student cannot manage groups. |
| TC-TEST-016-30 | Test isolation | Write data uses unique values and approved fixtures. |
| TC-TEST-016-31 | Documentation | Endpoints, roles, fixtures, cleanup, scopes, and commands are documented. |
