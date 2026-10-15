package com.mentify.tests.exam.model;

import java.util.List;

public record BatchMarksEntryRequest(List<MarkEntryRequest> results) {
}
