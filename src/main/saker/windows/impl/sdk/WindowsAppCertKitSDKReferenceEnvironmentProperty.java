package saker.windows.impl.sdk;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map.Entry;

import saker.build.file.path.SakerPath;
import saker.build.file.provider.LocalFileProvider;
import saker.build.runtime.environment.EnvironmentProperty;
import saker.build.runtime.environment.SakerEnvironment;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.sdk.support.api.SDKReference;
import saker.sdk.support.api.exc.SDKNotFoundException;
import saker.windows.impl.SakerWindowsImplUtils;

public class WindowsAppCertKitSDKReferenceEnvironmentProperty
		implements EnvironmentProperty<SDKReference>, Externalizable {
	private static final long serialVersionUID = 1L;

	public static final WindowsAppCertKitSDKReferenceEnvironmentProperty INSTANCE = new WindowsAppCertKitSDKReferenceEnvironmentProperty();

	public WindowsAppCertKitSDKReferenceEnvironmentProperty() {
	}

	@Override
	public SDKReference getCurrentValue(SakerEnvironment environment) throws Exception {
		for (Entry<String, String> entry : environment.getUserParameters().entrySet()) {
			String key = entry.getKey();
			if (!key.startsWith(
					VersionsWindowsKitsSDKReferenceEnvironmentProperty.VERSIONED_INSTALL_LOCATION_ENV_PARAMETER_PREFIX)) {
				continue;
			}
			SakerPath installdir = SakerPath.valueOf(entry.getValue());
			SakerPath appcertexepath = installdir.resolve("App Certification Kit/appcert.exe");
			try {
				if (LocalFileProvider.getInstance().getFileAttributes(appcertexepath).isRegularFile()) {
					return new WindowsAppCertKitSDKReference(installdir);
				}
			} catch (IOException e) {
				continue;
			}
			return new WindowsAppCertKitSDKReference(installdir);
		}

		WindowsAppCertKitSDKReference sdkref;
		sdkref = SakerWindowsImplUtils
				.searchWindowsAppCertKitInProgramFiles(SakerWindowsImplUtils.PATH_PROGRAM_FILES_X86);
		if (sdkref != null) {
			return sdkref;
		}
		sdkref = SakerWindowsImplUtils.searchWindowsAppCertKitInProgramFiles(SakerWindowsImplUtils.PATH_PROGRAM_FILES);
		if (sdkref != null) {
			return sdkref;
		}
		throw new SDKNotFoundException("Windows App Certification Kit SDK not found.");
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
