package io.github.micrqwe.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * @author shaowenxing@cnstrong.cn
 * @since 20:49
 */
@Service
public class EmailService {
    @Autowired
    private JavaMailSender sender;
    @Value("${email.to}")
    private String tos;

    public void totalEmail(StringBuilder zixuanStr) {
        String[] d = tos.split(",");
        for (String to : d) {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom("shaowenxing@cnstrong.cn");
            mailMessage.setTo(to);
            mailMessage.setSubject("统计结果:");
            mailMessage.setText(zixuanStr.toString());
            sender.send(mailMessage);
        }

    }

}
