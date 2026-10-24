package com.mentify.tests.material;

import java.net.URI;
import java.util.Locale;

final class MaterialDownloadUrlPolicy {

    private MaterialDownloadUrlPolicy() {
    }

    static URI validate(String downloadUrl, String allowedHost) {
        if (downloadUrl == null || downloadUrl.isBlank()) {
            throw new IllegalArgumentException("A presigned download URL is required");
        }
        if (allowedHost == null || allowedHost.isBlank()) {
            throw new IllegalArgumentException("An approved object-storage host is required");
        }

        URI uri = URI.create(downloadUrl);
        if (!"https".equalsIgnoreCase(uri.getScheme())
                || uri.getHost() == null
                || uri.getUserInfo() != null
                || uri.getFragment() != null
                || (uri.getPort() != -1 && uri.getPort() != 443)
                || !allowedHost.trim().toLowerCase(Locale.ROOT)
                        .equals(uri.getHost().toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Presigned URL is outside the explicitly approved HTTPS host");
        }
        return uri;
    }
}
