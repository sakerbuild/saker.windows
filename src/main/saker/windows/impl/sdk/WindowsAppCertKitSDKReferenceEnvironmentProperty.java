package saker.windows.impl.sdk;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import saker.build.exception.PropertyComputationFailedException;
import saker.build.file.path.SakerPath;
import saker.build.file.provider.LocalFileProvider;
import saker.build.runtime.environment.SakerEnvironment;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.trace.BuildTrace;
import saker.build.trace.TraceContributorEnvironmentProperty;
import saker.sdk.support.api.SDKReference;
import saker.sdk.support.api.exc.SDKNotFoundException;
import saker.windows.api.SakerWindowsUtils;
import saker.windows.impl.SakerWindowsImplUtils;

public class WindowsAppCertKitSDKReferenceEnvironmentProperty
		implements TraceContributorEnvironmentProperty<SDKReference>, Externalizable {
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
					return new WindowsAppCertKitSDKReference(appcertexepath.getParent());
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
	public void contributeBuildTraceInformation(SDKReference propertyvalue,
			PropertyComputationFailedException thrownexception) {
		if (propertyvalue != null) {
			try {
				LinkedHashMap<Object, Object> values = new LinkedHashMap<>();
				LinkedHashMap<Object, Object> props = new LinkedHashMap<>();

				values.put("Windows App Cert Kit", props);
				props.put("Install location",
						propertyvalue.getPath(SakerWindowsUtils.SDK_WINDOWSAPPCERTKIT_PATH_HOME).toString());
				BuildTrace.setValues(values, BuildTrace.VALUE_CATEGORY_ENVIRONMENT);
			} catch (Exception e) {
				if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_014) {
					BuildTrace.ignoredException(e);
				}
			}
		} else {
			//exceptions as values supported since 0.8.14
			if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_014) {
				BuildTrace.setValues(ImmutableUtils.singletonMap("Windows App Cert Kit", thrownexception.getCause()),
						BuildTrace.VALUE_CATEGORY_ENVIRONMENT);
			}
		}
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
