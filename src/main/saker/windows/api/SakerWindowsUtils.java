package saker.windows.api;

import java.util.Objects;
import java.util.Set;

import saker.sdk.support.api.SDKDescription;
import saker.sdk.support.api.SDKPathCollectionReference;
import saker.windows.impl.sdk.VersionsWindowsKitsSDKDescription;
import saker.windows.impl.sdk.VersionsWindowsUapSDKDescription;
import saker.windows.impl.sdk.WindowsAppCertKitSDKDescription;
import saker.windows.impl.sdk.WindowsUapApiContractsWinmdPathCollectionReference;
import saker.windows.impl.sdk.WindowsUapSDKReference;

/**
 * Utility class for accessing the functionality of the saker.windows package.
 */
public class SakerWindowsUtils {
	private static final SDKDescription DEFAULT_SDK_WINDOWSKITS = VersionsWindowsKitsSDKDescription.create(null);
	private static final SDKDescription DEFAULT_SDK_WINDOWSUAP = VersionsWindowsUapSDKDescription.create(null);
	private static final SDKDescription DEFAULT_SDK_WINDOWSAPPCERTKIT = new WindowsAppCertKitSDKDescription();

	/**
	 * SDK name for Windows Kits.
	 * <p>
	 * SDK constants in this class starting with <code>SDK_WINDOWSKITS_*</code> can be used with this SDK.
	 */
	public static final String SDK_NAME_WINDOWSKITS = "WindowsKits";
	/**
	 * SDK name for the Windows Universal Application Platform. (Also known as UWP)
	 * <p>
	 * SDK constants in this class starting with <code>SDK_WINDOWSUAP_*</code> can be used with this SDK.
	 */
	public static final String SDK_NAME_WINDOWSUAP = "WindowsUap";
	/**
	 * SDK name for the Windows App Cert Kit.
	 * <p>
	 * SDK constants in this class starting with <code>SDK_WINDOWSAPPCERTKIT_*</code> can be used with this SDK.
	 */
	public static final String SDK_NAME_WINDOWSAPPCERTKIT = "WindowsAppCertKit";

	/**
	 * SDK path identifier for the ucrt include directory.
	 */
	public static final String SDK_WINDOWSKITS_PATH_INCLUDE_UCRT = "include.ucrt";
	/**
	 * SDK path identifier for the cppwinrt include directory.
	 */
	public static final String SDK_WINDOWSKITS_PATH_INCLUDE_CPPWINRT = "include.cppwinrt";
	/**
	 * SDK path identifier for the winrt include directory.
	 */
	public static final String SDK_WINDOWSKITS_PATH_INCLUDE_WINRT = "include.winrt";
	/**
	 * SDK path identifier for the shared include directory.
	 */
	public static final String SDK_WINDOWSKITS_PATH_INCLUDE_SHARED = "include.shared";
	/**
	 * SDK path identifier for the um include directory.
	 */
	public static final String SDK_WINDOWSKITS_PATH_INCLUDE_UM = "include.um";

	/**
	 * SDK path identifier for the x86 ucrt library directory.
	 */
	public static final String SDK_WINDOWSKITS_PATH_LIB_X86_UCRT = "lib.x86.ucrt";
	/**
	 * SDK path identifier for the x86 um library directory.
	 */
	public static final String SDK_WINDOWSKITS_PATH_LIB_X86_UM = "lib.x86.um";

	/**
	 * SDK path identifier for the x64 ucrt library directory.
	 */
	public static final String SDK_WINDOWSKITS_PATH_LIB_X64_UCRT = "lib.x64.ucrt";
	/**
	 * SDK path identifier for the x64 um library directory.
	 */
	public static final String SDK_WINDOWSKITS_PATH_LIB_X64_UM = "lib.x64.um";

	/**
	 * SDK path identifier for the arm ucrt library directory.
	 */
	public static final String SDK_WINDOWSKITS_PATH_LIB_ARM_UCRT = "lib.arm.ucrt";
	/**
	 * SDK path identifier for the arm um library directory.
	 */
	public static final String SDK_WINDOWSKITS_PATH_LIB_ARM_UM = "lib.arm.um";

	/**
	 * SDK path identifier for the arm64 ucrt library directory.
	 */
	public static final String SDK_WINDOWSKITS_PATH_LIB_ARM64_UCRT = "lib.arm64.ucrt";
	/**
	 * SDK path identifier for the arm64 um library directory.
	 */
	public static final String SDK_WINDOWSKITS_PATH_LIB_ARM64_UM = "lib.arm64.um";

	/**
	 * SDK path identifier for the bin directory contaning binaries that work on the arm64 architecture.
	 */
	public static final String SDK_WINDOWSKITS_PATH_BIN_ARM64 = "bin.arm64";
	/**
	 * SDK path identifier for the bin directory contaning binaries that work on the arm architecture.
	 */
	public static final String SDK_WINDOWSKITS_PATH_BIN_ARM = "bin.arm";
	/**
	 * SDK path identifier for the bin directory contaning binaries that work on the x64 architecture.
	 */
	public static final String SDK_WINDOWSKITS_PATH_BIN_X64 = "bin.x64";
	/**
	 * SDK path identifier for the bin directory contaning binaries that work on the x86 architecture.
	 */
	public static final String SDK_WINDOWSKITS_PATH_BIN_X86 = "bin.x86";

	/**
	 * SDK path identifier for the makeappx tool that runs on x86 architecture.
	 */
	public static final String SDK_WINDOWSKITS_PATH_MAKEAPPX_X86 = "exe.x86.makeappx";
	/**
	 * SDK path identifier for the makeappx tool that runs on x64 architecture.
	 */
	public static final String SDK_WINDOWSKITS_PATH_MAKEAPPX_X64 = "exe.x64.makeappx";

	/**
	 * SDK path identifier for the appxlauncher tool.
	 */
	public static final String SDK_WINDOWSKITS_PATH_APPXLAUNCHER = "exe.appxlauncher";

	/**
	 * SDK path identifier for signtool that runs on x86 architecture.
	 */
	public static final String SDK_WINDOWSKITS_PATH_SIGNTOOL_X86 = "exe.x86.signtool";
	/**
	 * SDK path identifier for signtool that runs on x86 architecture.
	 */
	public static final String SDK_WINDOWSKITS_PATH_SIGNTOOL_X64 = "exe.x64.signtool";
	/**
	 * SDK path identifier for signtool that runs on arm architecture.
	 */
	public static final String SDK_WINDOWSKITS_PATH_SIGNTOOL_ARM = "exe.arm.signtool";
	/**
	 * SDK path identifier for signtool that runs on arm64 architecture.
	 */
	public static final String SDK_WINDOWSKITS_PATH_SIGNTOOL_ARM64 = "exe.arm64.signtool";

	/**
	 * SDK path identifier for the rc tool that runs on x86 architecture.
	 */
	public static final String SDK_WINDOWSKITS_PATH_RC_X86 = "exe.x86.rc";
	/**
	 * SDK path identifier for the rc tool that runs on x64 architecture.
	 */
	public static final String SDK_WINDOWSKITS_PATH_RC_X64 = "exe.x64.rc";
	/**
	 * SDK path identifier for the rc tool that runs on arm64 architecture.
	 */
	public static final String SDK_WINDOWSKITS_PATH_RC_ARM64 = "exe.arm64.rc";

	/**
	 * SDK path identifier for the fxc tool that runs on x86 architecture.
	 */
	public static final String SDK_WINDOWSKITS_PATH_FXC_X86 = "exe.x86.fxc";
	/**
	 * SDK path identifier for the fxc tool that runs on x64 architecture.
	 */
	public static final String SDK_WINDOWSKITS_PATH_FXC_X64 = "exe.x64.fxc";
	/**
	 * SDK path identifier for the fxc tool that runs on arm64 architecture.
	 */
	public static final String SDK_WINDOWSKITS_PATH_FXC_ARM64 = "exe.arm64.fxc";

	/**
	 * SDK property identifier for the version of the WindowsKits SDK.
	 */
	public static final String SDK_WINDOWSKITS_PROPERTY_VERSION = "version";

	/**
	 * SDK path identifier for the Platform.xml file.
	 * <p>
	 * The Platform.xml file is usually locateded at
	 * <code>Windows Kits/10/Platforms/UAP/&lt;VERSION&gt;/Platform.xml</code>.
	 */
	public static final String SDK_WINDOWSUAP_PATH_PLATFORM_XML = "xml.platform";
	/**
	 * SDK path identifier for the PreviousPlatforms.xml file.
	 * <p>
	 * The Platform.xml file is usually locateded at
	 * <code>Windows Kits/10/Platforms/UAP/&lt;VERSION&gt;/PreviousPlatforms.xml</code>.
	 */
	public static final String SDK_WINDOWSUAP_PATH_PREVIOUS_PLATFORMS_XML = "xml.previousplatforms";
	/**
	 * SDK path identifier for the Features.xml file.
	 * <p>
	 * The Platform.xml file is usually locateded at
	 * <code>Windows Kits/10/Platforms/UAP/&lt;VERSION&gt;/Features.xml</code>.
	 */
	public static final String SDK_WINDOWSUAP_PATH_FEATURES_XML = "xml.features";
	/**
	 * SDK property identifier for the version of the Windows Universal Application Platform SDK.
	 */
	public static final String SDK_WINDOWSUAP_PROPERTY_VERSION = "version";

	/**
	 * SDK path identifier for the makeappx tool that runs on x86 architecture.
	 */
	public static final String SDK_WINDOWSUAP_PATH_MAKEAPPX_X86 = "exe.x86.makeappx";
	/**
	 * SDK path identifier for the makeappx tool that runs on x64 architecture.
	 */
	public static final String SDK_WINDOWSUAP_PATH_MAKEAPPX_X64 = "exe.x64.makeappx";

	/**
	 * SDK path identifier for the appxlauncher tool.
	 */
	public static final String SDK_WINDOWSAPPCERTKIT_PATH_APPXLAUNCHER = "exe.appxlauncher";
	/**
	 * SDK path identifier for the makeappx tool.
	 */
	public static final String SDK_WINDOWSAPPCERTKIT_PATH_MAKEAPPX = "exe.makeappx";
	/**
	 * SDK path identifier for signtool.
	 */
	public static final String SDK_WINDOWSAPPCERTKIT_PATH_SIGNTOOL = "exe.signtool";

	private SakerWindowsUtils() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Gets the default Windows Kits SDK.
	 * 
	 * @return The SDK description.
	 */
	public static SDKDescription getDefaultWindowsKitsSDK() {
		return DEFAULT_SDK_WINDOWSKITS;
	}

	/**
	 * Gets the default Windows Universal Application Platform SDK.
	 * 
	 * @return The SDK description.
	 */
	public static SDKDescription getDefaultWindowsUapSDK() {
		return DEFAULT_SDK_WINDOWSUAP;
	}

	/**
	 * Gets the default Windows App Cert Kit SDK.
	 * 
	 * @return The SDK description.
	 */
	public static SDKDescription getDefaultWindowsAppCertKitSDK() {
		return DEFAULT_SDK_WINDOWSAPPCERTKIT;
	}

	/**
	 * Gets a Windows Kits SDK description for the specified versions.
	 * 
	 * @param versions
	 *            The versions or <code>null</code> to allow any version.
	 * @return The SDK description.
	 */
	public static SDKDescription getWindowsKitsSDKForVersions(Set<String> versions) {
		return VersionsWindowsKitsSDKDescription.create(versions);
	}

	/**
	 * Gets a Windows UAP SDK description for the specified versions.
	 * 
	 * @param versions
	 *            The versions or <code>null</code> to allow any version.
	 * @return The SDK description.
	 */
	public static SDKDescription getWindowsUapSDKForVersions(Set<String> versions) {
		return VersionsWindowsUapSDKDescription.create(versions);
	}

	/**
	 * Gets an SDK path identifier for the Windows UAP SDK that locates an API contract .winmd file for the specified
	 * contract name.
	 * <p>
	 * The path of an API contract .winmd is usually the following: <br>
	 * 
	 * <pre>
	 * Windows Kits/10/References/&lt;SDK_VERSION&gt;/&lt;CONTRACT_NAME&gt;/&lt;CONTRACT_VERSION&gt;/&lt;CONTRACT_NAME&gt;.winmd
	 * </pre>
	 * <p>
	 * E.g.: <br>
	 * 
	 * <pre>
	 * Windows Kits/10/References/10.0.18362.0/Windows.Foundation.FoundationContract/3.0.0.0/Windows.Foundation.FoundationContract.winmd
	 * </pre>
	 * <p>
	 * The <code>&lt;SDK_VERSION&gt;</code> is the version of the SDK the path is retrieved from, not part of the path
	 * identifier.
	 * 
	 * @param contractname
	 *            The contract name.
	 * @param version
	 *            The contract version.
	 * @return The path identifier.
	 * @throws NullPointerException
	 *             If any of the arguments are <code>null</code>.
	 */
	public static String getWindowsUapApiContractWinmdSDKPathIdentifier(String contractname, String version)
			throws NullPointerException {
		Objects.requireNonNull(contractname, "name");
		Objects.requireNonNull(version, "version");
		return WindowsUapSDKReference.SDK_PATH_APICONTRACT_PREFIX + contractname + "/" + version;
	}

	/**
	 * Gets an SDK path collection reference that returns the .winmd files for the default API contracts for the
	 * platform.
	 * <p>
	 * The path collection reference will collect the paths for the .winmd API contracts defined in the
	 * {@linkplain #SDK_WINDOWSUAP_PATH_PLATFORM_XML Platform.xml} file for the UAP SDK.
	 * 
	 * @return The SDK path collection reference.
	 */
	public static SDKPathCollectionReference getWindowsUapApiContractsWinmdSDKPathCollectionReference() {
		return WindowsUapApiContractsWinmdPathCollectionReference.INSTANCE;
	}
}
