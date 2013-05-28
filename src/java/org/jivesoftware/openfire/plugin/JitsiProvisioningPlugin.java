/**
 * $Revision: 1722 $
 * $Date: 2005-07-28 15:19:16 -0700 (Thu, 28 Jul 2005) $
 *
 * Copyright (C) 2005-2008 Jive Software. All rights reserved.
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

package org.jivesoftware.openfire.plugin;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.lockout.LockOutManager;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.StringUtils;
import org.jivesoftware.util.PropertyEventListener;
import org.jivesoftware.util.PropertyEventDispatcher;

import java.io.File;
import java.util.*;

/**
 * Plugin that allows the administration of users via HTTP requests.
 *
 * @author Justin Hunt
 */
public class JitsiProvisioningPlugin implements Plugin, PropertyEventListener {
	
    private XMPPServer server;

    private String secret;
    private boolean enabled;
    private Collection<String> allowedIPs;

    public void initializePlugin(PluginManager manager, File pluginDirectory) {
        server = XMPPServer.getInstance();

        secret = JiveGlobals.getProperty("plugin.jitsiprovisioning.secret", "");
        // If no secret key has been assigned to the user service yet, assign a random one.
        if (secret.equals("")){
            secret = StringUtils.randomString(8);
            setSecret(secret);
        }
        
        // See if the service is enabled or not.
        enabled = JiveGlobals.getBooleanProperty("plugin.jitsiprovisioning.enabled", false);

        // Get the list of IP addresses that can use this service. An empty list means that this filter is disabled.
        allowedIPs = StringUtils.stringToCollection(JiveGlobals.getProperty("plugin.jitsiprovisioning.allowedIPs", ""));

        // Listen to system property events
        PropertyEventDispatcher.addListener(this);
    }

    public void destroyPlugin() {
        // Stop listening to system property events
        PropertyEventDispatcher.removeListener(this);
    }

    /**
     * Returns the secret key that only valid requests should know.
     *
     * @return the secret key.
     */
    public String getSecret() {
        return secret;
    }

    /**
     * Sets the secret key that grants permission to use the userservice.
     *
     * @param secret the secret key.
     */
    public void setSecret(String secret) {
        JiveGlobals.setProperty("plugin.jitsiprovisioning.secret", secret);
        this.secret = secret;
    }

    public Collection<String> getAllowedIPs() {
        return allowedIPs;
    }

    public void setAllowedIPs(Collection<String> allowedIPs) {
        JiveGlobals.setProperty("plugin.jitsiprovisioning.allowedIPs", StringUtils.collectionToString(allowedIPs));
        this.allowedIPs = allowedIPs;
    }

    /**
     * Returns true if the user service is enabled. If not enabled, it will not accept
     * requests to create new accounts.
     *
     * @return true if the user service is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Enables or disables the user service. If not enabled, it will not accept
     * requests to create new accounts.
     *
     * @param enabled true if the user service should be enabled.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        JiveGlobals.setProperty("plugin.jitsiprovisioning.enabled",  enabled ? "true" : "false");
    }

    public void propertySet(String property, Map<String, Object> params) {
        if (property.equals("plugin.jitsiprovisioning.secret")) {
            this.secret = (String)params.get("value");
        }
        else if (property.equals("plugin.jitsiprovisioning.enabled")) {
            this.enabled = Boolean.parseBoolean((String)params.get("value"));
        }
        else if (property.equals("plugin.jitsiprovisioning.allowedIPs")) {
            this.allowedIPs = StringUtils.stringToCollection((String)params.get("value"));
        }
    }

    public void propertyDeleted(String property, Map<String, Object> params) {
        if (property.equals("plugin.jitsiprovisioning.secret")) {
            this.secret = "";
        }
        else if (property.equals("plugin.jitsiprovisioning.enabled")) {
            this.enabled = false;
        }
        else if (property.equals("plugin.jitsiprovisioning.allowedIPs")) {
            this.allowedIPs = Collections.emptyList();
        }
    }

    public void xmlPropertySet(String property, Map<String, Object> params) {
        // Do nothing
    }

    public void xmlPropertyDeleted(String property, Map<String, Object> params) {
        // Do nothing
    }
}
