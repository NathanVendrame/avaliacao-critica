package com.posfiap;


import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.*;

import java.io.IOException;
import java.util.Optional;

/**
 * Azure Functions with HTTP Trigger.
 */
public class NotificarAvaliacaoCritica {
    /**
     * This function listens at endpoint "/api/NotificarAvaliacaoCritica". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/NotificarAvaliacaoCritica
     * 2. curl {your host}/api/NotificarAvaliacaoCritica?name=HTTP%20Query
     */
    @FunctionName("NotificarAvaliacaoCritica")
    public HttpResponseMessage run(
            @HttpTrigger(
                    name = "req",
                    methods = {HttpMethod.POST},
                    authLevel = AuthorizationLevel.FUNCTION
            )
            HttpRequestMessage<Optional<NotificacaoRequest>> request,
            ExecutionContext context
    ) {

        context.getLogger().info("Iniciando envio de e-mail de avaliação crítica");

        NotificacaoRequest body = request.getBody().orElse(null);

        if (body == null || body.getEmail() == null || body.getDescricao() == null) {
            return request
                    .createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Email e descrição são obrigatórios")
                    .build();
        }

        try {
            enviarEmail(body.getEmail(), body.getDescricao());
            return request
                    .createResponseBuilder(HttpStatus.OK)
                    .body("Notificação enviada com sucesso teste github actions")
                    .build();
        } catch (Exception e) {
            context.getLogger().severe("Erro ao enviar email: " + e.getMessage());
            return request
                    .createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao enviar notificação")
                    .build();
        }
    }

    private void enviarEmail(String emailDestino, String descricao) throws IOException {

        String apiKey = System.getenv("SENDGRID_API_KEY");
        String emailRemetente = System.getenv("EMAIL_REMETENTE");

        Email from = new Email(emailRemetente);
        Email to = new Email(emailDestino);
        String subject = "⚠ Avaliação Crítica Recebida";

        Content content = new Content(
                "text/plain",
                "Uma avaliação crítica foi registrada:\n\nDescrição:\n" + descricao
        );

        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();

        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        sg.api(request);
    }

    public class NotificacaoRequest {

        private String descricao;
        private String email;

        public String getDescricao() {
            return descricao;
        }

        public void setDescricao(String descricao) {
            this.descricao = descricao;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}
