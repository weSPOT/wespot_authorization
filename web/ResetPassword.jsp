<%@ page import="net.wespot.oauth2.provider.AccountService" %>
<%@ page import="net.wespot.db.AccountReset" %>
<%@ page import="sun.tools.jar.resources.jar" %>
<%@ page import="net.wespot.oauth2.provider.MailDelegator" %>
<%@ page import="net.wespot.db.Account" %>
<!DOCTYPE html>
<html>
<head lang="en">
    <meta charset="UTF-8">
    <title>New Password</title>
    <meta name="viewport" content="width=device-width, height=device-height, initial-scale=1.0"/>

    <link rel="stylesheet" href="Login.css">
    <script src="jquery-1.9.1.min.js"></script>
</head>
<body>

<figure id="background"></figure>
<%
    String resetId = request.getParameter("resetId");
    String password = request.getParameter("password");
    String school = request.getParameter("school");
    String username = request.getParameter("username");
    String email = request.getParameter("email");
    if (resetId == null && (username != null || email != null)) {
        if (!school.equals("0")) username = school + "_" + username;
        Account account = AccountService.resetAccountGetAccount(username, email);
        if (account == null) {
            response.sendRedirect("ResetPassword.html");
        } else {
            AccountReset ar = AccountService.resetAccount(account);
            new MailDelegator().changePassword(account.getEmail(), ar.getHash());
        }
        %>
<section id="content" data-role="content">
    <h1 class="title">An email has been sent with further instructions</h1>
</section>
        <%
    }
    if (resetId != null && password != null) {
        AccountService.resetPassword(resetId, password);

        %>
<section id="content" data-role="content">
    <h1 class="title">Your password was altered</h1>
</section>
<%
    } else if (resetId != null) {
        AccountReset ar = AccountService.getAccountReset(resetId);
        if (ar != null) {
        %>
<section id="content" data-role="content">
    <h1 class="title">Choose a new password for <%= ar.getIdentifier() %></h1>
    <form id="check-user" method="post">
        <fieldset>
            <input name="resetId" type="hidden" value="<%= resetId %>">
            <input id="password" name="password" placeholder="Password" type="password">
        </fieldset>
        <input class="submit-btn" type="submit" name="submit" value="Submit">
    </form>
</section>
<%
        } else {
        %>
<section id="content" data-role="content">
    <h1 class="title">Link expired!</h1>
</section>
        <%
        }
    }
%>


</body>
</html>
