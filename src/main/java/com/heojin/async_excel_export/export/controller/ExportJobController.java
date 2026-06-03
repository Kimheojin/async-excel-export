package com.heojin.async_excel_export.export.controller;

import com.heojin.async_excel_export.export.config.ExportProperties;
import com.heojin.async_excel_export.export.dto.ExportJobCreateRequest;
import com.heojin.async_excel_export.export.dto.ExportJobCreateResponse;
import com.heojin.async_excel_export.export.dto.ExportJobResponse;
import com.heojin.async_excel_export.export.domain.ExportJob;
import com.heojin.async_excel_export.export.service.ExportDownloadService.DownloadFile;
import com.heojin.async_excel_export.export.service.ExportDownloadService;
import com.heojin.async_excel_export.export.service.ExportJobService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/export-jobs")
@RequiredArgsConstructor
public class ExportJobController {

    private static final String CLIENT_ID_COOKIE_NAME = "export_client_id";

    private final ExportJobService exportJobService;
    private final ExportDownloadService exportDownloadService;
    private final ExportProperties exportProperties;

    @PostMapping
    public ResponseEntity<ExportJobCreateResponse> create(
            @RequestBody(required = false) ExportJobCreateRequest createRequest,
            HttpServletRequest request
    ) {
        ClientCookie clientCookie = getOrCreateClientCookie(request);
        String fileName = createRequest == null ? null : createRequest.fileName();
        ExportJob job = exportJobService.create(clientCookie.clientId(), fileName);

        return ResponseEntity.ok()
                .headers(headersWithCookieIfNeeded(clientCookie))
                .body(ExportJobCreateResponse.from(job));
    }

    @GetMapping
    public ResponseEntity<List<ExportJobResponse>> findAll(HttpServletRequest request) {
        ClientCookie clientCookie = getOrCreateClientCookie(request);
        List<ExportJobResponse> response = exportJobService.findAllByClientId(clientCookie.clientId())
                .stream()
                .map(ExportJobResponse::from)
                .toList();

        return ResponseEntity.ok()
                .headers(headersWithCookieIfNeeded(clientCookie))
                .body(response);
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<ExportJobResponse> get(@PathVariable Long jobId, HttpServletRequest request) {
        ClientCookie clientCookie = getOrCreateClientCookie(request);
        ExportJob job = exportJobService.getByClientId(jobId, clientCookie.clientId());

        return ResponseEntity.ok()
                .headers(headersWithCookieIfNeeded(clientCookie))
                .body(ExportJobResponse.from(job));
    }

    @GetMapping("/{jobId}/download")
    public ResponseEntity<?> download(@PathVariable Long jobId, HttpServletRequest request) {
        ClientCookie clientCookie = getOrCreateClientCookie(request);
        DownloadFile downloadFile = exportDownloadService.getDownloadFile(jobId, clientCookie.clientId());
        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(downloadFile.fileName(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .headers(headersWithCookieIfNeeded(clientCookie))
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(downloadFile.resource());
    }

    private ClientCookie getOrCreateClientCookie(HttpServletRequest request) {
        String clientId = getCookieValue(request, CLIENT_ID_COOKIE_NAME);
        if (clientId != null) {
            return new ClientCookie(clientId, false);
        }

        return new ClientCookie(UUID.randomUUID().toString(), true);
    }

    private String getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        return Arrays.stream(cookies)
                .filter(cookie -> name.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private HttpHeaders headersWithCookieIfNeeded(ClientCookie clientCookie) {
        HttpHeaders headers = new HttpHeaders();
        if (clientCookie.created()) {
            ResponseCookie cookie = ResponseCookie.from(CLIENT_ID_COOKIE_NAME, clientCookie.clientId())
                    .path("/")
                    .httpOnly(true)
                    .sameSite("Lax")
                    .maxAge(Duration.ofHours(exportProperties.ttlHours()))
                    .build();
            headers.add(HttpHeaders.SET_COOKIE, cookie.toString());
        }
        return headers;
    }

    private record ClientCookie(String clientId, boolean created) {
    }
}
