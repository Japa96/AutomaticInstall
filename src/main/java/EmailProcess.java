import Utils.Modules;
import model.ResultTests;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.util.Properties;

public class EmailProcess{

    private static final Logger LOGGER = LogManager.getLogger(AgentAutomaticProcess.class.getName());

    public static void envioEmail(ResultTests resultTests) {

        PathConfig pathConfig = new PathConfig(new File(System.getProperty("user.dir")), true);

        // Recipient's email ID needs to be mentioned.
        String to = pathConfig.getEmail();

        // Sender's email ID needs to be mentioned
        String from = "nextlevelsenac@gmail.com";

        // Setup mail server
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.socketFactory.port", "465");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.port", "465");

        // Get the Session object.// and pass
        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {

            protected PasswordAuthentication getPasswordAuthentication() {
                //e-Mail: nextlevelsenac@gmail.com
                //Senha: ncwrzmozfacjfiro
                //Senha de APP: xsvatsazvpvottjz

                return new PasswordAuthentication("nextlevelsenac@gmail.com", "xsvatsazvpvottjz");

            }

        });
        //session.setDebug(true);

        try {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            // Set Subject: header field
            message.setSubject("Automatic Process - Seu processamento finalizou com sucesso!");

            Multipart multipart = new MimeMultipart();

            StringBuffer texto = new StringBuffer();
            MimeBodyPart textPart = new MimeBodyPart();

            int modulo = pathConfig.getModulo();

            Modules.getProcess(modulo);

            texto.append("<h2 align='center'>Seu de ped_install está finalizado!</h2>");

            texto.append("<br/>O módulo de processamento é: " + Modules.getProcess(modulo) + "<br/>");
            texto.append("A quantidade de documentos processados de ped_install foi de: " + resultTests.getQuantidadeArquivos() + "<br/>");
            texto.append("Sucesso: " + resultTests.getSucesso() + "<br/>");
            texto.append("Falha: " + resultTests.getFalha() + "<br/>");
            texto.append("A próxima execução dos testes automatizados será no próximo dia a partir das 08:00. <br/>");
            textPart.setText(texto.toString(), "UTF-8", "html");
            multipart.addBodyPart(textPart);

            message.setContent(multipart);
            LOGGER.info("Enviando e-Mail...");
            Transport.send(message);
            LOGGER.info("e-Mail enviado com sucesso....");
            LOGGER.info("e-Mail enviado com sucesso");
        } catch (MessagingException mex) {
            LOGGER.error(mex);
        }
        }
    }
