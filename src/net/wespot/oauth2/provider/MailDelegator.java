package net.wespot.oauth2.provider;

import com.google.appengine.api.utils.SystemProperty;

import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ****************************************************************************
 * Copyright (C) 2013 Open Universiteit Nederland
 * <p/>
 * This library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * <p/>
 * Contributors: Stefaan Ternier
 * ****************************************************************************
 */
public class MailDelegator {

    private static final Logger logger = Logger.getLogger(MailDelegator.class.getName());

    public void changePassword(String toMail, String resetId) {
        String from = "no-reply@" + SystemProperty.applicationId.get() + ".appspotmail.com";

        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        String link = "http://" + SystemProperty.applicationId.get() + ".appspot.com/ResetPassword.jsp?resetId=" + resetId;

        String msgBody = "<html><body>";
        msgBody += "Hello,<br>";
        msgBody += "<p>";
        msgBody += "We were asked to reset your weSPOT account. Follow the instructions below if this request comes from you.";
        msgBody += "</p>";
        msgBody += "<p>";
        msgBody += "Ignore the E-Mail if the request to reset your password does not come from you. Don't worry, your account is safe.";
        msgBody += "</p>";
        msgBody += "<p>";
        msgBody += "Click the following link to set a new password.";
        msgBody += "</p>";
        msgBody += "<p>";
        msgBody += "<a href=\"" + link + "\">" + link + "</a>";
        msgBody += "</p>";
        msgBody += "<p>";
        msgBody += "If clicking the link doesn't work you can copy the link into your browser window or type it there directly.</p>";
        msgBody += "<p>";
        msgBody += "Regards,<br>The weSPOT team";
        msgBody += "</p>";
        msgBody += "</body></html>";
        System.out.println(msgBody);
        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from, "weSPOT account service"));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(toMail));
            msg.addRecipient(Message.RecipientType.BCC, new InternetAddress(from));
            msg.setSubject("weSPOT account - change password");

            final MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(msgBody, "text/html");
            final Multipart mp = new MimeMultipart();
            mp.addBodyPart(htmlPart);

            msg.setContent(mp);
            Transport.send(msg);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
