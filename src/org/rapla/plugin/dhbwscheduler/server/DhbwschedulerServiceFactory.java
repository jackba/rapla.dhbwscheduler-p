package org.rapla.plugin.dhbwscheduler.server;

import org.rapla.entities.User;
import org.rapla.facade.RaplaComponent;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaContextException;
import org.rapla.plugin.dhbwscheduler.DhbwschedulerService;
import org.rapla.server.RemoteMethodFactory;
import org.rapla.server.RemoteSession;

public class DhbwschedulerServiceFactory extends RaplaComponent implements RemoteMethodFactory<DhbwschedulerService>
{

	public DhbwschedulerServiceFactory(RaplaContext context) {
		super(context);
	}

	@Override
	public DhbwschedulerService createService(RemoteSession remoteSession)
			throws RaplaContextException {
		User user = remoteSession.getUser();
		RaplaContext context = getContext();
		return new DhbwschedulerServiceImpl(context, user);
	}

}
