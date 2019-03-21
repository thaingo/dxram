/*
 * Copyright (C) 2018 Heinrich-Heine-Universitaet Duesseldorf, Institute of Computer Science,
 * Department Operating Systems
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package de.hhu.bsinfo.dxram.engine;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Base class for all components in DXRAM. A component serves the engine as a building block
 * providing features for a specific task. Splitting features and concepts
 * across multiple components allows creating a clear structure and higher flexibility
 * for the whole system. Components are allowed to depend on other components i.e. directly
 * interact with each other.
 *
 * @author Stefan Nothaas, stefan.nothaas@hhu.de, 26.01.2016
 */
public abstract class Component<T> extends Module<T> {

    /**
     * Constructor
     */
    protected Component() {
        super();
    }

    @Override
    protected boolean moduleInit(final DXRAMEngine p_engine) {
        boolean ret;

        try {
            ret = initComponent(p_engine.getConfig(), p_engine.getJNIManager());
        } catch (final Exception e) {
            LOGGER.error("Initializing component failed", e);

            return false;
        }

        return ret;
    }

    @Override
    protected boolean moduleShutdown() {
        return shutdownComponent();
    }

    /**
     * Called when the component is initialized. Setup data structures, read settings etc.
     *
     * @param p_config
     *         Configuration instance provided by the engine.
     * @param p_jniManager
     *         Instance of JNI manager to load JNI libraries
     * @return True if initialing was successful, false otherwise.
     */
    protected abstract boolean initComponent(final DXRAMConfig p_config, final DXRAMJNIManager p_jniManager);

    /**
     * Called when the component gets shut down. Cleanup any resources of your component in here.
     *
     * @return True if shutdown was successful, false otherwise.
     */
    protected abstract boolean shutdownComponent();
}
