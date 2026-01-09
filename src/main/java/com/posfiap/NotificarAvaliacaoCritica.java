package com.posfiap;


import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

import com.posfiap.dto.AvaliacaoCriticaDTO;
import com.posfiap.repository.UsuarioNotificacaoRepository;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.*;

import java.io.IOException;
import java.util.Optional;

/**
 * Azure Functions with HTTP Trigger.
 */
public class NotificarAvaliacaoCritica {

    @FunctionName("NotificarAvaliacaoCritica")
    public HttpResponseMessage run(
            @HttpTrigger(
                    name = "req",
                    methods = {HttpMethod.POST},
                    authLevel = AuthorizationLevel.FUNCTION
            )
            HttpRequestMessage<Optional<AvaliacaoCriticaDTO>> request,
            ExecutionContext context
    ) {

        context.getLogger().info("Iniciando envio de e-mail de avaliação crítica");



        AvaliacaoCriticaDTO body = request.getBody().orElse(null);

        UsuarioNotificacaoRepository repo = new UsuarioNotificacaoRepository();
        String email = repo.buscarEmailPorId(1);

        if (body == null || email == null || body.getDescricao() == null) {
            return request
                    .createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Email e descrição são obrigatórios")
                    .build();
        }

        try {
            enviarEmail(email, body.getDescricao());
            return request
                    .createResponseBuilder(HttpStatus.OK)
                    .body("Notificação enviada com sucesso")
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
}
