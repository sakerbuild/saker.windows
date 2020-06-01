package saker.windows.api;

import java.util.Set;

import saker.sdk.support.api.SDKDescription;
import saker.sdk.support.api.SDKPathCollectionReference;
import saker.windows.impl.sdk.VersionsWindowsKitsSDKDescription;
import saker.windows.impl.sdk.VersionsWindowsUapSDKDescription;
import saker.windows.impl.sdk.WindowsAppCertKitSDKDescription;
import saker.windows.impl.sdk.WindowsUapApiContractsWinmdPathCollectionReference;
import saker.windows.impl.sdk.WindowsUapSDKReference;

public class SakerWindowsUtils {
	private static final SDKDescription DEFAULT_SDK_WINDOWSKITS = VersionsWindowsKitsSDKDescription.create(null);
	private static final SDKDescription DEFAULT_SDK_WINDOWSUAP = VersionsWindowsUapSDKDescription.create(null);
	private static final SDKDescription DEFAULT_SDK_WINDOWSAPPCERTKIT = new WindowsAppCertKitSDKDescription();

	public static final String SDK_NAME_WINDOWSKITS = "WindowsKits";
	public static final String SDK_NAME_WINDOWSUAP = "WindowsUap";
	public static final String SDK_NAME_WINDOWSAPPCERTKIT = "WindowsAppCertKit";

	public static final String SDK_WINDOWSKITS_PATH_INCLUDE_UCRT = "include.ucrt";
	public static final String SDK_WINDOWSKITS_PATH_INCLUDE_CPPWINRT = "include.cppwinrt";
	public static final String SDK_WINDOWSKITS_PATH_INCLUDE_WINRT = "include.winrt";
	public static final String SDK_WINDOWSKITS_PATH_INCLUDE_SHARED = "include.shared";
	public static final String SDK_WINDOWSKITS_PATH_INCLUDE_UM = "include.um";

	public static final String SDK_WINDOWSKITS_PATH_LIB_X86_UCRT = "lib.x86.ucrt";
	public static final String SDK_WINDOWSKITS_PATH_LIB_X86_UM = "lib.x86.um";

	public static final String SDK_WINDOWSKITS_PATH_LIB_X64_UCRT = "lib.x64.ucrt";
	public static final String SDK_WINDOWSKITS_PATH_LIB_X64_UM = "lib.x64.um";

	public static final String SDK_WINDOWSKITS_PATH_LIB_ARM_UCRT = "lib.arm.ucrt";
	public static final String SDK_WINDOWSKITS_PATH_LIB_ARM_UM = "lib.arm.um";

	public static final String SDK_WINDOWSKITS_PATH_LIB_ARM64_UCRT = "lib.arm64.ucrt";
	public static final String SDK_WINDOWSKITS_PATH_LIB_ARM64_UM = "lib.arm64.um";

	public static final String SDK_WINDOWSKITS_PATH_BIN_ARM64 = "bin.arm64";
	public static final String SDK_WINDOWSKITS_PATH_BIN_ARM = "bin.arm";
	public static final String SDK_WINDOWSKITS_PATH_BIN_X64 = "bin.x64";
	public static final String SDK_WINDOWSKITS_PATH_BIN_X86 = "bin.x86";

	public static final String SDK_WINDOWSKITS_PATH_MAKEAPPX_X86 = "exe.x86.makeappx";
	public static final String SDK_WINDOWSKITS_PATH_MAKEAPPX_X64 = "exe.x64.makeappx";

	public static final String SDK_WINDOWSKITS_PATH_APPXLAUNCHER = "exe.appxlauncher";

	public static final String SDK_WINDOWSKITS_PATH_SIGNTOOL_X86 = "exe.x86.signtool";
	public static final String SDK_WINDOWSKITS_PATH_SIGNTOOL_X64 = "exe.x64.signtool";
	public static final String SDK_WINDOWSKITS_PATH_SIGNTOOL_ARM = "exe.arm.signtool";
	public static final String SDK_WINDOWSKITS_PATH_SIGNTOOL_ARM64 = "exe.arm64.signtool";
	
	public static final String SDK_WINDOWSKITS_PATH_RC_X86 = "exe.x86.rc";
	public static final String SDK_WINDOWSKITS_PATH_RC_X64 = "exe.x64.rc";
	public static final String SDK_WINDOWSKITS_PATH_RC_ARM64 = "exe.arm64.rc";

	public static final String SDK_WINDOWSKITS_PROPERTY_VERSION = "version";

	public static final String SDK_WINDOWSUAP_PATH_PLATFORM_XML = "xml.platform";
	public static final String SDK_WINDOWSUAP_PATH_PREVIOUS_PLATFORMS_XML = "xml.previousplatforms";
	public static final String SDK_WINDOWSUAP_PATH_FEATURES_XML = "xml.features";
	public static final String SDK_WINDOWSUAP_PROPERTY_VERSION = "version";

	public static final String SDK_WINDOWSUAP_PATH_MAKEAPPX_X86 = "exe.x86.makeappx";
	public static final String SDK_WINDOWSUAP_PATH_MAKEAPPX_X64 = "exe.x64.makeappx";

	public static final String SDK_WINDOWSAPPCERTKIT_PATH_APPXLAUNCHER = "exe.appxlauncher";
	public static final String SDK_WINDOWSAPPCERTKIT_PATH_MAKEAPPX = "exe.makeappx";
	public static final String SDK_WINDOWSAPPCERTKIT_PATH_SIGNTOOL = "exe.signtool";

	private SakerWindowsUtils() {
		throw new UnsupportedOperationException();
	}

	public static SDKDescription getDefaultWindowsKitsSDK() {
		return DEFAULT_SDK_WINDOWSKITS;
	}

	public static SDKDescription getDefaultWindowsUapSDK() {
		return DEFAULT_SDK_WINDOWSUAP;
	}

	public static SDKDescription getDefaultWindowsAppCertKitSDK() {
		return DEFAULT_SDK_WINDOWSAPPCERTKIT;
	}

	public static SDKDescription getWindowsKitsSDKForVersions(Set<String> versions) {
		return VersionsWindowsKitsSDKDescription.create(versions);
	}

	public static SDKDescription getWindowsUapSDKForVersions(Set<String> versions) {
		return VersionsWindowsUapSDKDescription.create(versions);
	}

	public static String getWindowsUapApiContractWinmdSDKPathIdentifier(String name, String version) {
		return WindowsUapSDKReference.SDK_PATH_APICONTRACT_PREFIX + name + "/" + version;
	}

	public static SDKPathCollectionReference getWindowsUapApiContractsWinmdSDKPathCollectionReference() {
		return WindowsUapApiContractsWinmdPathCollectionReference.INSTANCE;
	}
}
