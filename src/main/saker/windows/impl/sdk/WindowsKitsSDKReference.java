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
import java.util.Locale;

import saker.build.file.path.SakerPath;
import saker.build.file.provider.SakerPathFiles;
import saker.sdk.support.api.SDKReference;
import saker.windows.api.SakerWindowsUtils;

public class WindowsKitsSDKReference implements SDKReference, Externalizable {
	private static final long serialVersionUID = 1L;

	private String version;
	/**
	 * Path to the base directory. e.g. <code>c:\Program Files (x86)\Windows Kits\10\</code>
	 */
	private transient SakerPath baseDirectory;

	/**
	 * For {@link Externalizable}.
	 */
	public WindowsKitsSDKReference() {
	}

	public WindowsKitsSDKReference(SakerPath baseDirectory, String version) {
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
			case SakerWindowsUtils.SDK_WINDOWSKITS_PATH_HOME: {
				return baseDirectory;
			}
			case SakerWindowsUtils.SDK_WINDOWSKITS_PATH_BIN_ARM: {
				return baseDirectory.resolve("bin", version, "arm");
			}
			case SakerWindowsUtils.SDK_WINDOWSKITS_PATH_BIN_ARM64: {
				return baseDirectory.resolve("bin", version, "arm64");
			}
			case SakerWindowsUtils.SDK_WINDOWSKITS_PATH_BIN_X64: {
				return baseDirectory.resolve("bin", version, "x64");
			}
			case SakerWindowsUtils.SDK_WINDOWSKITS_PATH_BIN_X86: {
				return baseDirectory.resolve("bin", version, "x86");
			}

			case SakerWindowsUtils.SDK_WINDOWSKITS_PATH_INCLUDE_UCRT: {
				return baseDirectory.resolve("Include", version, "ucrt");
			}
			case SakerWindowsUtils.SDK_WINDOWSKITS_PATH_INCLUDE_CPPWINRT: {
				return baseDirectory.resolve("Include", version, "cppwinrt");
			}
			case SakerWindowsUtils.SDK_WINDOWSKITS_PATH_INCLUDE_WINRT: {
				return baseDirectory.resolve("Include", version, "winrt");
			}
			case SakerWindowsUtils.SDK_WINDOWSKITS_PATH_INCLUDE_SHARED: {
				return baseDirectory.resolve("Include", version, "shared");
			}
			case SakerWindowsUtils.SDK_WINDOWSKITS_PATH_INCLUDE_UM: {
				return baseDirectory.resolve("Include", version, "um");
			}

			case SakerWindowsUtils.SDK_WINDOWSKITS_PATH_LIB_X64_UCRT: {
				return baseDirectory.resolve("Lib", version, "ucrt", "x64");
			}
			case SakerWindowsUtils.SDK_WINDOWSKITS_PATH_LIB_X64_UM: {
				return baseDirectory.resolve("Lib", version, "um", "x64");
			}

			case SakerWindowsUtils.SDK_WINDOWSKITS_PATH_LIB_X86_UCRT: {
				return baseDirectory.resolve("Lib", version, "ucrt", "x86");
			}
			case SakerWindowsUtils.SDK_WINDOWSKITS_PATH_LIB_X86_UM: {
				return baseDirectory.resolve("Lib", version, "um", "x86");
			}

			case SakerWindowsUtils.SDK_WINDOWSKITS_PATH_LIB_ARM64_UCRT: {
				return baseDirectory.resolve("Lib", version, "ucrt", "arm64");
			}
			case SakerWindowsUtils.SDK_WINDOWSKITS_PATH_LIB_ARM64_UM: {
				return baseDirectory.resolve("Lib", version, "um", "arm64");
			}

			case SakerWindowsUtils.SDK_WINDOWSKITS_PATH_LIB_ARM_UCRT: {
				return baseDirectory.resolve("Lib", version, "ucrt", "arm");
			}
			case SakerWindowsUtils.SDK_WINDOWSKITS_PATH_LIB_ARM_UM: {
				return baseDirectory.resolve("Lib", version, "um", "arm");
			}

			case SakerWindowsUtils.SDK_WINDOWSKITS_PATH_MAKEAPPX_X64: {
				return baseDirectory.resolve("bin", version, "x64", "makeappx.exe");
			}
			case SakerWindowsUtils.SDK_WINDOWSKITS_PATH_MAKEAPPX_X86: {
				return baseDirectory.resolve("bin", version, "x86", "makeappx.exe");
			}

			case SakerWindowsUtils.SDK_WINDOWSKITS_PATH_SIGNTOOL_X86: {
				return baseDirectory.resolve("bin", version, "x86", "signtool.exe");
			}
			case SakerWindowsUtils.SDK_WINDOWSKITS_PATH_SIGNTOOL_X64: {
				return baseDirectory.resolve("bin", version, "x64", "signtool.exe");
			}
			case SakerWindowsUtils.SDK_WINDOWSKITS_PATH_SIGNTOOL_ARM: {
				return baseDirectory.resolve("bin", version, "arm", "signtool.exe");
			}
			case SakerWindowsUtils.SDK_WINDOWSKITS_PATH_SIGNTOOL_ARM64: {
				return baseDirectory.resolve("bin", version, "arm64", "signtool.exe");
			}

			case SakerWindowsUtils.SDK_WINDOWSKITS_PATH_RC_X86: {
				return baseDirectory.resolve("bin", version, "x86", "rc.exe");
			}
			case SakerWindowsUtils.SDK_WINDOWSKITS_PATH_RC_X64: {
				return baseDirectory.resolve("bin", version, "x64", "rc.exe");
			}
			case SakerWindowsUtils.SDK_WINDOWSKITS_PATH_RC_ARM64: {
				return baseDirectory.resolve("bin", version, "arm64", "rc.exe");
			}

			case SakerWindowsUtils.SDK_WINDOWSKITS_PATH_FXC_X86: {
				return baseDirectory.resolve("bin", version, "x86", "fxc.exe");
			}
			case SakerWindowsUtils.SDK_WINDOWSKITS_PATH_FXC_X64: {
				return baseDirectory.resolve("bin", version, "x64", "fxc.exe");
			}
			case SakerWindowsUtils.SDK_WINDOWSKITS_PATH_FXC_ARM64: {
				return baseDirectory.resolve("bin", version, "arm64", "fxc.exe");
			}

			case SakerWindowsUtils.SDK_WINDOWSKITS_PATH_APPXLAUNCHER: {
				return baseDirectory.resolve("App Certification Kit",
						"microsoft.windows.softwarelogo.appxlauncher.exe");
			}
		}
		return null;
	}

	@Override
	public String getProperty(String identifier) throws Exception {
		if (identifier == null) {
			return null;
		}
		switch (identifier.toLowerCase(Locale.ENGLISH)) {
			case SakerWindowsUtils.SDK_WINDOWSKITS_PROPERTY_VERSION: {
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
		WindowsKitsSDKReference other = (WindowsKitsSDKReference) obj;
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
