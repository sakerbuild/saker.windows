/*
 * Copyright (C) 2020 Bence Sipka
 *
 * This program is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package saker.windows.impl.sdk;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;

import saker.build.exception.PropertyComputationFailedException;
import saker.build.file.path.SakerPath;
import saker.build.runtime.environment.SakerEnvironment;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.build.trace.BuildTrace;
import saker.build.trace.TraceContributorEnvironmentProperty;
import saker.sdk.support.api.SDKReference;
import saker.sdk.support.api.exc.SDKNotFoundException;
import saker.windows.api.SakerWindowsUtils;
import saker.windows.impl.SakerWindowsImplUtils;

public class VersionsWindowsUapSDKReferenceEnvironmentProperty
		implements TraceContributorEnvironmentProperty<SDKReference>, Externalizable {
	private static final long serialVersionUID = 1L;

	public static final String VERSIONED_INSTALL_LOCATION_ENV_PARAMETER_PREFIX = "saker.windows.sdk.uap.install.location.";

	private Set<String> versions;

	/**
	 * For {@link Externalizable}.
	 */
	public VersionsWindowsUapSDKReferenceEnvironmentProperty() {
	}

	public VersionsWindowsUapSDKReferenceEnvironmentProperty(Set<String> version) {
		this.versions = version;
	}

	@Override
	public SDKReference getCurrentValue(SakerEnvironment environment) throws Exception {
		Predicate<? super String> versionpredicate = SakerWindowsImplUtils.getSDKVersionsPredicate(versions);

		for (Entry<String, String> entry : environment.getUserParameters().entrySet()) {
			String key = entry.getKey();
			if (!key.startsWith(VERSIONED_INSTALL_LOCATION_ENV_PARAMETER_PREFIX)) {
				continue;
			}
			String verstr = key.substring(VERSIONED_INSTALL_LOCATION_ENV_PARAMETER_PREFIX.length());
			if (versionpredicate.test(verstr)) {
				SakerPath installdir = SakerPath.valueOf(entry.getValue());
				return new WindowsUapSDKReference(installdir, verstr);
			}
		}

		WindowsUapSDKReference sdkref;
		sdkref = SakerWindowsImplUtils.searchWindowsUapInProgramFiles(SakerWindowsImplUtils.PATH_PROGRAM_FILES_X86,
				versionpredicate);
		if (sdkref != null) {
			return sdkref;
		}
		sdkref = SakerWindowsImplUtils.searchWindowsUapInProgramFiles(SakerWindowsImplUtils.PATH_PROGRAM_FILES,
				versionpredicate);
		if (sdkref != null) {
			return sdkref;
		}
		throw new SDKNotFoundException("Windows UAP SDK not found for versions: "
				+ (versions == null ? "any" : StringUtils.toStringJoin(", ", versions)));
	}

	@Override
	public void contributeBuildTraceInformation(SDKReference propertyvalue,
			PropertyComputationFailedException thrownexception) {
		if (propertyvalue != null) {
			try {
				LinkedHashMap<Object, Object> values = new LinkedHashMap<>();
				LinkedHashMap<Object, Object> props = new LinkedHashMap<>();

				values.put("Windows UAP SDK "
						+ propertyvalue.getProperty(SakerWindowsUtils.SDK_WINDOWSUAP_PROPERTY_VERSION), props);
				props.put("Install location",
						propertyvalue.getPath(SakerWindowsUtils.SDK_WINDOWSUAP_PATH_HOME).toString());
				BuildTrace.setValues(values, BuildTrace.VALUE_CATEGORY_ENVIRONMENT);
			} catch (Exception e) {
				if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_014) {
					BuildTrace.ignoredException(e);
				}
			}
		} else {
			//exceptions as values supported since 0.8.14
			if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_014) {
				if (versions == null) {
					BuildTrace.setValues(ImmutableUtils.singletonMap("Windows UAP SDK", thrownexception.getCause()),
							BuildTrace.VALUE_CATEGORY_ENVIRONMENT);
				} else {
					for (String v : versions) {
						BuildTrace.setValues(
								ImmutableUtils.singletonMap("Windows UAP SDK " + v, thrownexception.getCause()),
								BuildTrace.VALUE_CATEGORY_ENVIRONMENT);
					}
				}
			}
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		SerialUtils.writeExternalCollection(out, versions);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		versions = SerialUtils.readExternalImmutableNavigableSet(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((versions == null) ? 0 : versions.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VersionsWindowsUapSDKReferenceEnvironmentProperty other = (VersionsWindowsUapSDKReferenceEnvironmentProperty) obj;
		if (versions == null) {
			if (other.versions != null)
				return false;
		} else if (!versions.equals(other.versions))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + versions + "]";
	}

}
