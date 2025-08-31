package br.com.brad.campaigningest.exception.handler;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.RequiredArgsConstructor;
import io.micrometer.tracing.Tracer;

import br.com.brad.campaigningest.exception.RequiredFieldException;

/**
 * Manipulador global de exceções da aplicação de ingestão de campanhas.
 *
 * <p>Anotado com {@link RestControllerAdvice}, intercepta exceções lançadas pelos
 * controladores e as transforma em respostas padronizadas no formato
 * {@link ProblemDetail} (RFC 7807).</p>
 *
 * <h2>Política de resposta</h2>
 * <ul>
 *   <li>Inclui propriedades adicionais em {@link ProblemDetail}: <code>timestamp</code>,
 *       <code>path</code> e <code>traceId</code> para facilitar o diagnóstico.</li>
 *   <li>Define, quando apropriado, os campos <em>type</em> (URI com a especificação do erro)
 *       e <em>title</em> (título curto e legível).</li>
 * </ul>
 *
 * <h2>Rastreamento</h2>
 * <p>O <code>traceId</code> é obtido do {@link Tracer} atual (Micrometer). Na ausência
 * de um <em>span</em> ativo, um UUID é gerado e adicionado como atributo da requisição
 * sob a chave <code>"traceId"</code>.</p>
 *
 * @since 1.0
 */
@RestControllerAdvice
@RequiredArgsConstructor
public class CampaingIngestGlobalExceptionHandler {

    /**
     * Componente de rastreamento para obtenção do <em>traceId</em> correlacionado.
     */
    private final Tracer tracer;

    /**
     * URI descritiva do status HTTP 400 a ser usada no campo {@code type} do {@link ProblemDetail}.
     */
    private static final String TYPE_400 = "https://httpstatuses.io/400";

    /**
     * Trata {@link RequiredFieldException} retornando uma resposta HTTP 400 (Bad Request)
     * no formato {@link ProblemDetail}, enriquecida com metadados e a lista de campos inválidos.
     *
     * <p>Propriedades adicionais definidas:</p>
     * <ul>
     *   <li><strong>type</strong>: {@value #TYPE_400}</li>
     *   <li><strong>title</strong>: "Validation failed"</li>
     *   <li><strong>timestamp</strong>: instante atual em ISO-8601</li>
     *   <li><strong>path</strong>: URI do recurso solicitado</li>
     *   <li><strong>traceId</strong>: identificador de correlação</li>
     *   <li><strong>errors</strong>: lista de campos obrigatórios ausentes/invalidos</li>
     * </ul>
     *
     * @param exception exceção de validação contendo os nomes dos campos problemáticos
     * @param request contexto HTTP atual utilizado para extrair o caminho e armazenar o <em>traceId</em>
     * @return um {@link ProblemDetail} representando o erro de validação
     * @see RequiredFieldException
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RequiredFieldException.class)
    public ProblemDetail requiredFieldException(RequiredFieldException exception, HttpServletRequest request) {
        var pd = getProblemDetail(HttpStatus.BAD_REQUEST, "Erro de validação no corpo da requisição.");
        enrichProblemDetail(pd, request, TYPE_400, "Validation failed");
        pd.setProperty("errors", exception.getFields());

        return pd;
    }

    /**
     * Cria uma instância de {@link ProblemDetail} com o {@link HttpStatus} e a mensagem detalhada informados.
     *
     * @param httpStatus código de status HTTP a ser aplicado na resposta
     * @param errorMessage descrição legível do problema
     * @return {@link ProblemDetail} básico com status e detalhe
     */
    private static ProblemDetail getProblemDetail(HttpStatus httpStatus, String errorMessage) {
        return ProblemDetail.forStatusAndDetail(httpStatus, errorMessage);
    }

    /**
     * Enriquecedor de {@link ProblemDetail} com metadados adicionais.
     *
     * <p>Define, quando presentes, os campos padronizados <em>type</em> e <em>title</em>.
     * Além disso, acrescenta propriedades customizadas:</p>
     * <ul>
     *   <li><strong>timestamp</strong>: data/hora atual (ISO-8601)</li>
     *   <li><strong>path</strong>: caminho da requisição</li>
     *   <li><strong>traceId</strong>: obtido do {@link Tracer} atual ou gerado via UUID</li>
     * </ul>
     *
     * @param pd objeto a ser enriquecido
     * @param request requisição HTTP de origem
     * @param type URI descritiva do tipo de erro (pode ser nula ou vazia para omitir)
     * @param title título curto do problema (pode ser nulo ou vazio para omitir)
     * @implNote Caso não exista um <em>span</em> corrente no {@link Tracer}, um UUID é gerado
     *           e armazenado em {@link HttpServletRequest#setAttribute(String, Object)} com a chave {@code "traceId"}.
     */
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

    /**
     * Obtém o identificador de rastreamento da requisição.
     *
     * <p>Se houver um <em>span</em> corrente no {@link Tracer}, retorna seu <em>traceId</em>.
     * Caso contrário, gera um novo UUID, associa-o à requisição e o retorna.</p>
     *
     * @param request requisição HTTP de onde o <em>traceId</em> pode ser lido/armazenado
     * @return o <em>traceId</em> corrente ou um novo UUID gerado
     */
    private String getOrCreateTraceId(HttpServletRequest request) {
        var span = tracer.currentSpan();
        if (Objects.nonNull(span)) {
            return span.context().traceId();
        }

        var generated = UUID.randomUUID().toString();
        request.setAttribute("traceId", generated);
        return generated;
    }
}
