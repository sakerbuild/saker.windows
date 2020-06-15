package saker.windows.impl.appx.manifest;

import java.io.Externalizable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.util.Objects;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import saker.build.exception.InvalidPathFormatException;
import saker.build.file.SakerDirectory;
import saker.build.file.SakerFile;
import saker.build.file.SakerFileBase;
import saker.build.file.content.ContentDescriptor;
import saker.build.file.content.DirectoryContentDescriptor;
import saker.build.file.path.SakerPath;
import saker.build.file.provider.LocalFileProvider;
import saker.build.file.provider.SakerPathFiles;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.build.thirdparty.saker.util.io.function.IOSupplier;
import saker.build.trace.BuildTrace;
import saker.std.api.file.location.ExecutionFileLocation;
import saker.std.api.file.location.FileLocation;
import saker.std.api.file.location.FileLocationVisitor;
import saker.std.api.file.location.LocalFileLocation;
import saker.std.api.util.SakerStandardUtils;
import saker.windows.api.appx.manifest.PatchAppxManifestWorkerTaskOutput;
import saker.windows.main.appx.manifest.PatchAppxManifestTaskFactory;

public class PatchAppxManifestWorkerTaskFactory implements TaskFactory<Object>, Task<Object>, Externalizable {
	private static final long serialVersionUID = 1L;

	private FileLocation file;
	private String processorArchitecture;
	private String version;

	/**
	 * For {@link Externalizable}.
	 */
	public PatchAppxManifestWorkerTaskFactory() {
	}

	public PatchAppxManifestWorkerTaskFactory(FileLocation file) {
		this.file = file;
	}

	public void setProcessorArchitecture(String processorArchitecture) {
		this.processorArchitecture = processorArchitecture;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public Task<? extends Object> createTask(ExecutionContext executioncontext) {
		return this;
	}

	@Override
	public Object run(TaskContext taskcontext) throws Exception {
		if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
			BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_WORKER);
		}
		taskcontext.setStandardOutDisplayIdentifier(PatchAppxManifestTaskFactory.TASK_NAME);

		PatchAppxManifestWorkerTaskIdentifier taskid = (PatchAppxManifestWorkerTaskIdentifier) taskcontext.getTaskId();

		SakerPath outputrelativepath = taskid.getRelativeOutput();

		AppxManifestPatchingSakerFile[] addfile = { null };

		file.accept(new FileLocationVisitor() {
			@Override
			public void visit(LocalFileLocation loc) {
				SakerPath localpath = loc.getLocalPath();
				ContentDescriptor fcontents = taskcontext.getTaskUtilities()
						.getReportExecutionDependency(SakerStandardUtils
								.createLocalFileContentDescriptorExecutionProperty(localpath, UUID.randomUUID()));
				if (fcontents == null || fcontents instanceof DirectoryContentDescriptor) {
					throw ObjectUtils.sneakyThrow(new FileNotFoundException(localpath.toString()));
				}

				addfile[0] = new AppxManifestPatchingSakerFile(localpath.getFileName(),
						() -> LocalFileProvider.getInstance().openInputStream(localpath),
						new PatchedAppxManifestContentDescriptor(fcontents, processorArchitecture, version));
			}

			@Override
			public void visit(ExecutionFileLocation loc) {
				SakerFile f = taskcontext.getTaskUtilities().resolveFileAtPath(loc.getPath());
				if (f == null) {
					throw ObjectUtils.sneakyThrow(new FileNotFoundException(loc.getPath().toString()));
				}
				ContentDescriptor fcontents = f.getContentDescriptor();
				taskcontext.reportInputFileDependency(null, loc.getPath(), fcontents);
				addfile[0] = new AppxManifestPatchingSakerFile(f.getName(), f::openInputStreamImpl,
						new PatchedAppxManifestContentDescriptor(fcontents, processorArchitecture, version));
			}
		});

		SakerDirectory outputdir = taskcontext.getTaskUtilities().resolveDirectoryAtRelativePathCreate(
				SakerPathFiles.requireBuildDirectory(taskcontext), outputrelativepath.getParent());

		outputdir.add(addfile[0]);
		SakerPath outputfilesakerpath = addfile[0].getSakerPath();
		taskcontext.reportOutputFileDependency(null, outputfilesakerpath, addfile[0].getContentDescriptor());
		addfile[0].synchronize();

		PatchAppxManifestWorkerTaskOutput result = new PatchAppxManifestWorkerTaskOutputImpl(outputfilesakerpath);
		taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
		return result;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(file);
		out.writeObject(processorArchitecture);
		out.writeObject(version);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		file = SerialUtils.readExternalObject(in);
		processorArchitecture = SerialUtils.readExternalObject(in);
		version = SerialUtils.readExternalObject(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((file == null) ? 0 : file.hashCode());
		result = prime * result + ((processorArchitecture == null) ? 0 : processorArchitecture.hashCode());
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
		PatchAppxManifestWorkerTaskFactory other = (PatchAppxManifestWorkerTaskFactory) obj;
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.equals(other.file))
			return false;
		if (processorArchitecture == null) {
			if (other.processorArchitecture != null)
				return false;
		} else if (!processorArchitecture.equals(other.processorArchitecture))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}

	private static class AppxManifestPatchingSakerFile extends SakerFileBase {
		private static final String ELEM_PACKAGE = "Package";
		private static final String ELEM_IDENTITY = "Identity";
		private static final String ATTR_VERSION = "Version";
		private static final String ATTR_PROCESSOR_ARCHITECTURE = "ProcessorArchitecture";

		private IOSupplier<? extends InputStream> inputSupplier;
		private PatchedAppxManifestContentDescriptor contentDescriptor;

		public AppxManifestPatchingSakerFile(String name, IOSupplier<? extends InputStream> inputSupplier,
				PatchedAppxManifestContentDescriptor contentDescriptor)
				throws NullPointerException, InvalidPathFormatException {
			super(name);
			this.inputSupplier = inputSupplier;
			this.contentDescriptor = contentDescriptor;
		}

		@Override
		public ContentDescriptor getContentDescriptor() {
			return contentDescriptor;
		}

		@Override
		public void writeToStreamImpl(OutputStream os) throws IOException, NullPointerException {
			try (InputStream is = inputSupplier.get()) {
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				dbFactory.setNamespaceAware(true);
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(is);

				patchAppxManifestXml(doc);

				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				transformerFactory.setAttribute("indent-number", 4);
				Transformer transformer = transformerFactory.newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty(OutputKeys.METHOD, "xml");
				transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
				DOMSource source = new DOMSource(doc);
				transformer.transform(source, new StreamResult(os));
			} catch (IOException e) {
				throw e;
			} catch (Exception e) {
				throw new IOException("Failed to patch Appx manifest.", e);
			}
		}

		private void patchAppxManifestXml(Document doc) {
			Element rootelem = doc.getDocumentElement();
			if (!ELEM_PACKAGE.equals(rootelem.getLocalName())) {
				throw new IllegalArgumentException(
						"Invalid AppxManifest.xml, expected Package root element instead of " + rootelem.getNodeName());
			}
			String rootns = rootelem.getNamespaceURI();

			String arch = contentDescriptor.getProcessorArchitecture();
			String version = contentDescriptor.getVersion();
			NodeList children = rootelem.getChildNodes();
			for (int i = 0, clen = children.getLength(); i < clen; i++) {
				Node item = children.item(i);
				if (item.getNodeType() != Node.ELEMENT_NODE) {
					continue;
				}
				if (!ELEM_IDENTITY.equals(item.getLocalName())) {
					continue;
				}
				String elemns = item.getNamespaceURI();
				if (!Objects.equals(rootns, elemns)) {
					continue;
				}
				Element elem = (Element) item;
				String prefix = elem.getPrefix();
				if (prefix == null) {
					if (arch != null) {
						elem.setAttribute(ATTR_PROCESSOR_ARCHITECTURE, arch);
					}
					if (version != null) {
						elem.setAttribute(ATTR_VERSION, version);
					}
				} else {
					if (arch != null) {
						elem.setAttribute(prefix + ":" + ATTR_PROCESSOR_ARCHITECTURE, arch);
					}
					if (version != null) {
						elem.setAttribute(prefix + ":" + ATTR_VERSION, version);
					}
				}
				return;
			}
			throw new IllegalArgumentException("Identity element not found in AppxManifest.");
		}

	}

	private static class PatchedAppxManifestContentDescriptor implements ContentDescriptor, Externalizable {
		private static final long serialVersionUID = 1L;

		private ContentDescriptor originalContents;
		private String processorArchitecture;
		private String version;

		/**
		 * For {@link Externalizable}.
		 */
		public PatchedAppxManifestContentDescriptor() {
		}

		public PatchedAppxManifestContentDescriptor(ContentDescriptor originalContents, String processorArchitecture,
				String version) {
			this.originalContents = originalContents;
			this.processorArchitecture = processorArchitecture;
			this.version = version;
		}

		public ContentDescriptor getOriginalContents() {
			return originalContents;
		}

		public String getProcessorArchitecture() {
			return processorArchitecture;
		}

		public String getVersion() {
			return version;
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeObject(originalContents);
			out.writeObject(processorArchitecture);
			out.writeObject(version);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			originalContents = SerialUtils.readExternalObject(in);
			originalContents = SerialUtils.readExternalObject(in);
			version = SerialUtils.readExternalObject(in);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((originalContents == null) ? 0 : originalContents.hashCode());
			result = prime * result + ((processorArchitecture == null) ? 0 : processorArchitecture.hashCode());
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
			PatchedAppxManifestContentDescriptor other = (PatchedAppxManifestContentDescriptor) obj;
			if (originalContents == null) {
				if (other.originalContents != null)
					return false;
			} else if (!originalContents.equals(other.originalContents))
				return false;
			if (processorArchitecture == null) {
				if (other.processorArchitecture != null)
					return false;
			} else if (!processorArchitecture.equals(other.processorArchitecture))
				return false;
			if (version == null) {
				if (other.version != null)
					return false;
			} else if (!version.equals(other.version))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "PatchedAppxManifestContentDescriptor["
					+ (processorArchitecture != null ? "processorArchitecture=" + processorArchitecture + ", " : "")
					+ (version != null ? "version=" + version + ", " : "")
					+ (originalContents != null ? "originalContents=" + originalContents : "") + "]";
		}

	}

}
