package saker.windows.impl.sdk;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Locale;

import saker.build.file.path.SakerPath;
import saker.build.file.provider.SakerPathFiles;
import saker.sdk.support.api.SDKReference;
import saker.sdk.support.api.exc.SDKPathNotFoundException;
import saker.windows.api.SakerWindowsUtils;

public class WindowsUapSDKReference implements SDKReference, Externalizable {
	private static final long serialVersionUID = 1L;

	public static final String SDK_PATH_APICONTRACT_PREFIX = "apicontract.";

	private String version;
	/**
	 * Same base directory as {@link WindowsKitsSDKReference}.
	 */
	private transient SakerPath baseDirectory;

	/**
	 * For {@link Externalizable}.
	 */
	public WindowsUapSDKReference() {
	}

	public WindowsUapSDKReference(SakerPath baseDirectory, String version) {
		SakerPathFiles.requireAbsolutePath(baseDirectory);
		this.baseDirectory = baseDirectory;
		this.version = version;
	}

	public SakerPath getBaseDirectory() {
		return baseDirectory;
	}

	public String getVersion() {
		return version;
	}

	@Override
	public SakerPath getPath(String identifier) throws Exception {
		if (identifier == null) {
			return null;
		}
		String loweridentifier = identifier.toLowerCase(Locale.ENGLISH);
		switch (loweridentifier) {
			case SakerWindowsUtils.SDK_WINDOWSUAP_PATH_HOME: {
				return baseDirectory;
			}
			case SakerWindowsUtils.SDK_WINDOWSUAP_PATH_PLATFORM_XML: {
				return baseDirectory.resolve("Platforms", "UAP", version, "Platform.xml");
			}
			case SakerWindowsUtils.SDK_WINDOWSUAP_PATH_PREVIOUS_PLATFORMS_XML: {
				return baseDirectory.resolve("Platforms", "UAP", version, "PreviousPlatforms.xml");
			}
			case SakerWindowsUtils.SDK_WINDOWSUAP_PATH_FEATURES_XML: {
				return baseDirectory.resolve("Platforms", "UAP", version, "Features.xml");
			}

			case SakerWindowsUtils.SDK_WINDOWSUAP_PATH_MAKEAPPX_X64: {
				return baseDirectory.resolve("bin", version, "x64", "makeappx.exe");
			}
			case SakerWindowsUtils.SDK_WINDOWSUAP_PATH_MAKEAPPX_X86: {
				return baseDirectory.resolve("bin", version, "x86", "makeappx.exe");
			}

			default: {
				if (loweridentifier.startsWith(SDK_PATH_APICONTRACT_PREFIX)) {
					int slashidx = identifier.lastIndexOf('/');
					if (slashidx < 0) {
						throw new SDKPathNotFoundException("Invalid apicontract SDK identifier: " + identifier);
					}
					String contractversion = identifier.substring(slashidx + 1);
					String contract = identifier.substring(SDK_PATH_APICONTRACT_PREFIX.length(), slashidx);
					return baseDirectory.resolve("References", this.version, contract, contractversion,
							contract + ".winmd");
				}
				break;
			}
		}
		return null;
	}

	@Override
	public String getProperty(String identifier) throws Exception {
		if (identifier == null) {
			return null;
		}
		String loweridentifier = identifier.toLowerCase(Locale.ENGLISH);
		switch (loweridentifier) {
			case SakerWindowsUtils.SDK_WINDOWSUAP_PROPERTY_VERSION: {
				return version;
			}
		}
		return null;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(baseDirectory);
		out.writeObject(version);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		baseDirectory = (SakerPath) in.readObject();
		version = (String) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((version == null) ? 0 : version.hashCode());
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
		WindowsUapSDKReference other = (WindowsUapSDKReference) obj;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (baseDirectory != null ? "baseDirectory=" + baseDirectory + ", " : "")
				+ (version != null ? "version=" + version : "") + "]";
	}

}
