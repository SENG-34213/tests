package com.mentify.tests.material;

import java.net.URI;
import java.util.Locale;

final class MaterialDownloadUrlPolicy {

    private MaterialDownloadUrlPolicy() {
    }

    static URI validate(String downloadUrl, String allowedHost) {
        return validate(downloadUrl, allowedHost, false);
    }

    static URI validate(String downloadUrl, String allowedHost, boolean allowLocalHttp) {
        if (downloadUrl == null || downloadUrl.isBlank()) {
            throw new IllegalArgumentException("A presigned download URL is required");
        }
        if (allowedHost == null || allowedHost.isBlank()) {
            throw new IllegalArgumentException("An approved object-storage host is required");
        }

        URI uri = URI.create(downloadUrl);
        String scheme = uri.getScheme();
        String host = uri.getHost();
        String normalizedAllowedHost = allowedHost.trim().toLowerCase(Locale.ROOT);
        boolean secureHttps = "https".equalsIgnoreCase(scheme)
                && (uri.getPort() == -1 || uri.getPort() == 443)
                && normalizedAllowedHost.equals(host == null ? null : host.toLowerCase(Locale.ROOT));
        boolean explicitlyAllowedLocalHttp = allowLocalHttp
                && "http".equalsIgnoreCase(scheme)
                && isLoopbackHost(host)
                && uri.getPort() > 0
                && normalizedAllowedHost.equals((host + ":" + uri.getPort()).toLowerCase(Locale.ROOT));

        if ((!secureHttps && !explicitlyAllowedLocalHttp)
                || host == null
                || uri.getUserInfo() != null
                || uri.getFragment() != null) {
            throw new IllegalArgumentException("Presigned URL is outside the explicitly approved storage origin");
        }
        return uri;
    }

    private static boolean isLoopbackHost(String host) {
        return host != null && (host.equalsIgnoreCase("localhost")
                || host.equals("127.0.0.1")
                || host.equals("[::1]")
                || host.equals("::1"));
    }
}
