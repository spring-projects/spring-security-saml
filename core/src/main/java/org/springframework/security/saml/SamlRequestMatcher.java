/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.springframework.security.saml;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.saml.provider.HostedProvider;
import org.springframework.security.saml.provider.ProviderProvisioning;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import static org.springframework.security.saml.util.StringUtils.addAliasPath;
import static org.springframework.security.saml.util.StringUtils.appendSlash;
import static org.springframework.security.saml.util.StringUtils.stripEndingSlases;
import static org.springframework.security.saml.util.StringUtils.stripSlashes;

public class SamlRequestMatcher implements RequestMatcher {

	private ProviderProvisioning provisioning;
	private boolean matchAgainstAliasPath;
	private String path;

	public SamlRequestMatcher(ProviderProvisioning provisioning, String path) {
		this(provisioning, path, false);
	}

	public SamlRequestMatcher(ProviderProvisioning provisioning, String path, boolean matchAgainstAliasPath) {
		this.matchAgainstAliasPath = matchAgainstAliasPath;
		this.provisioning = provisioning;
		this.path = path;
	}

	public boolean isMatchAgainstAliasPath() {
		return matchAgainstAliasPath;
	}

	public SamlRequestMatcher setMatchAgainstAliasPath(boolean matchAgainstAliasPath) {
		this.matchAgainstAliasPath = matchAgainstAliasPath;
		return this;
	}

	@Override
	public boolean matches(HttpServletRequest request) {
		HostedProvider provider = this.provisioning.getHostedProvider(request);
		String prefix = provider.getConfiguration().getPrefix();
		String alias = provider.getConfiguration().getAlias();
		String path = this.path;
		String matcherUrl = getExpectedPath(prefix, alias, path);
		AntPathRequestMatcher matcher = new AntPathRequestMatcher(matcherUrl);
		return matcher.matches(request);
	}

	private String getExpectedPath(String prefix, String alias, String path) {
		String result = "/" + stripSlashes(prefix);
		result = stripEndingSlases(result) + "/" + stripSlashes(path);
		if (isMatchAgainstAliasPath()) {
			result = appendSlash(result);
			result = addAliasPath(result, alias);
		}
		result = result + "/**";
		return result;
	}
}
