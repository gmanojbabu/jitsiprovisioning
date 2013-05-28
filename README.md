JitsiProvisioning
=================

Jitsi Provisioning Plugin provides the ability to authenticate users and provision the Jitsi client according to the Enterprise Needs. Provisioning is the feature that allows network and provider administrators to remotely configure Jitsi instances that they are responsible for. 	Jitsi’s provisioning module uses http. This means that, based on a few parameters like an IP or a mac layer address, or a user name and a password, this Plugin can feed to a freshly installed Jitsi all the details that it needs in order to start making calls, downloading updates or configure codec preferences.

Installation
=================

To build the plugin simply place the directory structure in the Openfire plugins directory. You can then use the ANT plugin target to build the plugin. After building simply place the .jar file that is created in the target/plugins directory in to your Openfire installation's plugin directory.
