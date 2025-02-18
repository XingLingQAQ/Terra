/*
 * Copyright (c) 2020-2024 Polyhedral Development
 *
 * The Terra API is licensed under the terms of the MIT License. For more details,
 * reference the LICENSE file in the common/api directory.
 */

package com.dfsek.terra.api.config;

import com.dfsek.tectonic.api.config.template.ConfigTemplate;
import com.dfsek.tectonic.api.exception.LoadException;

import com.dfsek.terra.api.Platform;


public interface ConfigFactory<C extends ConfigTemplate, O> {
    O build(C config, Platform platform) throws LoadException;
}
