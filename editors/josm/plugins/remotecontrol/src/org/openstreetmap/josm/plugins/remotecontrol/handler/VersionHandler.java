package org.openstreetmap.josm.plugins.remotecontrol.handler;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.plugins.remotecontrol.PermissionPref;
import org.openstreetmap.josm.plugins.remotecontrol.RequestHandler;
import org.openstreetmap.josm.plugins.remotecontrol.RequestHandlerBadRequestException;
import org.openstreetmap.josm.plugins.remotecontrol.RequestHandlerErrorException;
import org.openstreetmap.josm.plugins.remotecontrol.RequestProcessor;

/**
 * Handler for version request.
 */
public class VersionHandler extends RequestHandler {

	public static final String command = "/version";

	@Override
	protected void handleRequest() throws RequestHandlerErrorException,
			RequestHandlerBadRequestException {
		content = RequestProcessor.PROTOCOLVERSION;
		contentType = "application/json";
		if (args.containsKey("jsonp")) {
			content = args.get("jsonp")+ " && " + args.get("jsonp") + "(" + content + ")";
		}
	}

	@Override
	public String getPermissionMessage() {
		return tr("Remote Control has been asked to report its protocol version. This enables web sites to detect a running JOSM.");
	}

	public PermissionPref getPermissionPref()
	{
		return new PermissionPref("remotecontrol.permission.read-protocolversion",
				"RemoteControl: /version forbidden by preferences");
	}
}
