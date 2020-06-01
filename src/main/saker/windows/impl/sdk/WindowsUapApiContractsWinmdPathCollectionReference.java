package saker.windows.impl.sdk;

import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import saker.build.file.path.SakerPath;
import saker.build.file.provider.LocalFileProvider;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.sdk.support.api.SDKPathCollectionReference;
import saker.sdk.support.api.SDKReference;
import saker.sdk.support.api.exc.SDKManagementException;
import saker.sdk.support.api.exc.SDKNotFoundException;
import saker.sdk.support.api.exc.SDKPathNotFoundException;
import saker.windows.api.SakerWindowsUtils;

public final class WindowsUapApiContractsWinmdPathCollectionReference implements SDKPathCollectionReference, Externalizable {
	private static final long serialVersionUID = 1L;

	public static final WindowsUapApiContractsWinmdPathCollectionReference INSTANCE = new WindowsUapApiContractsWinmdPathCollectionReference();

	/**
	 * For {@link Externalizable}.
	 */
	public WindowsUapApiContractsWinmdPathCollectionReference() {
	}

	@Override
	public Collection<SakerPath> getValue(Map<String, ? extends SDKReference> sdks)
			throws NullPointerException, Exception {
		SDKReference uapsdk = sdks.get(SakerWindowsUtils.SDK_NAME_WINDOWSUAP);
		if (uapsdk == null) {
			throw new SDKNotFoundException(SakerWindowsUtils.SDK_NAME_WINDOWSUAP);
		}
		SakerPath platformxmlpath = uapsdk.getPath(SakerWindowsUtils.SDK_WINDOWSUAP_PATH_PLATFORM_XML);
		if (platformxmlpath == null) {
			throw new SDKPathNotFoundException(
					"Platform.xml not found in SDK: " + SakerWindowsUtils.SDK_NAME_WINDOWSUAP);
		}
		Collection<SakerPath> result = new ArrayList<>();
		try (InputStream is = LocalFileProvider.getInstance().openInputStream(platformxmlpath)) {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			//not namespace aware
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(is);
			Element rootelem = doc.getDocumentElement();
			if (!"ApplicationPlatform".equals(rootelem.getNodeName())) {
				throw new IllegalArgumentException(
						"Invalid Platform.xml found in " + SakerWindowsUtils.SDK_NAME_WINDOWSUAP + " SDK.");
			}
			String version = rootelem.getAttribute("version");
			if (ObjectUtils.isNullOrEmpty(version)) {
				throw new IllegalArgumentException("No version attribute found.");
			}
			String uapsdkversion = uapsdk.getProperty(SakerWindowsUtils.SDK_WINDOWSUAP_PROPERTY_VERSION);
			if (!version.equals(uapsdkversion)) {
				throw new IllegalArgumentException(
						"Version mismatch in Platform.xml. Expected: " + version + " actual: " + uapsdkversion);
			}
			int foundnodes = 0;
			NodeList children = rootelem.getChildNodes();
			for (int i = 0, clen = children.getLength(); i < clen; i++) {
				Node item = children.item(i);
				if (item.getNodeType() != Node.ELEMENT_NODE) {
					continue;
				}
				if (!"ContainedApiContracts".equals(item.getNodeName())) {
					continue;
				}
				++foundnodes;
				NodeList contractchildren = item.getChildNodes();
				for (int j = 0, cclen = contractchildren.getLength(); j < cclen; j++) {
					Node contractitem = contractchildren.item(j);
					if (contractitem.getNodeType() != Node.ELEMENT_NODE) {
						continue;
					}
					if (!"ApiContract".equals(contractitem.getNodeName())) {
						continue;
					}
					Element contractelem = (Element) contractitem;
					String contractname = contractelem.getAttribute("name");
					String contractversion = contractelem.getAttribute("version");
					SakerPath winmdpath = uapsdk.getPath(SakerWindowsUtils
							.getWindowsUapApiContractWinmdSDKPathIdentifier(contractname, contractversion));
					if (winmdpath == null) {
						throw new SDKPathNotFoundException("ApiContract winmd SDK path not found for: " + contractname
								+ " / " + contractversion + " in SDK: " + uapsdk);
					}
					result.add(winmdpath);
				}
			}
			if (foundnodes == 0) {
				throw new IllegalArgumentException("ContainedApiContracts element not found in Platform.xml");
			}
		} catch (SDKManagementException e) {
			throw e;
		} catch (Exception e) {
			throw new SDKManagementException("Failed to parse Platform.xml at: " + platformxmlpath, e);
		}
		return result;
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