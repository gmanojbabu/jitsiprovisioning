/**
 * $RCSfile$
 * $Revision: 1710 $
 * $Date: 2005-07-26 11:56:14 -0700 (Tue, 26 Jul 2005) $
 *
 * Copyright (C) 2004-2008 Jive Software. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jivesoftware.openfire.plugin.jitsiProvisioning;

import gnu.inet.encoding.Stringprep;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jivesoftware.admin.AuthCheckFilter;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.plugin.JitsiProvisioningPlugin;
import org.jivesoftware.openfire.user.UserAlreadyExistsException;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.jivesoftware.util.Log;
import org.xmpp.packet.JID;
import org.jivesoftware.openfire.auth.AuthFactory;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.jivesoftware.openfire.auth.ConnectionException;
import org.jivesoftware.openfire.auth.InternalUnauthenticatedException;


/**
 * Servlet that authenticates and returns jitsi client provisioning data
 * <p>
 * <p/>
 * The request <b>MUST</b> include the <b>secret,username,password</b> parameter. This parameter will be used
 * to authenticate the request. If this parameter is missing from the request then
 * an error will be logged and 401 will be returned.
 *
 * @author Manoj Babu
 */
public class JitsiProvisioningServlet extends HttpServlet {

    private JitsiProvisioningPlugin plugin;


    @Override
	public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        plugin = (JitsiProvisioningPlugin) XMPPServer.getInstance().getPluginManager().getPlugin("jitsiprovisioning");
 
        // Exclude this servlet from requiring the user to login
        AuthCheckFilter.addExclude("jitsiProvisioning/provision");
    }

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
		doPost(request, response);
	}
    
    @Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
				// Printwriter for writing out responses to browser
		        PrintWriter out = response.getWriter();

				String username = request.getParameter("username");
		        String password = request.getParameter("password");
		        String secret = request.getParameter("secret");

		        if (!plugin.getAllowedIPs().isEmpty()) {
		            // Get client's IP address
		            String ipAddress = request.getHeader("x-forwarded-for");
		            if (ipAddress == null) {
		                ipAddress = request.getHeader("X_FORWARDED_FOR");
		                if (ipAddress == null) {
		                    ipAddress = request.getHeader("X-Forward-For");
		                    if (ipAddress == null) {
		                        ipAddress = request.getRemoteAddr();
		                    }
		                }
		            }
		            if (!plugin.getAllowedIPs().contains(ipAddress)) {
		                Log.warn("Jitsi Provisioning rejected service to IP address: " + ipAddress);
		                replyError("RequestNotAuthorised",response, out);
		                return;
		            }
		        }



		       // Check that our plugin is enabled.
		        if (!plugin.isEnabled()) {
		            Log.warn("Jitsi Provisioning plugin is disabled: " + request.getQueryString());
		            replyError("JitsiProvisioning Disabled",response, out);
		            return;
		        }

		        // Check this request is authorised
		        if (secret == null || !secret.equals(plugin.getSecret())){
		            Log.warn("An unauthorised user service request was received: " + request.getQueryString());
		            replyError("RequestNotAuthorised",response, out);
		            return;
		         }

				// Some checking is required on the username
		        if (username == null){
					replyError("IllegalArgumentException",response, out);
		            return;
		        }


		        // Check the request type and process accordingly
		        try {
		            username = username.trim().toLowerCase();
		            username = JID.escapeNode(username);
		            username = Stringprep.nodeprep(username);
					try {
						AuthFactory.authenticate(username,password);
								replyProvisionMessage(username,password,response, out);
						}
			            catch (ConnectionException e) {
							replyError("ConnectionException",response, out);
			            }
						catch (InternalUnauthenticatedException e) {
							replyError("InternalUnauthenticatedException",response, out);
			            }
			            catch (UnauthorizedException e) {
							replyError("Invalid Username/Password",response, out);
			            }
		        }
		        catch (IllegalArgumentException e) {

		            replyError("IllegalArgumentException",response, out);
		        }
		        catch (Exception e) {
		            replyError(e.toString(),response, out);
		        }
    }

    @Override
	public void destroy() {
        super.destroy();
        // Release the excluded URL
        AuthCheckFilter.removeExclude("jitsiProvisioning/provision");
    }

	private void replyProvisionMessage(String username, String password,HttpServletResponse response,PrintWriter out)
	{
		String domain = "chat.softwaydev.com";
		response.setContentType("plain/text");
		String resStr = String.format("net.java.sip.communicator.impl.gui.main.configforms.SHOW_ACCOUNT_CONFIG=false\nnet.java.sip.communicator.impl.gui.main.configforms.SHOW_OPTIONS_WINDOW=false\n"+
			"net.java.sip.communicator.plugin.generalconfig.DISABLED=true\nnet.java.sip.communicator.plugin.notificationconfiguration.DISABLED=false\nnet.java.sip.communicator.plugin.advancedconfig.DISABLED=true\n"+
			"net.java.sip.communicator.plugin.chatconfig.DISABLED=true\nnet.java.sip.communicator.impl.gui.accounts=${null}\n"+
			"net.java.sip.communicator.impl.protocol=${null}\nnet.java.sip.communicator.impl.protocol.jabber.accxmpp=accxmpp\n"+
			"net.java.sip.communicator.impl.protocol.jabber.accxmpp.ACCOUNT_UID=Jabber\\:%s@%s@%s\nnet.java.sip.communicator.impl.protocol.jabber.accxmpp.ALLOW_NON_SECURE=false\n"+
			"net.java.sip.communicator.impl.protocol.jabber.accxmpp.AUTO_DISCOVER_JINGLE_NODES=false\nnet.java.sip.communicator.impl.protocol.jabber.accxmpp.AUTO_DISCOVER_STUN=false\nnet.java.sip.communicator.impl.protocol.jabber.accxmpp.AUTO_GENERATE_RESOURCE=true\nnet.java.sip.communicator.impl.protocol.jabber.accxmpp.BYPASS_GTALK_CAPABILITIES=false\nnet.java.sip.communicator.impl.protocol.jabber.accxmpp.DTMF_METHOD=AUTO_DTMF\n"+
			"net.java.sip.communicator.impl.protocol.jabber.accxmpp.PASSWORD=%s\nnet.java.sip.communicator.impl.protocol.jabber.accxmpp.GMAIL_NOTIFICATIONS_ENABLED=false\n"+
			"net.java.sip.communicator.impl.protocol.jabber.accxmpp.GOOGLE_CONTACTS_ENABLED=false\nnet.java.sip.communicator.impl.protocol.jabber.accxmpp.GTALK_ICE_ENABLED=true\nnet.java.sip.communicator.impl.protocol.jabber.accxmpp.ICE_ENABLED=true\nnet.java.sip.communicator.impl.protocol.jabber.accxmpp.IS_PREFERRED_PROTOCOL=false\nnet.java.sip.communicator.impl.protocol.jabber.accxmpp.JINGLE_NODES_ENABLED=true\n"+
			"net.java.sip.communicator.impl.protocol.jabber.accxmpp.OVERRIDE_PHONE_SUFFIX=\nnet.java.sip.communicator.impl.protocol.jabber.accxmpp.PROTOCOL_NAME=Jabber\nnet.java.sip.communicator.impl.protocol.jabber.accxmpp.RESOURCE=jitsi\nnet.java.sip.communicator.impl.protocol.jabber.accxmpp.RESOURCE_PRIORITY=30\nnet.java.sip.communicator.impl.protocol.jabber.accxmpp.SERVER_ADDRESS=%s\nnet.java.sip.communicator.impl.protocol.jabber.accxmpp.SERVER_PORT=5222\n"+
			"net.java.sip.communicator.impl.protocol.jabber.accxmpp.TELEPHONY_BYPASS_GTALK_CAPS=\nnet.java.sip.communicator.impl.protocol.jabber.accxmpp.UPNP_ENABLED=false\nnet.java.sip.communicator.impl.protocol.jabber.accxmpp.USER_ID=%s@%s\nnet.java.sip.communicator.impl.protocol.jabber.accxmpp.USE_DEFAULT_STUN_SERVER=true\nnet.java.sip.communicator.impl.protocol.jabber.accxmpp.CALLING_DISABLED=false\n",username,domain,domain,password,domain,username,domain);
    	out.println(resStr);
    	out.flush();
	}
	
    private void replyError(String error,HttpServletResponse response, PrintWriter out)
		throws ServletException,
		     java.io.IOException
		{
		response.setContentType("text/xml");        
			/* if the servlet tries to access a resource and finds out that the client is not
			authorized to access it - "401 Unauthorized" */

			        response.sendError(401, error);
				out.flush();
			  /* if the servlet tries to access a resource that is forbidden for this client and there
			is no further information on it - "403 Forbidden" */
			        //response.sendError(403,
			        //  "You are forbidden from viewing the requested component; no
			        //further information");

			/* if the servlet tries to access a resource that is not found given the client's provided
			URL - "404 Not Found" */
			        //response.sendError(404,
			        //"The server could not find the requested component");
			//response.sendError(HttpServletResponse.SC_UNAUTHORIZED, error);
    }
}
