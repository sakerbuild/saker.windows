package saker.windows.impl.sdk;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.runtime.environment.SakerEnvironment;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.sdk.support.api.EnvironmentSDKDescription;
import saker.sdk.support.api.SDKReference;

public class WindowsAppCertKitSDKDescription implements EnvironmentSDKDescription, Externalizable {
	private static final long serialVersionUID = 1L;

	public WindowsAppCertKitSDKDescription() {
	}

	@Override
	public SDKReference getSDK(SakerEnvironment environment) throws Exception {
		return environment.getEnvironmentPropertyCurrentValue(new WindowsAppCertKitSDKReferenceEnvironmentProperty());
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
	}

	@Override
	public int hashCode() {
		return getClass().getName().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return ObjectUtils.isSameClass(this, obj);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[]";
	}
}
