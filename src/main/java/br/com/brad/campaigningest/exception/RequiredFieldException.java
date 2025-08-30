package br.com.brad.campaigningest.exception;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RequiredFieldException extends RuntimeException {
    private List<String> fields;
}
