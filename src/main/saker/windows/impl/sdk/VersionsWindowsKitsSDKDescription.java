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
import java.util.Collections;
import java.util.NavigableSet;
import java.util.Set;

import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.sdk.support.api.EnvironmentSDKDescription;
import saker.sdk.support.api.IndeterminateSDKDescription;
import saker.sdk.support.api.SDKDescription;
import saker.sdk.support.api.SDKReference;

public class VersionsWindowsKitsSDKDescription implements IndeterminateSDKDescription, Externalizable {
	private static final long serialVersionUID = 1L;

	private NavigableSet<String> versions;

	/**
	 * For {@link Externalizable}.
	 */
	public VersionsWindowsKitsSDKDescription() {
	}

	private VersionsWindowsKitsSDKDescription(Set<String> versions) {
		this.versions = ImmutableUtils.makeImmutableNavigableSet(versions);
	}

	public static SDKDescription create(Set<String> versions) {
		return new VersionsWindowsKitsSDKDescription(versions);
	}

	@Override
	public SDKDescription getBaseSDKDescription() {
		return EnvironmentSDKDescription.create(new VersionsWindowsKitsSDKReferenceEnvironmentProperty(versions));
	}

	@Override
	public SDKDescription pinSDKDescription(SDKReference sdkreference) {
		if (sdkreference instanceof WindowsKitsSDKReference) {
			return EnvironmentSDKDescription.create(new VersionsWindowsKitsSDKReferenceEnvironmentProperty(
					Collections.singleton(((WindowsKitsSDKReference) sdkreference).getVersion())));
		}
		//shouldn't happen, but handle just in case
		return getBaseSDKDescription();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		SerialUtils.writeExternalCollection(out, versions);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		versions = SerialUtils.readExternalSortedImmutableNavigableSet(in);
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
		VersionsWindowsKitsSDKDescription other = (VersionsWindowsKitsSDKDescription) obj;
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
