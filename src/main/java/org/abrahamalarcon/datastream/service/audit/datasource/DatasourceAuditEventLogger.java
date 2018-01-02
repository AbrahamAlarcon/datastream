package org.abrahamalarcon.datastream.service.audit.datasource;

import org.abrahamalarcon.datastream.service.audit.AuditEvent;
import org.abrahamalarcon.datastream.service.audit.AuditEventLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatasourceAuditEventLogger extends AuditEventLogger {
	private DatasourceAuditEventLogFormatter formatter = new DatasourceAuditEventLogFormatter();
	public final Logger logger = LoggerFactory.getLogger(this.getClass());

	public void log(AuditEvent event) {
		logger.info(getMessage(formatter, event));
	}
}
