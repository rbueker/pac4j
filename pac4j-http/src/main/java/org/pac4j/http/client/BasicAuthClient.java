/*
  Copyright 2012 - 2015 pac4j organization

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.pac4j.http.client;

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;
import org.pac4j.core.client.Mechanism;
import org.pac4j.core.client.RedirectAction;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.core.exception.RequiresHttpAction;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.http.credentials.UsernamePasswordAuthenticator;
import org.pac4j.http.credentials.UsernamePasswordCredentials;
import org.pac4j.http.profile.UsernameProfileCreator;

/**
 * <p>This class is the client to authenticate users through HTTP basic auth.</p>
 * <p>For authentication, the user is redirected to the callback url. If the user is not authenticated by basic auth, a
 * specific exception : {@link RequiresHttpAction} is returned which must be handled by the application to force
 * authentication.</p>
 * <p>The realm name can be defined using the {@link #setRealmName(String)} method.</p>
 * <p>It returns a {@link org.pac4j.http.profile.HttpProfile}.</p>
 * 
 * @see org.pac4j.http.profile.HttpProfile
 * @author Jerome Leleu
 * @since 1.4.0
 */
public class BasicAuthClient extends AbstractHeaderClient<UsernamePasswordCredentials> {

    public BasicAuthClient() {
        this(null, null);
    }

    public BasicAuthClient(final UsernamePasswordAuthenticator usernamePasswordAuthenticator) {
        this(usernamePasswordAuthenticator, null);
    }

    public BasicAuthClient(final UsernamePasswordAuthenticator usernamePasswordAuthenticator,
            final UsernameProfileCreator profilePopulator) {
        setRealmName("authentication required");
        setHeaderName(HttpConstants.AUTHORIZATION_HEADER);
        setPrefixHeader("Basic ");
        setAuthenticator(usernamePasswordAuthenticator);
        setProfileCreator(profilePopulator);
    }

    @Override
    protected void internalInit() {
        super.internalInit();
        CommonHelper.assertNotBlank("callbackUrl", this.callbackUrl);
        CommonHelper.assertNotBlank("realmName", getRealmName());
    }

    @Override
    protected BasicAuthClient newClient() {
        final BasicAuthClient newClient = new BasicAuthClient();
        newClient.setRealmName(getRealmName());
        newClient.setHeaderName(getHeaderName());
        newClient.setPrefixHeader(getPrefixHeader());
        return newClient;
    }

    @Override
    protected RedirectAction retrieveRedirectAction(final WebContext context) {
        return RedirectAction.redirect(getContextualCallbackUrl(context));
    }

    @Override
    protected UsernamePasswordCredentials retrieveCredentialsFromHeader(String header) {
        final byte[] decoded = Base64.decodeBase64(header);

        String token;
        try {
            token = new String(decoded, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new CredentialsException("Bad format of the basic auth header");
        }

        final int delim = token.indexOf(":");
        if (delim < 0) {
            throw new CredentialsException("Bad format of the basic auth header");
        }
        return new UsernamePasswordCredentials(token.substring(0, delim),
                token.substring(delim + 1), getName());
    }

    @Override
    public String toString() {
        return CommonHelper.toString(this.getClass(), "callbackUrl", this.callbackUrl, "name", getName(), "realmName",
                getRealmName(), "headerName", getHeaderName(), "prefixHeader", getPrefixHeader(), "authenticator",
                getAuthenticator(), "profileCreator",
                getProfileCreator());
    }

    @Override
    public Mechanism getMechanism() {
        return Mechanism.BASICAUTH_MECHANISM;
    }
}
