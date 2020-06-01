package saker.windows.main.appx;

import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import java.util.Collection;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import saker.build.exception.InvalidPathFormatException;
import saker.build.file.SakerFile;
import saker.build.file.content.ContentDescriptor;
import saker.build.file.path.SakerPath;
import saker.build.file.provider.LocalFileProvider;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.CommonTaskContentDescriptors;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.utils.SimpleStructuredObjectTaskResult;
import saker.build.task.utils.annot.SakerInput;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.task.utils.dependencies.RecursiveFileCollectionStrategy;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.trace.BuildTrace;
import saker.nest.utils.FrontendTaskFactory;
import saker.std.api.file.location.ExecutionFileLocation;
import saker.std.api.file.location.FileLocation;
import saker.std.api.file.location.FileLocationVisitor;
import saker.std.api.file.location.LocalFileLocation;
import saker.std.api.util.SakerStandardUtils;
import saker.std.main.file.option.FileLocationTaskOption;
import saker.std.main.file.option.MultiFileLocationTaskOption;
import saker.std.main.file.utils.TaskOptionUtils;
import saker.windows.impl.appx.PrepareAppxWorkerTaskFactory;
import saker.windows.impl.appx.PrepareAppxWorkerTaskIdentifier;

public class PrepareAppxTaskFactory extends FrontendTaskFactory<Object> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "saker.appx.prepare";

	@Override
	public ParameterizableTask<? extends Object> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<Object>() {

			@SakerInput(value = "AppxManifest", required = true)
			public FileLocationTaskOption appxManifestOption;

			@SakerInput(value = "Files")
			public Collection<MultiFileLocationTaskOption> filesOption;

			@SakerInput(value = "Output")
			public SakerPath outputOption;

			@Override
			public Object run(TaskContext taskcontext) throws Exception {
				if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
					BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_FRONTEND);
				}

				FileLocation appxfl = TaskOptionUtils.toFileLocation(appxManifestOption, taskcontext);
				filesOption = ObjectUtils.cloneArrayList(filesOption, MultiFileLocationTaskOption::clone);

				SakerPath outputpath;
				if (outputOption != null) {
					if (!outputOption.isForwardRelative()) {
						throw new InvalidPathFormatException("Output" + " must be forward relative: " + outputOption);
					}
					if (outputOption.getFileName() == null) {
						throw new InvalidPathFormatException("Output" + " must have a file name: " + outputOption);
					}
					outputpath = outputOption;
				} else {
					outputpath = inferOutputPathFromAppxManifest(taskcontext, appxfl);
				}
				NavigableMap<SakerPath, FileLocation> resources = new TreeMap<>();

				resources.put(SakerPath.valueOf("AppxManifest.xml"), appxfl);
				if (!ObjectUtils.isNullOrEmpty(filesOption)) {
					for (MultiFileLocationTaskOption fo : filesOption) {
						for (FileLocation fl : TaskOptionUtils.toFileLocations(fo, taskcontext, null)) {
							fl.accept(new FileLocationVisitor() {
								@Override
								public void visit(ExecutionFileLocation loc) {
									SakerPath locpath = loc.getPath();

									NavigableMap<SakerPath, SakerFile> dirfiles = taskcontext.getTaskUtilities()
											.collectFilesReportAdditionDependency(null,
													RecursiveFileCollectionStrategy.create(locpath));
									if (dirfiles.isEmpty()) {
										//not a directory
										resources.put(SakerPath.valueOf(locpath.getFileName()), fl);
										return;
									}
									int subpathidx = Math.max(locpath.getNameCount() - 1, 0);
									for (SakerPath path : dirfiles.keySet()) {
										resources.put(path.subPath(subpathidx), ExecutionFileLocation.create(path));
									}
									taskcontext.getTaskUtilities().reportInputFileDependency(null,
											ObjectUtils.singleValueMap(dirfiles.navigableKeySet(),
													CommonTaskContentDescriptors.PRESENT));
								}
							});
						}
					}
				}

				PrepareAppxWorkerTaskIdentifier workertaskid = new PrepareAppxWorkerTaskIdentifier(outputpath);
				PrepareAppxWorkerTaskFactory workertask = new PrepareAppxWorkerTaskFactory(resources);
				taskcontext.startTask(workertaskid, workertask, null);

				SimpleStructuredObjectTaskResult result = new SimpleStructuredObjectTaskResult(workertaskid);
				taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
				return result;
			}
		};
	}

	protected static SakerPath inferOutputPathFromAppxManifest(TaskContext taskcontext, FileLocation fl)
			throws Exception {
		SakerPath[] result = { null };

		fl.accept(new FileLocationVisitor() {
			@Override
			public void visit(LocalFileLocation loc) {
				ContentDescriptor cd = taskcontext.getTaskUtilities().getReportExecutionDependency(SakerStandardUtils
						.createLocalFileContentDescriptorExecutionProperty(loc.getLocalPath(), UUID.randomUUID()));
				if (cd == null) {
					throw ObjectUtils.sneakyThrow(
							new NoSuchFileException("Specified AppxManifest is not a file: " + loc.getLocalPath()));
				}
				try (InputStream is = LocalFileProvider.getInstance().openInputStream(loc.getLocalPath())) {
					result[0] = inferOutputPathFromAppxManifestStream(is);
				} catch (Exception e) {
					throw ObjectUtils.sneakyThrow(e);
				}
			}

			@Override
			public void visit(ExecutionFileLocation loc) {
				SakerFile f = taskcontext.getTaskUtilities().resolveFileAtPath(loc.getPath());
				if (f == null) {
					taskcontext.reportInputFileDependency(null, loc.getPath(),
							CommonTaskContentDescriptors.IS_NOT_FILE);
					throw ObjectUtils.sneakyThrow(
							new NoSuchFileException("Specified AppxManifest is not a file: " + loc.getPath()));
				}
				try (InputStream is = f.openInputStream()) {
					result[0] = inferOutputPathFromAppxManifestStream(is);
				} catch (Exception e) {
					throw ObjectUtils.sneakyThrow(e);
				}
			}
		});
		return result[0];
	}

	protected static SakerPath inferOutputPathFromAppxManifestStream(InputStream is) throws Exception {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		//not namespace aware
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(is);
		Element rootelem = doc.getDocumentElement();
		if (!"Package".equals(rootelem.getNodeName())) {
			throw new IllegalArgumentException(
					"Invalid AppxManifest.xml, expected Package root element instead of " + rootelem.getNodeName());
		}
		NodeList children = rootelem.getChildNodes();
		for (int i = 0, clen = children.getLength(); i < clen; i++) {
			Node item = children.item(i);
			if (item.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			if (!"Identity".equals(item.getNodeName())) {
				continue;
			}
			Element elem = (Element) item;
			String name = elem.getAttribute("Name");
			String version = elem.getAttribute("Version");
			String arch = elem.getAttribute("ProcessorArchitecture");
			SakerPath p = SakerPath.valueOf(name);
			if (!ObjectUtils.isNullOrEmpty(version)) {
				p = p.resolve(version);
			}
			if (!ObjectUtils.isNullOrEmpty(arch)) {
				p = p.resolve(arch);
			}
			if (SakerPath.EMPTY.equals(p)) {
				p = SakerPath.valueOf("default");
			}
			return p;
		}
		throw new IllegalArgumentException("Identity element not found in AppxManifest.");
	}
}
