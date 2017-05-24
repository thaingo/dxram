package de.hhu.bsinfo.dxram.engine;

import com.google.gson.annotations.Expose;

/**
 * Provides configuration values for a service. Use this as a base class for all services to add further configuration values
 *
 * @author Stefan Nothaas, stefan.nothaas@hhu.de, 24.05.2017
 */
public class DXRAMServiceConfig {
    @Expose
    private String m_class;

    @Expose
    private String m_serviceClass;

    @Expose
    private boolean m_enabledForSuperpeer;

    @Expose
    private boolean m_enabledForPeer;

    /**
     * Constructor
     *
     * @param p_class
     *         Class extending the abstract service class of this configuration
     * @param p_enabledForSuperpeer
     *         True to enable the service if the node is a superpeer, false to disable
     * @param p_enabledForPeer
     *         True to enable the service if the node is a peer, false to disable
     */
    protected DXRAMServiceConfig(final Class<? extends AbstractDXRAMService> p_class, final boolean p_enabledForSuperpeer, final boolean p_enabledForPeer) {
        m_class = getClass().getName();
        m_serviceClass = p_class.getSimpleName();
        m_enabledForSuperpeer = p_enabledForSuperpeer;
        m_enabledForPeer = p_enabledForPeer;
    }

    /**
     * Get the fully qualified class name of the config class
     */
    public String getClassName() {
        return m_class;
    }

    /**
     * Get the fully qualified class name of the service of this configuration
     */
    public String getServiceClass() {
        return m_serviceClass;
    }

    /**
     * True to enable the service if the node is a superpeer, false to disable
     */
    public boolean isEnabledForSuperpeer() {
        return m_enabledForSuperpeer;
    }

    /**
     * True to enable the service if the node is a peer, false to disable
     */
    public boolean isEnabledForPeer() {
        return m_enabledForPeer;
    }
}
