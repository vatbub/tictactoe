package com.github.vatbub.tictactoe.common;

/*-
 * #%L
 * tictactoe.common
 * %%
 * Copyright (C) 2016 - 2017 Frederik Kammel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.io.Serializable;

/**
 * If sent to a tictactoeserver, the server will try to pull a update from its maven repository
 */
public class UpdateServerRequest implements Serializable{
    private String totpPassword;

    /**
     * Get the totp password to authenticate
     * @return The totp password to authenticate
     */
    public String getTotpPassword() {
        return totpPassword;
    }

    /**
     * Set the totp password to authenticate
     * @param totpPassword The new totp password to authenticate
     */
    public void setTotpPassword(String totpPassword) {
        this.totpPassword = totpPassword;
    }
}
