package br.com.brad.campaigningest.exception.handler;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.UUID;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.brad.campaigningest.exception.RequiredFieldException;

@RestControllerAdvice
public class CampaingIngestGlobalExceptionHandler {

    private static final String TYPE_400 = "https://httpstatuses.io/400";

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RequiredFieldException.class)
    public ProblemDetail requiredFieldException(RequiredFieldException exception, HttpServletRequest request) {
        var pd = getProblemDetail(HttpStatus.BAD_REQUEST, "Erro de validação no corpo da requisição.");
        enrichProblemDetail(pd, request, TYPE_400, "Validation failed");
        pd.setProperty("errors", exception.getFields());

        return pd;
    }

    private static ProblemDetail getProblemDetail(HttpStatus httpStatus, String errorMessage) {
        return ProblemDetail.forStatusAndDetail(httpStatus, errorMessage);
    }

    private void enrichProblemDetail(ProblemDetail pd, HttpServletRequest request, String type, String title) {
        if (StringUtils.hasText(type)) {
            pd.setType(URI.create(type));
        }
        if (StringUtils.hasText(title)) {
            pd.setTitle(title);
        }
        pd.setProperty("timestamp", OffsetDateTime.now().toString());
        pd.setProperty("path", request.getRequestURI());
        pd.setProperty("traceId", getOrCreateTraceId(request));
    }

    private String getOrCreateTraceId(HttpServletRequest request) {
        // TODO jogar a responsabilidade de gerar o traceID para o Spring sugestao Spring Sleuth
        var attr = request.getAttribute("traceId");
        if (attr instanceof String s && StringUtils.hasText(s)) {
            return s;
        }
        var generated = UUID.randomUUID().toString();
        request.setAttribute("traceId", generated);
        return generated;
    }
}
