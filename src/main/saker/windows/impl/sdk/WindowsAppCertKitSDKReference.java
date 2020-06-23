package saker.windows.impl.sdk;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Locale;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.sdk.support.api.SDKReference;
import saker.windows.api.SakerWindowsUtils;

public class WindowsAppCertKitSDKReference implements SDKReference, Externalizable {
	private static final long serialVersionUID = 1L;

	/**
	 * Path to the base directory. e.g. <code>c:\Program Files (x86)\Windows Kits\10\</code>
	 */
	private transient SakerPath baseDirectory;

	/**
	 * For {@link Externalizable}.
	 */
	public WindowsAppCertKitSDKReference() {
	}

	public WindowsAppCertKitSDKReference(SakerPath baseDirectory) {
		this.baseDirectory = baseDirectory;
	}

	public SakerPath getBaseDirectory() {
		return baseDirectory;
	}

	@Override
	public SakerPath getPath(String identifier) throws Exception {
		if (identifier == null) {
			return null;
		}
		String loweridentifier = identifier.toLowerCase(Locale.ENGLISH);
		switch (loweridentifier) {
			case SakerWindowsUtils.SDK_WINDOWSAPPCERTKIT_PATH_HOME: {
				return baseDirectory;
			}
			case SakerWindowsUtils.SDK_WINDOWSAPPCERTKIT_PATH_APPXLAUNCHER: {
				return baseDirectory.resolve("microsoft.windows.softwarelogo.appxlauncher.exe");
			}
			case SakerWindowsUtils.SDK_WINDOWSAPPCERTKIT_PATH_MAKEAPPX: {
				return baseDirectory.resolve("makeappx.exe");
			}
			case SakerWindowsUtils.SDK_WINDOWSAPPCERTKIT_PATH_SIGNTOOL: {
				return baseDirectory.resolve("signtool.exe");
			}
		}
		return null;
	}

	@Override
	public String getProperty(String identifier) throws Exception {
		return null;
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
		return "WindowsAppCertKitSDKReference[" + (baseDirectory != null ? "baseDirectory=" + baseDirectory : "") + "]";
	}

}
