package org.libvirt.sample;

import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;

class EventSample {
	volatile static boolean keepGoing = true;

	public static void main(String[] args) throws LibvirtException {
		Connect c = null;
		try {
			Connect.initEventLoop();

			c = new Connect("qemu:///system");

			c.setKeepAlive(3, 10);

			int cb1 = c.domainEventRegister(new Connect.DomainEvent.LifecycleCallback() {
				@Override
				public void onLifecycleChange(Connect connect, Domain d,
						Connect.DomainEvent.LifecycleCallback.Event event, int detail) {
					try {
						System.out.println("lifecycle change: " + d.getName() + " " + event + " " + detail);
					} catch (LibvirtException e) {
						e.printStackTrace();
					}
				}
			});

			System.out.println("Press Ctrl+D to exit.\n");

			new Thread() {
				@Override
				public void run() {
					try {
						while (System.in.read() > 0) {
							// NOOP
						}
					} catch (java.io.IOException e) {
					}
					keepGoing = false;
				}
			}.start();

			while (keepGoing && c.isAlive()) {
				c.processEvent();
			}
			c.domainEventDeregister(cb1);
		} finally {
			if (c != null) {
				c.close();
			}
		}
	}
}
