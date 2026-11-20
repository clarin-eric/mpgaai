package de.mpg.aai.shhaa.config;

import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

public class ConfigContextListener implements ServletContextListener {
    /** the logger */
    private static Logger log = LoggerFactory.getLogger(ConfigContextListener.class);
    /** (init-)parameter name for config-location */
    public static final String PARAM_NAME_CONFIG_LOCATION = "ShhaaConfigLocation";

    /** the configuration context */
    private ConfigContext configCtx;

    /**
     * default constructor
     */
    public ConfigContextListener() {
    }

    /** {@inheritDoc} */
    @Override
    public void contextDestroyed(ServletContextEvent event) {
        this.closeConfigContext(event.getServletContext());
    }

    /** {@inheritDoc} */
    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext servletCtx = event.getServletContext();
        this.initConfigContext(servletCtx);
    }

    /**
     * initializes the configuration context and loads the config
     * 
     * @param servletCtx the (web)applications servlet context
     * @see #getConfigFileLocationFromServletContext(ServletContext)
     * @see #setConfigContext(ConfigContext, ServletContext)
     */
    protected void initConfigContext(ServletContext servletCtx) {
        String location = null;
        try {
            log.debug("init configuration context");
            // first check if already done
            if (ConfigContext.getActiveConfigContext(servletCtx) != null)
                throw new IllegalStateException(
                        "cannot initialize context because there is already a root application context present - " +
                                "check whether you have multiple ConfigContext* definitions in your web.xml");

            // lookup config-file location and init ConfigContext
            location = getConfigFileLocationFromServletContext(servletCtx);
            URL locURL = location.startsWith("/")
                    ? servletCtx.getResource(location)
                    : new URL(location);

            ConfigContext ctx = new ConfigContext();
            ctx.init(locURL);
            setConfigContext(ctx, servletCtx);
        } catch (ConfigurationException cE) {
            log.error("failed to initialize configuration context: {}", cE.getMessage());
            throw cE;
        } catch (MalformedURLException muE) {
            throw new ConfigurationException("invalid configuration file location " + location, muE);
        }
    }

    /**
     * closes the configuration context
     * and removes it (as attribute) from the given servlet context
     * 
     * @param servletCtx
     */
    private void closeConfigContext(ServletContext servletCtx) {
        log.debug("closing configuration context");
        this.configCtx = null;
        servletCtx.removeAttribute(ConfigContext.CONTEXT_ID);
    }

    /**
     * retrieve the config-file location from the servlet
     * 
     * @param servletCtx the (web)applications servlet context
     * @return config-file location or {@link ConfigContext#DEFAULT_CONF_LOCATION}
     *         if not unset
     * @see #PARAM_NAME_CONFIG_LOCATION
     */
    protected String getConfigFileLocationFromServletContext(ServletContext servletCtx) {
        String location = null;

        // lookup config-file location
        location = servletCtx.getInitParameter(PARAM_NAME_CONFIG_LOCATION);
        if (location == null) {
            location = ConfigContext.DEFAULT_CONF_LOCATION;
            log.debug("no config-location found as init-parameter {}, fallback to {}", PARAM_NAME_CONFIG_LOCATION,
                    location);
        } else
            log.debug("found config-location from init-parameter {}: {}", PARAM_NAME_CONFIG_LOCATION, location);

        return location;
    }

    /**
     * set the {@link ConfigContext} in the {@link ServletContext}
     * 
     * @param configCtx  the configuration context to set
     * @param servletCtx the servlet context where the configuration context is to
     *                   be set as attribute {@link ConfigContext#CONTEXT_ID}
     * @see ServletContext#setAttribute(String, Object)
     * @apiNote intended for subclasses to set private {@link #configCtx} field
     */
    protected void setConfigContext(ConfigContext configCtx, ServletContext servletCtx) {
        this.configCtx = configCtx;
        servletCtx.setAttribute(ConfigContext.CONTEXT_ID, this.configCtx);
    }
}
