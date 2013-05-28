<%@ page import="java.util.*,
                 org.jivesoftware.openfire.XMPPServer,
                 org.jivesoftware.util.*,
                 org.jivesoftware.openfire.plugin.JitsiProvisioningPlugin"
    errorPage="error.jsp"
%>

<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt_rt" prefix="fmt" %>

<%-- Define Administration Bean --%>
<jsp:useBean id="admin" class="org.jivesoftware.util.WebManager"  />
<c:set var="admin" value="${admin.manager}" />
<% admin.init(request, response, session, application, out ); %>

<%  // Get parameters
    boolean save = request.getParameter("save") != null;
    boolean success = request.getParameter("success") != null;
    String secret = ParamUtils.getParameter(request, "secret");
    boolean enabled = ParamUtils.getBooleanParameter(request, "enabled");
    String allowedIPs = ParamUtils.getParameter(request, "allowedIPs");

    JitsiProvisioningPlugin plugin = (JitsiProvisioningPlugin) XMPPServer.getInstance().getPluginManager().getPlugin("jitsiprovisioning");

    // Handle a save
    Map errors = new HashMap();
    if (save) {
        if (errors.size() == 0) {
            plugin.setEnabled(enabled);
        	plugin.setSecret(secret);
            plugin.setAllowedIPs(StringUtils.stringToCollection(allowedIPs));
            response.sendRedirect("jitsi-provisioning.jsp?success=true");
            return;
        }
    }

    secret = plugin.getSecret();
    enabled = plugin.isEnabled();
    allowedIPs = StringUtils.collectionToString(plugin.getAllowedIPs());
%>

<html>
    <head>
        <title>User Service Properties</title>
        <meta name="pageID" content="jitsi-provisioning"/>
    </head>
    <body>


<p>
Use the form below to enable or disable the Jitsi Provisioning and configure the secret key.
By default the Jitsi Provisioning plugin is <strong>disabled</strong>, which means that
HTTP/HTTPS requests to the service will be ignored.
</p>

<%  if (success) { %>

    <div class="jive-success">
    <table cellpadding="0" cellspacing="0" border="0">
    <tbody>
        <tr><td class="jive-icon"><img src="images/success-16x16.gif" width="16" height="16" border="0"></td>
        <td class="jive-icon-label">
            Jitsi Provisioning properties edited successfully.
        </td></tr>
    </tbody>
    </table>
    </div><br>
<% } %>

<form action="jitsi-provisioning.jsp?save" method="post">

<fieldset>
    <legend>Jitsi Provisioning</legend>
    <div>
    <p>
    	This service is meant for Jitsi Provisioning. Provisioning is the feature that allows network and provider administrators to remotely configure Jitsi instances that they are responsible for.
		Jitsi’s provisioning module uses http. This means that, based on a few parameters like an IP or a mac layer address, or a user name and a password, this Plugin can feed to a freshly installed Jitsi all the details that it needs in order to start making calls, downloading updates or configure codec preferences.</p>

    <p>However, the presence of this service exposes a security risk. Therefore,
    a secret key is used to validate legitimate requests to this service. Moreover,
    for extra security you can specify the list of IP addresses that are allowed to
    use this service. An empty list means that the service can be accessed from any
    location. Addresses are delimited by commas.
    </p>
    <ul>
        <input type="radio" name="enabled" value="true" id="rb01"
        <%= ((enabled) ? "checked" : "") %>>
        <label for="rb01"><b>Enabled</b> - User service requests will be processed.</label>
        <br>
        <input type="radio" name="enabled" value="false" id="rb02"
         <%= ((!enabled) ? "checked" : "") %>>
        <label for="rb02"><b>Disabled</b> - User service requests will be ignored.</label>
        <br><br>

        <label for="text_secret">Secret key:</label>
        <input type="text" name="secret" value="<%= secret %>" id="text_secret">
        <br><br>

        <label for="text_secret">Allowed IP Addresses:</label>
        <textarea name="allowedIPs" cols="40" rows="3" wrap="virtual"><%= ((allowedIPs != null) ? allowedIPs : "") %></textarea>
    </ul>
    </div>
</fieldset>

<br><br>

<input type="submit" value="Save Settings">
</form>


</body>
</html>