package com.incept5.rest.service;


import com.incept5.rest.api.ExternalUser;
import com.incept5.rest.config.ApplicationConfig;
import com.incept5.rest.model.Role;
import com.incept5.rest.model.User;
import com.incept5.rest.model.VerificationToken;
import com.incept5.rest.service.data.EmailServiceTokenModel;
import com.incept5.rest.service.impl.MailSenderServiceImpl;
import org.apache.commons.codec.binary.Base64;
import org.apache.velocity.app.VelocityEngine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.Multipart;
import javax.mail.internet.MimeMessage;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author: Iain Porter
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:META-INF/spring/root-context.xml")
@ActiveProfiles(profiles = "dev")
@Transactional
public class MailSenderServiceTest extends BaseServiceTest {

    private MailSenderService mailService;

    private MockJavaMailSender mailSender;

    @Autowired
    VelocityEngine velocityEngine;

    @Autowired
    ApplicationConfig config;

    @Before
    public void setUpServices() {
        mailSender = new MockJavaMailSender();
        mailService = new MailSenderServiceImpl(mailSender, velocityEngine);
    }


    @Test
    public void sendVerificationEmail() throws Exception {
        ExternalUser externalUser = createUserWithRandomUserName(Role.authenticated);
        User user = userRepository.findByUuid(externalUser.getId());
        VerificationToken token = new VerificationToken(user,
                VerificationToken.VerificationTokenType.emailVerification);
        mailService.sendVerificationEmail(new EmailServiceTokenModel(user, token, config.getHostNameUrl()));
        List<MimeMessage> messages = mailSender.getMessages();
        assertThat(messages.size(), is(1));
        MimeMessage message = messages.get(0);
        assertThat(message.getAllRecipients()[0].toString(), is((user.getEmailAddress())));
        Multipart multipart = (Multipart)message.getContent();
        String content = (String)multipart.getBodyPart(0).getContent();
        assertThat(content, containsString(new String(Base64.encodeBase64(token.getToken().getBytes()))));
    }

    @Test
    public void sendRegistrationEmail() throws Exception {
        ExternalUser externalUser = createUserWithRandomUserName(Role.authenticated);
        User user = userRepository.findByUuid(externalUser.getId());
        VerificationToken token = new VerificationToken(user,
                VerificationToken.VerificationTokenType.emailRegistration);
        mailService.sendRegistrationEmail(new EmailServiceTokenModel(user, token, config.getHostNameUrl()));
        List<MimeMessage> messages = mailSender.getMessages();
        assertThat(messages.size(), is(1));
        MimeMessage message = messages.get(0);
        assertThat(message.getAllRecipients()[0].toString(), is((user.getEmailAddress())));
        Multipart multipart = (Multipart)message.getContent();
        String content = (String)multipart.getBodyPart(0).getContent();
        assertThat(content, containsString(new String(Base64.encodeBase64(token.getToken().getBytes()))));
    }


}