package com.posfiap;

import java.util.*;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import com.posfiap.dto.AvaliacaoCriticaDTO;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.ObjectMapper;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Avaliacao {

    @FunctionName("ReceberAvaliacao")
    public HttpResponseMessage run(
            @HttpTrigger(
                    name = "req",
                    methods = {HttpMethod.POST},
                    authLevel = AuthorizationLevel.FUNCTION
            )
            HttpRequestMessage<Optional<AvaliacaoRequest>> request,
            ExecutionContext context
    ) {

        context.getLogger().info("Recebendo avaliação");

        AvaliacaoRequest body = request.getBody().orElse(null);

        if (body == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Corpo da requisição inválido")
                    .build();
        }

        if (body.getNota() == null || body.getNota() < 0 || body.getNota() > 10) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Nota deve estar entre 0 e 10")
                    .build();
        }

        if (body.getDescricao() == null || body.getDescricao().isBlank()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Descrição é obrigatória")
                    .build();
        }

        if (body.getAlunoId() == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Id do aluno é obrigatório")
                    .build();
        }

        context.getLogger().info(
                "Avaliação recebida - Aluno: " + body.getAlunoId() +
                        ", Nota: " + body.getNota()
        );

        ServiceBusSenderClient sender =
                new ServiceBusClientBuilder()
                        .connectionString(System.getenv("SERVICEBUS_CONNECTION"))
                        .sender()
                        .topicName(System.getenv("SERVICEBUS_TOPIC"))
                        .buildClient();

        AvaliacaoCriticaDTO avaliacaoCriticaDTO = new AvaliacaoCriticaDTO(body.getDescricao(), body.getNota());
        ObjectMapper mapper = new ObjectMapper();
        String payload = mapper.writeValueAsString(avaliacaoCriticaDTO);

        sender.sendMessage(new ServiceBusMessage(payload));
        sender.close();

        return request.createResponseBuilder(HttpStatus.CREATED)
                .body("Avaliação registrada com sucesso")
                .build();
    }

    @Setter
    @Getter
    public static class AvaliacaoRequest {

        private Integer nota;
        private String descricao;
        private Integer alunoId;

    }
}
