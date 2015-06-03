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
    <link rel="stylesheet" href="jquery.mobile-1.4.0.min.css" />
    <script src="jquery-1.9.1.min.js"></script>
    <script src="jquery.mobile-1.4.0.min.js"></script>
</head>
<body>

<%
String resetId = request.getParameter("resetId");
String password = request.getParameter("password");
    String school = request.getParameter("school");
    String username = request.getParameter("username");
    String email = request.getParameter("email");
    if (resetId == null && (username !=null ||email != null)){
        if (!school.equals("0")) username = school+"_"+username;
        Account account =AccountService.resetAccountGetAccount(username, email);
        if (account == null) {
            response.sendRedirect("ResetPassword.html");
        } else {
                AccountReset ar = AccountService.resetAccount(account);
               new MailDelegator().changePassword(account.getEmail(), ar.getHash());
        }
        %>


<div data-role="page" id="first" class="wrapper" data-theme="b" style="background-size: cover;background-image: url('/images/login_background.jpg')">
    <div data-role="content"  id="content">

        <h1> An email has been sent with further instructions</h1>
    </div>
</div>
        <%
    }
    if (resetId != null && password != null) {
        AccountService.resetPassword(resetId, password);

        %>


<div data-role="page" id="first" class="wrapper" data-theme="b" style="background-size: cover;background-image: url('/images/login_background.jpg')">
    <div data-role="content"  id="content">

        <h1> Your password was altered</h1>
    </div>
</div>
<%
    }else
    if (resetId!= null) {

        AccountReset ar = AccountService.getAccountReset(resetId);
        if (ar!= null) {
        %>
<div data-role="page" id="first" class="wrapper" data-theme="b" style="background-size: cover;background-image: url('/images/login_background.jpg')">
    <h1> Choose a new password for <%=ar.getIdentifier()%></h1>
    <div data-role="content"  id="content">
        <form id="check-user" class="ui-body ui-body-a ui-corner-all"  method="post">
            <fieldset>

                <div data-role="fieldcontain">
                    <input type="hidden" value="<%= resetId%>" name="resetId"/>
                    <input type="password" value="" name="password" id="password" placeholder="password" />
                </div>

                <input type="submit" data-theme="b" name="Submit"  value="Submit">
            </fieldset>
        </form>
    </div>
</div>


<%
        }
    }

%>


</body>
</html>