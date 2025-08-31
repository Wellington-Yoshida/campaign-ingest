package br.com.brad.campaigningest.exception;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Exceção de regra de validação lançada quando um ou mais campos obrigatórios
 * não foram informados ou estão inválidos.
 *
 * <p>A lista {@code fields} contém os nomes dos campos que falharam na validação.
 * O chamador pode utilizá-la para construir mensagens de erro detalhadas ou
 * respostas HTTP 400.</p>
 *
 * <h2>Exemplo de uso</h2>
 * <pre>{@code
 * if (isBlank(request.getName())) {
 *     throw new RequiredFieldException(List.of("name"));
 * }
 * }</pre>
 *
 * @since 1.0
 */
@Getter
@AllArgsConstructor
public class RequiredFieldException extends RuntimeException {
    private List<String> fields;
}
