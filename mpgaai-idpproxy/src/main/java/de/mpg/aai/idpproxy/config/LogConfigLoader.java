package de.mpg.aai.idpproxy.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.StatusManager;

public class LogConfigLoader {
	
	
	/**
	 * loads log config from given configuration file
	 * @param configCtx the current configuration context
	 * @throws ConfigException if something goes wrong
	 */
	public void load(ConfigContext configCtx) throws ConfigException {
		URL location = null;
		LoggerContext loggerCtx = (LoggerContext) LoggerFactory.getILoggerFactory();
		StatusManager statusManager = loggerCtx.getStatusManager();
		boolean started = loggerCtx.isStarted();
		try {
			location = configCtx.getLogConfLocation();
			InputStream logConf = location.openStream();
			// found|ioE otherwise
			statusManager.add(new InfoStatus("loading logging configuration file: " + location, this));
			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(loggerCtx);
			// do not reset if already started <=> would stop/remove already existing loggers
			if(!started) 
				loggerCtx.reset();
			configurator.doConfigure(logConf);
			if(!started)
				loggerCtx.start();
		} catch (JoranException jE) {
			statusManager.add(new ErrorStatus("Error loading logging configuration file: " + location, this, jE));
		} catch (MalformedURLException muE) {
			statusManager.add(new ErrorStatus("Error invalid logging configuration file location: " + location, this, muE));
		} catch (IOException ioE) {
			statusManager.add(new InfoStatus("no logging configuration file found at: " + location, this, ioE));
		}
	}
}
