package com.posfiap;


import com.google.gson.Gson;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

import com.posfiap.dto.AvaliacaoCriticaDTO;
import com.posfiap.infrastructure.AppContext;
import com.posfiap.repository.UsuarioNotificacaoRepository;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.*;

import java.io.IOException;
import java.util.List;

/**
 * Azure Functions with HTTP Trigger.
 */
public class NotificarAvaliacaoCritica {

    @FunctionName("NotificarAvaliacaoCritica")
    public void run(
            @ServiceBusTopicTrigger(
                    name = "mensagem",
                    topicName = "%SERVICEBUS_TOPIC%",
                    subscriptionName = "%SERVICEBUS_SUBSCRIPTION%",
                    connection = "SERVICEBUS_CONNECTION"
            )
            String mensagem,
            ExecutionContext context
    ) {
        context.getLogger().info("Mensagem recebida: " + mensagem);
        context.getLogger().info("Iniciando envio de e-mail de avaliação crítica");

        Gson gson = new Gson();
        AvaliacaoCriticaDTO avaliacaoCriticaDTO = gson.fromJson(mensagem, AvaliacaoCriticaDTO.class);

        if (avaliacaoCriticaDTO.getNota() > 4) {
            return;
        }

        List<String> listaEmails;
        try {
            UsuarioNotificacaoRepository repo = new UsuarioNotificacaoRepository(AppContext.dataSource());
            listaEmails = repo.buscarEmailsParaNotificacao();
            context.getLogger().info("Mensagem recebida: " + mensagem);

        } catch (Exception e) {
            context.getLogger().severe("erro ao buscar email: " + e.getMessage());
        }

        try {
            enviarEmail("evident.pinniped.dvao@protectsmail.net", avaliacaoCriticaDTO.getDescricao());
        } catch (Exception e) {
            context.getLogger().severe("Erro ao enviar email: " + e.getMessage());
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
